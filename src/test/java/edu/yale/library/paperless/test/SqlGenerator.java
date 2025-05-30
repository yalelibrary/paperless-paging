package edu.yale.library.paperless.test;

import edu.yale.library.paperless.entities.Task;
import edu.yale.library.paperless.entities.TaskStatus;
import edu.yale.library.paperless.entities.User;
import edu.yale.library.paperless.entities.UserTaskBatch;
import edu.yale.library.paperless.repositories.UserTaskBatchRepository;
import edu.yale.library.paperless.repositories.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Disabled
public class SqlGenerator {


    @Autowired
    UserTaskBatchRepository userTaskBatchRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DumpRepository dumpRepository;

    void enableUser(User user) {
        user.setAccountNonLocked(true);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
    }

    @Test
    void generateUserTaskBatch() {
        User assigningUser1 = new User();
        assigningUser1.setAssign(true);
        assigningUser1.setFirstName("Test1");
        assigningUser1.setLastName("Assigner1");
        assigningUser1.setNetId("ta555");
        enableUser(assigningUser1);
        assigningUser1 = userRepository.save(assigningUser1);

        User retrievingUser = new User();
        retrievingUser.setRetrieve(true);
        retrievingUser.setFirstName("Test1");
        retrievingUser.setLastName("Retriever1");
        retrievingUser.setNetId("tr555");
        enableUser(retrievingUser);
        retrievingUser = userRepository.save(retrievingUser);

        User assigningUser2 = new User();
        assigningUser2.setAssign(true);
        assigningUser2.setFirstName("Test2");
        assigningUser2.setLastName("Assigner2");
        assigningUser2.setNetId("ta556");
        enableUser(assigningUser2);
        assigningUser2 = userRepository.save(assigningUser2);


        Task task1 = new Task();
        task1.setTitle("Task1");
        task1.setStatus(TaskStatus.New);
        task1.setAlmaBibMmsId("mms_id1");
        task1.setAlmaHoldingId("holding_id1");
        task1.setAlmaItemPid("pid1");
        task1.setAuthor("Author1");
        task1.setTaskLocation("sml");
        task1.setCallNumberNormalized("TASK-1");
        task1.setCallNumberDisplay("Task-1");
        task1.setCallNumber("Task-1");
        task1.setItemPermLocation("sml");
        task1.setItemBarcode("barcode1");
        task1.setHoldingLocation("sml");

        Task task2 = new Task();
        task2.setTitle("Task2");
        task2.setStatus(TaskStatus.New);
        task2.setAlmaBibMmsId("mms_id2");
        task2.setAlmaHoldingId("holding_id2");
        task2.setAlmaItemPid("pid2");
        task2.setAuthor("Author2");
        task2.setTaskLocation("sml");
        task2.setCallNumberNormalized("TASK-2");
        task2.setCallNumberDisplay("Task-2");
        task2.setCallNumber("Task-2");
        task2.setItemPermLocation("sml");
        task2.setItemBarcode("barcode2");
        task2.setHoldingLocation("sml");

        Task task3 = new Task();
        task3.setTitle("Task3");
        task3.setStatus(TaskStatus.New);
        task3.setAlmaBibMmsId("mms_id3");
        task3.setAlmaHoldingId("holding_id3");
        task3.setAlmaItemPid("pid3");
        task3.setAuthor("Author3");
        task3.setTaskLocation("sml");
        task3.setCallNumberNormalized("TASK-3");
        task3.setCallNumberDisplay("Task-3");
        task3.setCallNumber("Task-3");
        task3.setItemPermLocation("sml");
        task3.setItemBarcode("barcode3");
        task3.setHoldingLocation("sml");
        List<Task> tasks = Arrays.asList(task1, task2, task3);

        UserTaskBatch userTaskBatch = new UserTaskBatch();
        userTaskBatch.setAssigningUser(assigningUser1);
        userTaskBatch.setUser(retrievingUser);
        userTaskBatch.setClosingUser(assigningUser2);
        userTaskBatch.setTasks(tasks);

        task1.setCurrentBatch(userTaskBatch);
        task2.setCurrentBatch(userTaskBatch);
        task3.setCurrentBatch(userTaskBatch);

        userTaskBatchRepository.save(userTaskBatch);
        for (String r:dumpRepository.dumpAllDataToFile("src/test/resources/sql/task-batch.sql")) {
            System.out.println(r);
        }

    }
}
