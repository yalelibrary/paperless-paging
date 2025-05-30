package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.Task;
import edu.yale.library.paperless.entities.TaskStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends PagingAndSortingRepository<Task, Long>, CrudRepository<Task, Long> {

    List<Task> findAllByOpenTrueOrderByCallNumber();

    List<Task> findAllByOpenTrueAndLibraryCodeAndCircDeskCodeOrderByCallNumber(String libraryCode, String circDeskCode);

    List<Task> findAllByPatronClearedFalseAndOpenFalseAndCreateDateTimeBefore(Timestamp timestamp);

    List<Task> findAllByPatronClearedFalseAndOpenFalseAndUpdateDateTimeBefore(Timestamp timestamp);

    List<Task> findAllByOpenTrueAndCurrentBatchIsNullOrderByCallNumber();

    List<Task> findByAlmaBibMmsId(String mmsId);

    Optional<Task> findByAlmaRequestIdAndItemBarcode(String requestId, String barcode);

    boolean existsByAlmaRequestIdAndItemBarcode(String requestId, String barcode);
}
