package edu.yale.library.paperless.repositories;
import edu.yale.library.paperless.entities.Task;
import edu.yale.library.paperless.entities.TaskAssignment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskAssignmentRepository extends CrudRepository<TaskAssignment, Long> {

    List<TaskAssignment> findByTask(Task task);

    List<TaskAssignment> findByTaskOrderByUpdateDateTime(Task task);
}
