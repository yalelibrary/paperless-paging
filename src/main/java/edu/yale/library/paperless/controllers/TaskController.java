package edu.yale.library.paperless.controllers;

import edu.yale.library.alma.api.client.ApiClientException;
import edu.yale.library.paperless.report.UserBatchEmailGenerator;
import edu.yale.library.paperless.services.*;
import edu.yale.library.paperless.entities.*;
import edu.yale.library.paperless.repositories.TaskLogRepository;
import edu.yale.library.paperless.repositories.TaskRepository;
import edu.yale.library.paperless.repositories.UserTaskBatchRepository;
import jakarta.mail.MessagingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DataLoadManager dataLoadManager;
    private final TaskRepository taskRepository;
    private final UserTaskBatchRepository userTaskBatchRepository;
    private final UserService userService;
    private final UserTaskBatchService userTaskBatchService;
    private final TaskLogRepository taskLogRepository;
    private final UserBatchEmailGenerator userBatchEmailGenerator;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;


    @RolesAllowed({"ASSIGN", "TASK"})
    @RequestMapping(method = RequestMethod.GET, value = "/users/{user-id}/task-batches/open")
    public List<UserTaskBatch> openTaskBatches(@PathVariable("user-id") long userId) throws UnauthorizedRequestException {
        User user = userService.lookupLoggedInUser();
        if (user.isAssign() || user.getId() == userId) {
            List<UserTaskBatch> userTaskBatches = userTaskBatchRepository.findOpenBatchesByUser(user);
            return userTaskBatches;
        } else {
            throw new UnauthorizedRequestException();
        }
    }

    @RolesAllowed({"ASSIGN", "TASK"})
    @RequestMapping(method = RequestMethod.GET, value = "/users/{user-id}/task-batches/current")
    public UserTaskBatch currentTaskBatch(@PathVariable("user-id") long userId) throws
            UnauthorizedRequestException {
        User user = userService.lookupLoggedInUser();
        if (user.isAssign() || user.getId() == userId) {
            UserTaskBatch userTaskBatch = userTaskBatchRepository.findCurrentBatchByUser(userService.findById(userId));
            if ( userTaskBatch != null ) {
                if ( user.getId() == userId && userTaskBatch.getStartTime() == null ) {
                    userTaskBatch.setStartTime(new Timestamp(System.currentTimeMillis()));
                    try {
                        userTaskBatchRepository.save(userTaskBatch);
                    } catch (StaleObjectStateException e) {
                        // this is ok
                    }
                }
                userTaskBatch.getTasks().stream().forEach(
                        task -> {
                            task.setIncomingStatus(task.getStatus());
                            task.setStatus(TaskStatus.New);
                        });
                userTaskBatchService.applyBatchResponsesToBatch(userTaskBatch, true, false);
                // Detatch so updates don't go to the database.  These updates are just for the client.
                entityManager.detach(userTaskBatch);
            }
            return userTaskBatch;
        } else {
            throw new UnauthorizedRequestException();
        }
    }

    @RolesAllowed({"ASSIGN"})
    @RequestMapping(method = RequestMethod.PUT, value = "/users/{user-id}/task-batches")
    public UserTaskBatch submitNewTaskBatch(@RequestBody TaskList tasks,
                                            @PathVariable("user-id") long userId)
            throws UnauthorizedRequestException {
        User user = userService.findById(userId);
        return userTaskBatchService.createNewBatchForUser(userService.lookupLoggedInUser(), user, tasks);
    }

    @RolesAllowed({"ASSIGN"})
    @RequestMapping(method = RequestMethod.GET, value = "/task-batches/{task-batch-id}/assigner-close")
    public UserTaskBatch submitBatchByAssigner(@PathVariable("task-batch-id") long batchId)
            throws UnauthorizedRequestException {
        User loggedInUser = userService.lookupLoggedInUser();
        UserTaskBatch batch = userTaskBatchRepository.findById(batchId).orElse(null);
        if ( batch != null ) {
            return userTaskBatchService.assignerCloseTaskBatch(loggedInUser, batch);
        } else {
            return null;
        }
    }

    @RolesAllowed({"ASSIGN", "ADMIN"})
    @RequestMapping(method = RequestMethod.GET, value = "/tasks")
    public Iterable<Task> allTasks() throws UnauthorizedRequestException {
        return taskRepository.findAll();
    }


    @RolesAllowed({"ASSIGN", "ADMIN"})
    @RequestMapping(method = RequestMethod.GET, value = "/task-batch-infos")
    public Iterable<UserTaskBatchInfo> allBatchesInfo() throws UnauthorizedRequestException {
        User loggedInUser = userService.lookupLoggedInUser();
        List<UserTaskBatchInfo> ret = StreamSupport.stream(userTaskBatchRepository.findByEndTimeIsNull().spliterator(), false)
                // pre filter based on user's locations
                .filter(batch1 -> batch1.getUser().getCircDesks().stream().anyMatch(
                        library -> loggedInUser.getCircDesks().stream().anyMatch(circulationDesk -> circulationDesk.getId() == library.getId())
                    )
                )
                .map(
                (batch)->{
                    batch.getTasks().size(); // trigger lazy loading before detach
                    int responseCount = userTaskBatchService.countBatchResponses(batch);
                    entityManager.detach(batch);
                    UserTaskBatchInfo info = UserTaskBatchInfo.fromTaskBatch(
                        userTaskBatchService.applyBatchResponsesToBatch(batch,false, false)
                    );
                    info.setCancellable(responseCount==0);
                    info.setResponseCount(responseCount);
                    return info;
                }).filter(userTaskBatchInfo ->  // filter after based on locations in actual batch
            userTaskBatchInfo.getTaskLocations().stream()
                    .anyMatch(csLocation ->
                            loggedInUser.getCircDesks().stream()
                                    .anyMatch(circulationDesk -> circulationDesk.getName().equals(csLocation)))
        ).sorted(Comparator.comparing(o -> o.getUserSort()))
                .collect(Collectors.toList());
        return ret;
    }

    @RolesAllowed({"ASSIGN", "ADMIN"})
    @RequestMapping(method = RequestMethod.GET, value = "/tasks/open")
    public Iterable<Task> allOpenTasks() throws UnauthorizedRequestException {
        return taskRepository.findAllByOpenTrueOrderByCallNumber();
    }


    @RolesAllowed({"ASSIGN", "ADMIN"})
    @RequestMapping(method = RequestMethod.GET, value = "/tasks/unassigned")
    public TasksList allOpenAndUnassignedTasks(@RequestParam(required = false) Boolean reload) throws UnauthorizedRequestException, ApiClientException {
        if (reload != null && reload) {
            dataLoadManager.loadAllTasks(false);
        }
        TasksList unassignedTasks = new TasksList();
        unassignedTasks.setTasks(taskRepository.findAllByOpenTrueAndCurrentBatchIsNullOrderByCallNumber());
        unassignedTasks.setAge(dataLoadManager.secondsSinceLastChange());
        return unassignedTasks;
    }


    @RolesAllowed({"ASSIGN", "TASK"})
    @RequestMapping(method = RequestMethod.PUT, value = "/tasks/{task-id}/status")
    public Task putTaskStatus(@PathVariable("task-id") long taskId,
                                  @RequestBody TaskStatusRequest taskStatusRequestObject) throws UnauthorizedRequestException {
        User user = userService.lookupLoggedInUser();
        Task task = taskRepository.findById(taskId).orElse(null);
        return userTaskBatchService.updateSingleTask(user, task, taskStatusRequestObject);
    }

    @RolesAllowed({"ASSIGN", "TASK"})
    @RequestMapping(method = RequestMethod.GET, value = "/task-batches/{batch-id}")
    public UserTaskBatch getTaskBatch(@PathVariable("batch-id") long batchId)
            throws UnauthorizedRequestException {
        UserTaskBatch userTaskBatch = userTaskBatchRepository.findById(batchId).orElse(null);
        if ( userTaskBatch == null ) return null;
        User loggedInUser = userService.lookupLoggedInUser();
        if (loggedInUser.isAssign() ||
                userTaskBatch.getUser().getId().equals(loggedInUser.getId())) {
            return userTaskBatch;
        } else {
            return null;
        }
    }

    @RolesAllowed({"TASK"})
    @RequestMapping(method = RequestMethod.PUT, value = "/task-batches/{batch-id}")
    public UserTaskBatch submitUpdatesToTaskBatch(@RequestBody TaskList tasks,
                                                      @PathVariable("batch-id") long batchId)
            throws UnauthorizedRequestException {
        UserTaskBatch userTaskBatch = userTaskBatchRepository.findById(batchId).orElse(null);
        if ( userTaskBatch == null ) {
            throw new UnauthorizedRequestException("Unable to find the Task batch.");
        }
        User loggedInUser = userService.lookupLoggedInUser();
        userTaskBatchService.submitTaskBatch(loggedInUser, userTaskBatch, tasks, true);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        new Thread(() -> transactionTemplate.executeWithoutResult(transactionStatus -> {
            userTaskBatchRepository.findById(batchId).ifPresent(userTaskBatch1 -> {
                try {
                    userBatchEmailGenerator.sendSubmitEmails(userTaskBatch1);
                } catch (MessagingException e) {
                    logger.error("Error sending email", e);
                }
            });
        })).start();
        return userTaskBatch;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/task-status-list")
    public List<TaskStatusValue> taskStatusList() throws UnauthorizedRequestException {
        TaskStatus statuses[] = TaskStatus.values();
        List<TaskStatus> ret = Arrays.asList(statuses);
        return ret.stream().map(taskStatus -> {
            TaskStatusValue r = new TaskStatusValue();
            r.code = taskStatus.getCode();
            r.description = taskStatus.getDescription();
            r.searchIndex = taskStatus.getSearchIndex();
            r.value = taskStatus.toString();
            return r;
        }).collect(Collectors.toList());
    }

    @RolesAllowed({"TASK"})
    @RequestMapping(method = RequestMethod.PUT, value = "/task-batches/{batch-id}/task/{task-id}/batch-response")
    public UserTaskBatch submitUpdatesToTaskpBatch(@RequestBody BatchResponse response,
                                                      @PathVariable("batch-id") long batchId,
                                                      @PathVariable("task-id") long taskId)
            throws UnauthorizedRequestException {
        UserTaskBatch userTaskBatch = userTaskBatchRepository.findById(batchId).orElse(null);
        Task task = taskRepository.findById(taskId).orElse(null);
        User loggedInUser = userService.lookupLoggedInUser();
        userTaskBatchService.storeBatchResponse(loggedInUser, userTaskBatch, task, response);
        return userTaskBatch;
    }

    @Getter
    @Setter
    public static class TaskStatusValue {
        private String value;
        private int searchIndex;
        private String description;
        private String code;
    }

    @Getter
    @Setter
    public static class TasksList {
        private Iterable<Task> tasks;
        private long age;
    }

}
