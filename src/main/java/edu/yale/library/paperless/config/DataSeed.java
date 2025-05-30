package edu.yale.library.paperless.config;

import edu.yale.library.paperless.entities.TaskFillProblem;
import edu.yale.library.paperless.entities.TaskProblem;
import edu.yale.library.paperless.repositories.CirculationDeskRepository;
import edu.yale.library.paperless.repositories.TaskFillProblemRepository;
import edu.yale.library.paperless.repositories.TaskProblemRepository;
import edu.yale.library.paperless.services.ConfigurationLoader;
import edu.yale.library.paperless.services.DataLoadManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class DataSeed implements ApplicationRunner {

    private final DataLoadManager dataLoadManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        dataLoadManager.createFillProblemList();
        dataLoadManager.createProblemList();
        dataLoadManager.loadConfiguration();
        dataLoadManager.loadAllTasks(true);
    }
}
