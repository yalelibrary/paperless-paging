package edu.yale.library.paperless.controllers;

import edu.yale.library.paperless.services.UnauthorizedRequestException;
import edu.yale.library.paperless.entities.TaskFillProblem;
import edu.yale.library.paperless.entities.TaskProblem;
import edu.yale.library.paperless.repositories.TaskFillProblemRepository;
import edu.yale.library.paperless.repositories.TaskProblemRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProblemController {

    TaskProblemRepository problemRepository;
    TaskFillProblemRepository problemFillRepository;

    public ProblemController(TaskProblemRepository problemRepository,
                             TaskFillProblemRepository problemFillRepository) {
        this.problemRepository = problemRepository;
        this.problemFillRepository = problemFillRepository;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/task-fill-problems")
    public List<TaskFillProblem> listTaskFillProblems() throws UnauthorizedRequestException {
        List<TaskFillProblem> ret = problemFillRepository.findAll();
        return ret;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/task-problems")
    public Iterable<TaskProblem> listTaskProblems() throws UnauthorizedRequestException {
        return problemRepository.findAll();
    }
}
