package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.Barcode;
import edu.yale.library.paperless.entities.Task;
import edu.yale.library.paperless.entities.TaskLog;
import edu.yale.library.paperless.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BarcodeRepository extends CrudRepository<Barcode, Long> {
    List<Barcode> findByTask(Task task);

    List<Barcode> findByBarcode(String barcode);
}

