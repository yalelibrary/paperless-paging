package edu.yale.library.paperless.services;

import edu.yale.library.alma.api.client.ApiClientException;
import edu.yale.library.paperless.repositories.TaskRepository;
import edu.yale.library.paperless.test.AlmaApiMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql("/sql/truncate-config.sql")
class TaskLoaderTest {

    @Autowired
    TaskLoader taskLoader;

    @Autowired
    ConfigurationLoader configurationLoader;

    @Autowired
    TaskRepository taskRepository;

    @BeforeEach
    void setUp() throws ApiClientException {
        AlmaApiMocks.mockTaskApi();
        AlmaApiMocks.mockConfigurationsApi();
        configurationLoader.loadConfiguration();
    }

    @Test
    void loadTasks() throws ApiClientException {
        taskLoader.loadTasks("TESTLITLIBRARY", "DEFAULT-CIRC-DESK", false);

    }

    @Test
    void loadAllTasks() throws ApiClientException {
        taskLoader.loadAllTasks(false);
    }
}