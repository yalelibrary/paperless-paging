package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.User;
import edu.yale.library.paperless.entities.UserTaskBatch;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserTaskBatchRepository extends CrudRepository<UserTaskBatch, Long> {
    List<UserTaskBatch> findByEndTimeIsNull();

    List<UserTaskBatch> findByUserAndEndTimeIsNullOrderByCreateDateTimeDesc(User user);

    UserTaskBatch findFirstByUserAndEndTimeIsNullOrderByCreateDateTimeDesc(User user);

    boolean existsByAssigningUserOrClosingUserOrUser(User assignUser, User closingUser, User user);

    default UserTaskBatch findCurrentBatchByUser(User user) {
        return findFirstByUserAndEndTimeIsNullOrderByCreateDateTimeDesc(user);
    }

    default List<UserTaskBatch> findOpenBatchesByUser(User user) {
        return findByUserAndEndTimeIsNullOrderByCreateDateTimeDesc(user);
    }

    default boolean batchExistUser(User user) {
        return existsByAssigningUserOrClosingUserOrUser(user, user, user);
    }


}
