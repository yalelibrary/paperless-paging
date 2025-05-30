package edu.yale.library.paperless.services;

import edu.yale.library.paperless.entities.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserTaskBatchService {
    UserTaskBatch createNewBatchForUser(User assigningUser, User user, TaskList tasks)
            throws UnauthorizedRequestException;

    
    UserTaskBatch submitTaskBatch(User submittingUser,
                                  UserTaskBatch userTaskBatch,
                                  TaskList tasks, boolean closeOutBatch) throws UnauthorizedRequestException;

    Task updateSingleTask(User submittingUser,
                          Task task,
                          TaskStatusRequest taskStatusRequest) throws UnauthorizedRequestException;

    UserTaskBatch assignerCloseTaskBatch(User closingUser, UserTaskBatch batch)
            throws UnauthorizedRequestException;

    List<UserTaskBatch> currentBatches(User user);

    void storeBatchResponse(User loggedInUser, UserTaskBatch userTaskBatch, Task task, BatchResponse response) throws UnauthorizedRequestException;

    boolean hasBatchRepsonses(UserTaskBatch batch);

    int countBatchResponses(UserTaskBatch batch);

    UserTaskBatch applyBatchResponsesToBatch(UserTaskBatch userTaskBatch, boolean setNewState, boolean upgradeStatuses);
}
