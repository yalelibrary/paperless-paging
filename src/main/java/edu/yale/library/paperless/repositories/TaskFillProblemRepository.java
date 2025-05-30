package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.TaskFillProblem;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskFillProblemRepository extends CrudRepository<TaskFillProblem, Long> {
    boolean existsByName(String name);
    boolean existsByProblemCode(String problemCode);
    List<TaskFillProblem> findAll();
}
