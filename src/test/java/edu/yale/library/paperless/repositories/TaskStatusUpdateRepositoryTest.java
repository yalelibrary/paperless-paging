package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql("/sql/truncate-config.sql")
@Sql("/sql/truncate-task-data.sql")
class TaskStatusUpdateRepositoryTest {

    @Autowired
    UserTaskBatchRepository userTaskBatchRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userAccountRepository;

    @Autowired
    TaskStatusUpdateRepository taskStatusUpdateRepository;

    @Test
    @Transactional
    void saveStatusUpdate() {
        UserTaskBatch userTaskBatch = new UserTaskBatch();
        userTaskBatchRepository.save(userTaskBatch);
        Task task = new Task();
        task.setStatus(TaskStatus.FOS);
        taskRepository.save(task);
        User user = new User();
        userAccountRepository.save(user);
        taskStatusUpdateRepository.saveStatusUpdate(userTaskBatch, task, user, TaskStatus.NOS);

        assertEquals(1, taskStatusUpdateRepository.count());
        List<TaskStatusUpdate> taskStatusUpdates = taskStatusUpdateRepository.findAllByTask(task);
        assertEquals(1, taskStatusUpdates.size());
        assertEquals(TaskStatus.NOS, taskStatusUpdates.get(0).getOldStatus());
        assertEquals(TaskStatus.FOS, taskStatusUpdates.get(0).getNewStatus());
    }
}