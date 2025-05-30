package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.BatchResponse;
import edu.yale.library.paperless.entities.Task;
import edu.yale.library.paperless.entities.TaskStatus;
import edu.yale.library.paperless.entities.UserTaskBatch;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BatchResponseRepository extends CrudRepository<BatchResponse, Long> {

    BatchResponse findByTask(Task task);

    List<BatchResponse> findByUserTaskBatch(UserTaskBatch userTaskBatch);

    void deleteByUserTaskBatch(UserTaskBatch userTaskBatch);

    boolean existsByUserTaskBatch(UserTaskBatch userTaskBatch);

    int countByUserTaskBatchAndStatusNot(UserTaskBatch userTaskBatch, TaskStatus status);
}
