package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.Task;
import edu.yale.library.paperless.entities.TaskStatus;
import edu.yale.library.paperless.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql("/sql/truncate-config.sql")
@Sql("/sql/truncate-task-data.sql")
class TaskLogRepositoryTest {

    @Autowired
    TaskLogRepository taskLogRepository;


    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userAccountRepository;

    @Test
    @Transactional
    void log() {
        Task task = new Task();
        task.setStatus(TaskStatus.FOS);
        taskRepository.save(task);
        User user = new User();
        userAccountRepository.save(user);

        taskLogRepository.log(task, user, "Test Information");

        assertEquals(1, taskLogRepository.count());
    }
}