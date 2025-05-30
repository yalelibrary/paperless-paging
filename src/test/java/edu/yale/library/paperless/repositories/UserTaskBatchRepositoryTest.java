package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql("/sql/truncate-config.sql")
@Sql("/sql/truncate-task-data.sql")
class UserTaskBatchRepositoryTest {

    @Autowired
    UserRepository userAccountRepository;

    @Autowired
    UserTaskBatchRepository userTaskBatchRepository;

    User user;

    @BeforeEach
    void init() {
        user = new User();
        userAccountRepository.save(user);
    }

    @Test
    void findCurrentBatchByUser() {
        userTaskBatchRepository.findCurrentBatchByUser(user);
    }

    @Test
    void findOpenBatchesByUser() {
        userTaskBatchRepository.findOpenBatchesByUser(user);
    }

    @Test
    void batchExistUser() {
        userTaskBatchRepository.batchExistUser(user);
    }
}