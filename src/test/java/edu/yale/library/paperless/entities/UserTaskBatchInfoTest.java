package edu.yale.library.paperless.entities;

import edu.yale.library.paperless.repositories.UserTaskBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Sql("/sql/drop-all.sql")
@Sql("/sql/task-batch.sql")
class UserTaskBatchInfoTest {

    @Autowired
    UserTaskBatchRepository userTaskBatchRepository;
    UserTaskBatchInfo userTaskBatchInfo;

    @BeforeEach
    @Transactional
    void init() {
        UserTaskBatch userTaskBatch = userTaskBatchRepository.findAll().iterator().next();
        userTaskBatch.getTasks().get(1).setIncomingStatus(TaskStatus.NOS);
        userTaskBatchInfo = UserTaskBatchInfo.fromTaskBatch(userTaskBatch);
    }

    @Test
    @Transactional
    void getAssigningUserFirstName() {
        assertEquals("Test1", userTaskBatchInfo.getAssigningUserFirstName());
    }

    @Test
    @Transactional
    void getAssigningUserLastName() {
        assertEquals("Assigner1", userTaskBatchInfo.getAssigningUserLastName());
    }

    @Test
    @Transactional
    void getAssigningUserNetId() {
        assertEquals("ta555", userTaskBatchInfo.getAssigningUserNetId());
    }

    @Test
    @Transactional
    void getAssigningUserId() {
        assertEquals(1, userTaskBatchInfo.getAssigningUserId());
    }

    @Test
    @Transactional
    void getUserFirstName() {
        assertEquals("Test1", userTaskBatchInfo.getUserFirstName());
    }

    @Test
    @Transactional
    void getUserLastName() {
        assertEquals("Retriever1", userTaskBatchInfo.getUserLastName());
    }

    @Test
    @Transactional
    void getUserNetId() {
        assertEquals("tr555", userTaskBatchInfo.getUserNetId());
    }

    @Test
    @Transactional
    void isFirstPass() {
        assertFalse(userTaskBatchInfo.isFirstPass());
    }

    @Test
    @Transactional
    void isSecondPass() {
        assertTrue(userTaskBatchInfo.isSecondPass());
    }

    @Test
    @Transactional
    void getUserSort() {
        assertEquals("RETRIEVER1 TEST1 TR555", userTaskBatchInfo.getUserSort());
    }

    @Test
    @Transactional
    void getUserId() {
        assertEquals(2, userTaskBatchInfo.getUserId());
    }

    @Test
    @Transactional
    void getTaskLocationSort() {
        assertEquals("SML", userTaskBatchInfo.getTaskLocationSort());
    }

}