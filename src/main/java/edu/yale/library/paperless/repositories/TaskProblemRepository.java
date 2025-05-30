package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.TaskProblem;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskProblemRepository extends CrudRepository<TaskProblem, Long> {
    boolean existsByName(String name);
    boolean existsByValue(String value);
    TaskProblem findByValue(String value);
    List<TaskProblem> findAll();
}
