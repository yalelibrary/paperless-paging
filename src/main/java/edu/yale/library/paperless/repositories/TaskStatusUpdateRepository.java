package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.*;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskStatusUpdateRepository extends CrudRepository<TaskStatusUpdate, Long> {

    default TaskStatusUpdate saveStatusUpdate(UserTaskBatch userTaskBatch, Task task, User user, TaskStatus oldStatus ) {
        TaskStatusUpdate ret = new TaskStatusUpdate();
        ret.setTask(task);
        ret.setUser(user);
        ret.setNewStatus(task.getStatus());
        ret.setUserTaskBatch(userTaskBatch);
        ret.setOldStatus(oldStatus);
        ret = save(ret);
        return ret;
    }

    List<TaskStatusUpdate> findAllByTask(Task task);

}

