package edu.yale.library.paperless.services;

import edu.yale.library.paperless.entities.*;
import edu.yale.library.paperless.report.UserBatchEmailGenerator;
import edu.yale.library.paperless.repositories.*;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserTaskBatchServiceImpl implements UserTaskBatchService {

    private final TaskAssignmentRepository taskAssignmentRepository;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final UserTaskBatchRepository userTaskBatchRepository;
    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;
    private final TaskStatusUpdateRepository taskStatusUpdateRepository;
    private final TaskFillProblemRepository taskFillProblemRepository;
    private final BatchResponseRepository batchResponseRepository;
    private final UserBatchTaskFillProblemRepository userBatchTaskFillProblemRepository;

    public UserTaskBatchServiceImpl(UserTaskBatchRepository userTaskBatchRepository,
                                    TaskRepository taskRepository,
                                    TaskLogRepository taskLogRepository,
                                    TaskStatusUpdateRepository taskStatusUpdateRepository,
                                    TaskFillProblemRepository taskFillProblemRepository,
                                    TaskAssignmentRepository taskAssignmentRepository,
                                    BatchResponseRepository batchResponseRepository,
                                    UserBatchTaskFillProblemRepository userBatchTaskFillProblemRepository
                                    ) {
        this.userTaskBatchRepository = userTaskBatchRepository;
        this.taskRepository = taskRepository;
        this.taskLogRepository = taskLogRepository;
        this.taskStatusUpdateRepository = taskStatusUpdateRepository;
        this.taskFillProblemRepository = taskFillProblemRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.batchResponseRepository = batchResponseRepository;
        this.userBatchTaskFillProblemRepository = userBatchTaskFillProblemRepository;
    }

    /***
     * Creates a new task batch using the list of tasks, or updates the existing batch if the user
     * has a batch which has not been started.
     * If a batch has been started but not completed, this marks the existing batch to complete and creates a new one.
     * @param user
     * @param tasks
     * @return
     */
    @Override
    @Transactional
    public UserTaskBatch createNewBatchForUser(User assigningUser, User user, TaskList tasks)
            throws UnauthorizedRequestException {
        List<Task> taskList = tasks.stream().map(task ->
                taskRepository.findById(task.getId()).orElse(null)).collect(Collectors.toList());

        boolean assignedTask = taskList.stream().anyMatch(task -> {
           return task.getCurrentBatch() != null;
        });

        if ( assignedTask ) {
            logger.error("Error creating new batch because one of the tasks had a current batch.");
            throw new UnauthorizedRequestException("Error creating new batch because one of the tasks had a current batch.");
        }

        UserTaskBatch userTaskBatch = userTaskBatchRepository.findCurrentBatchByUser(user);
        if (userTaskBatch != null) {
            logger.error("Error creating new batch because the user already had a batch.");
            throw new UnauthorizedRequestException("Error creating new batch because the user already had a batch.");
        } else {
            userTaskBatch = new UserTaskBatch();
            userTaskBatch.setUser(user);
        }
        userTaskBatch.setAssigningUser(assigningUser);
        UserTaskBatch batch = userTaskBatch;
        taskRepository.saveAll(userTaskBatch.getTasks());

        userTaskBatch.setTasks(taskList);
        UserTaskBatch ret = userTaskBatchRepository.save(userTaskBatch);
        userTaskBatch.getTasks().stream().forEach(task -> {
            task.setCurrentBatch(batch);
            taskLogRepository.log(task, assigningUser, "Assigned To Batch");
            TaskAssignment taskAssignment = new TaskAssignment();
            taskAssignment.setAssignedDateTime(new Timestamp(System.currentTimeMillis()));
            taskAssignment.setAssigner(assigningUser);
            taskAssignment.setRetriever(user);
            taskAssignment.setTaskStatus(task.getStatus());
            taskAssignment.setTask(task);
            taskAssignment.setItemBarcode(task.getItemBarcode());
            taskAssignmentRepository.save(taskAssignment);
        });
        taskRepository.saveAll(userTaskBatch.getTasks());
        return ret;
    }


    @Override
    @Transactional
    public UserTaskBatch submitTaskBatch(User submittingUser,
                                         UserTaskBatch userTaskBatch,
                                         TaskList tasks, boolean closeOutBatch) throws UnauthorizedRequestException {
        Timestamp submitDateTime = new Timestamp(System.currentTimeMillis());
        if (submittingUser.isAdmin() || submittingUser.isAssign() ||
                userTaskBatch.getUser().getId().equals(submittingUser.getId())) {
            if ( userTaskBatch.getEndTime() != null ) {
                logger.error("Batch already closed and resubmitted: " + userTaskBatch.getId());
                throw new UnauthorizedRequestException("Batch already closed and resubmitted: " + userTaskBatch.getId());
            }
            userTaskBatch.setEndTime(new Timestamp(System.currentTimeMillis()));
            Map<Long, Task> statusMap = new HashMap<>();
            tasks.forEach((cs -> statusMap.put(cs.getId(), cs)));
            UserTaskBatch batch = userTaskBatch;
            userTaskBatch.getTasks().forEach(task -> {
                task.setIncomingStatus(task.getStatus());
                if ( task.getTaskFillProblems() != null && task.getTaskFillProblems().size() == 0 ) {
                    task.setTaskFillProblems(null);
                }
                Task submittedTask = statusMap.get(task.getId());
                TaskStatus oldStatus = task.getStatus();
                if (submittedTask != null) {
                    taskLogRepository.log(task, submittingUser,
                            String.format("Batch save %s to %s. [Batch ID: %d]", oldStatus,
                                    submittedTask.getStatus(), batch.getId()));
                    if ( submittedTask.getStatus() != TaskStatus.New ) {
                        task.setStatus(submittedTask.getStatus());
                        if ( oldStatus != TaskStatus.New ) {
                            // if this is the 2nd pass, set to pass 2 statuses.
                            boolean fos = submittedTask.getStatus()== TaskStatus.FOS || submittedTask.getStatus()== TaskStatus.FOS_2x;
                            task.setStatus(fos? TaskStatus.FOS_2x: TaskStatus.NOS_2x);
                        } else {
                            task.setStatus(submittedTask.getStatus());
                        }
                    }
                    task.setTaskStatusDateTime(submitDateTime);
                    task.setTaskStatusOpid(submittingUser.getNetId());

                    if ( submittedTask.getTaskFillProblems() != null ) {
                        task.setTaskFillProblems(submittedTask.getTaskFillProblems().stream().map(
                                taskFillProblem ->
                                        taskFillProblemRepository.findById(taskFillProblem.getId()).orElse(null)
                        ).collect(Collectors.toList()));
                        submittedTask.getTaskFillProblems().stream().forEach(
                                taskFillProblem -> {
                                    UserBatchTaskFillProblem userBatchTaskFillProblem = new UserBatchTaskFillProblem();
                                    userBatchTaskFillProblem.setTaskFillProblem(taskFillProblem);
                                    userBatchTaskFillProblem.setUserTaskBatch(batch);
                                    userBatchTaskFillProblem.setTask(task);
                                    userBatchTaskFillProblemRepository.save(userBatchTaskFillProblem);
                                }
                        );


                    }
                    task.setNotes(submittedTask.getNotes());
                    taskStatusUpdateRepository.saveStatusUpdate(batch, task, submittingUser, oldStatus);
                    statusMap.remove(submittedTask.getId());
                }
                if ( closeOutBatch ) {
                    task.setCurrentBatch(null);
                    // reopen if it's not found or NOS2
                    task.setOpen(task.getStatus() == TaskStatus.New ||
                            task.getStatus() == TaskStatus.NOS);
                }
            });
            if ( closeOutBatch ) {
                batchResponseRepository.deleteByUserTaskBatch(userTaskBatch);
                userTaskBatch.setEndTime(submitDateTime);
            }
            if (statusMap.size() > 0) {
                logger.error("Attempt to save items not in batch: " + userTaskBatch.getId() +
                        "  " + statusMap.size());
            }
            batch.setClosingUser(submittingUser);
            // this save cascades to store all the contained tasks that have been changed.
            userTaskBatch = userTaskBatchRepository.save(userTaskBatch);
        } else {
            throw new UnauthorizedRequestException("Unauthorized submit");
        }
        return userTaskBatch;
    }

    @Override
    @Transactional
    public Task updateSingleTask(User submittingUser,
                                 Task task,
                                 TaskStatusRequest taskStatusRequest) throws UnauthorizedRequestException {

        if (!submittingUser.isAdmin() && !submittingUser.isAssign()) {
            UserTaskBatch userTaskBatch = userTaskBatchRepository.findCurrentBatchByUser(submittingUser);
            if (task == null || userTaskBatch.getTasks().indexOf(task) < 0) {
                throw new UnauthorizedRequestException("Illegal attempt to update call clip batch");
            }
        }
        // if there's a version mismatch, do not update the task, return in current state.
        if (task.getVersion() != taskStatusRequest.getTaskVersion()) {
            return task;
        }
        taskLogRepository.log(task, submittingUser,
                String.format("Manually set status from %s to %s.", task.getStatus(), taskStatusRequest.getStatus()));
        task.setStatus(taskStatusRequest.getStatus());
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public UserTaskBatch assignerCloseTaskBatch(User closingUser, UserTaskBatch batch)
            throws UnauthorizedRequestException{
        UserTaskBatchInfo info = UserTaskBatchInfo.fromTaskBatch(batch);
        if (closingUser.isAdmin() || info.getTaskLocations().stream().anyMatch(
                s -> closingUser.getCircDesks().stream().anyMatch(circulationDesk -> circulationDesk.getName().equals(s))
        )
        ){
            boolean hasResponses = batchResponseRepository.existsByUserTaskBatch( batch );
            if (hasResponses) {
                applyBatchResponsesToBatch(batch, false, true);
                if (batch.getStartTime() == null) {
                    batch.setStartTime(new Timestamp(System.currentTimeMillis()));
                }
                batch.setEndTime(new Timestamp(System.currentTimeMillis()));
            }
            batch.getTasks().stream().forEach((task) -> {
                task.setCurrentBatch(null);
                task.setOpen(task.getStatus() == TaskStatus.NOS || task.getStatus() == TaskStatus.New);
                taskLogRepository.log(task, closingUser, "Removed from completed batch: " + (task.isOpen() ? "Open" : "Closed"));
            });
            if (hasResponses) {
                batch.setClosingUser(closingUser);
                return userTaskBatchRepository.save(batch);
            } else {
                taskRepository.saveAll(batch.getTasks());
                userTaskBatchRepository.delete(batch);
                return batch;
            }
        } else {
            throw new UnauthorizedRequestException("Illegal attempt to close batch");
        }
    }

    @Override
    public List<UserTaskBatch> currentBatches(User user) {
        return userTaskBatchRepository.findOpenBatchesByUser(user);
    }

    @Override
    @Transactional
    public void storeBatchResponse(User loggedInUser, UserTaskBatch userTaskBatch, Task task, BatchResponse response) throws UnauthorizedRequestException {
        if ( task == null || userTaskBatch == null ) {
            logger.error("Attempt to save to a null task or batch");
            return;
        }
        if ( userTaskBatch.getUser().getId() != loggedInUser.getId() ) {
            throw new UnauthorizedRequestException("Attempt to store batch that is not owned by the user");
        }
        BatchResponse oldResponse = batchResponseRepository.findByTask(task);
        if ( oldResponse != null ) batchResponseRepository.delete(oldResponse);
        response.setUserTaskBatch(userTaskBatch);
        response.setTask(task);
        if ( response.getTaskFillProblemList() != null ) {
            response.setTaskFillProblemList(
                    response.getTaskFillProblemList().stream().map((p)-> taskFillProblemRepository.findById(p.getId()).orElse(null)).collect(Collectors.toList())
            );
        }
        batchResponseRepository.save(response);
    }

    @Override
    public boolean hasBatchRepsonses(UserTaskBatch batch) {
        return batchResponseRepository.existsByUserTaskBatch(batch);
    }

    @Override
    public int countBatchResponses(UserTaskBatch batch) {
        return batchResponseRepository.countByUserTaskBatchAndStatusNot(batch, TaskStatus.New);
    }

    @Override
    public UserTaskBatch applyBatchResponsesToBatch(UserTaskBatch userTaskBatch, boolean setNewState, boolean upgradeStatuses) {
        List<BatchResponse> responses = batchResponseRepository.findByUserTaskBatch(userTaskBatch);
        Map<Long, Task> taskMap = new HashMap<>();
        userTaskBatch.getTasks().stream().forEach((cs)->taskMap.put(cs.getId(), cs));
        responses.stream().forEach((response->{
            Task cs = taskMap.get(response.getTask().getId());
            if ( cs != null ) {
                if ( setNewState || response.getStatus() != TaskStatus.New ) {
                    if ( upgradeStatuses && cs.getStatus() == TaskStatus.NOS ) {
                        boolean fos = response.getStatus()== TaskStatus.FOS || response.getStatus()== TaskStatus.FOS_2x;
                        cs.setStatus(fos? TaskStatus.FOS_2x: TaskStatus.NOS_2x);
                    } else {
                        cs.setStatus(response.getStatus());
                    }
                }
                cs.setTaskFillProblems(new ArrayList<>(response.getTaskFillProblemList()));
                cs.setNotes(response.getNotes());
                if ( userTaskBatch.getMostRecentResponse() == null || userTaskBatch.getMostRecentResponse().before(response.getUpdateDateTime())) {
                    userTaskBatch.setMostRecentResponse(response.getUpdateDateTime());
                }
            }
        }));
        return userTaskBatch;
    }

}
