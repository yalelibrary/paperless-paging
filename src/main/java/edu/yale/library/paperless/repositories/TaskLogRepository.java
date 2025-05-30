package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.Task;
import edu.yale.library.paperless.entities.TaskLog;
import edu.yale.library.paperless.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskLogRepository extends CrudRepository<TaskLog, Long> {

    default TaskLog log(Task task, User user, String information) {
        TaskLog ret = new TaskLog();
        ret.setTask(task);
        ret.setUser(user);
        ret.setLogInformation(information);
        ret.setItemBarcode(task.getItemBarcode());
        ret = save(ret);
        return ret;
    }

    List<TaskLog> findByTask(Task task);

    List<TaskLog> findByTaskOrderByUpdateDateTime(Task task);
}

