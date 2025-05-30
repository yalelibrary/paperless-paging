package edu.yale.library.paperless.services;

import edu.yale.library.alma.api.client.ApiClientException;
import edu.yale.library.paperless.entities.TaskFillProblem;
import edu.yale.library.paperless.entities.TaskProblem;
import edu.yale.library.paperless.repositories.TaskFillProblemRepository;
import edu.yale.library.paperless.repositories.TaskProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataLoadManagerImpl implements DataLoadManager {

    private final static TemporalAmount taskLoadInterval = java.time.Duration.ofSeconds(60);
    private final static TemporalAmount configurationLoadInterval = java.time.Duration.ofDays(1);

    private final TaskLoader taskLoader;

    private final ConfigurationLoader configurationLoader;

    private final TaskProblemRepository taskProblemRepository;

    private final TaskFillProblemRepository taskFillProblemRepository;
    private LocalDateTime lastTaskLoad;

    private LocalDateTime lastConfigurationLoad;

    @Override
    @Transactional
    public synchronized boolean loadAllTasks(boolean reloadDetails) throws ApiClientException {
        if (isTimeToLoadTasks()) {
            taskLoader.loadAllTasks(reloadDetails);
            lastTaskLoad = LocalDateTime.now();
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public synchronized boolean loadConfiguration() throws ApiClientException {
        if (isTimeToLoadConfiguration()) {
            configurationLoader.loadConfiguration();
            lastConfigurationLoad = LocalDateTime.now();
            return true;
        } else {
            return false;
        }
    }


    private static final String[][] fillProblems = {
            {"fip", "Found in place", "Y"},
            {"aip", "Almost In place", "Y"},
            {"foc", "Found on cart (on the correct floor)", "Y"},
            {"srt", "Found in sorting room", "Y"},
    };

    private static final String[][] problems = {
            {"cns","callnumber","Call Number Suppressed"},
            {"cno","callnumber","Call Number On Order"},
            {"cni","callnumber","Call Number In Process"},
            {"cnu","callnumber","Call Number Uncat"},
            {"cnn","callnumber","Call Number Missing"},
            {"hly","callnumber","Call Number is Old Yale class call number"},
            {"cnp","location","Perm Loc smlpres"},
            {"hli","location","Holding Loc is not Item Perm Loc"},
            {"yta","location","NonCirculating Temp Location"},
            {"itl","location","Item is In Temp Location"},
    };

    public void createProblemList() {
        if (!taskProblemRepository.findAll().isEmpty()) return;
        List<TaskProblem> callSlipProblems = Arrays.stream(problems).map( (p)-> {
            TaskProblem problem = new TaskProblem();
            problem.setValue(p[0]);
            problem.setProblemType(p[1]);
            problem.setName(p[2]);
            return problem;
        }).filter(
                (problem) -> {
                    return !taskProblemRepository.existsByValue(problem.getValue());
                }).collect(Collectors.toList());
        taskProblemRepository.saveAll(callSlipProblems);
    }

    public void createFillProblemList() {
        if (!taskFillProblemRepository.findAll().isEmpty()) return;
        List<TaskFillProblem> problems = Arrays.stream(fillProblems).map(
                (s) -> {
                    TaskFillProblem problem = new TaskFillProblem();
                    problem.setName(s[1]);
                    problem.setProblemCode(s[0]);
                    problem.setSecondSearch(s[2].equals("Y"));
                    return problem;
                }).filter(
                (problem) -> {
                    return !taskFillProblemRepository.existsByProblemCode(problem.getProblemCode());
                }
        ).collect(Collectors.toList());
        taskFillProblemRepository.saveAll(problems);
    }

    @Override
    public long secondsSinceLastChange() {
        if (lastTaskLoad == null) {
            return 0;
        } else {
            Duration diff = Duration.between(lastTaskLoad, LocalDateTime.now());
            return diff.toSeconds();
        }
    }

    private boolean isTimeToLoadTasks() {
        return lastTaskLoad == null || LocalDateTime.now().isAfter(lastTaskLoad.plus(taskLoadInterval));
    }

    private boolean isTimeToLoadConfiguration() {
        return lastConfigurationLoad == null || LocalDateTime.now().isAfter(lastConfigurationLoad.plus(configurationLoadInterval));
    }

}
