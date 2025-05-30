package edu.yale.library.paperless.test;

import edu.yale.library.paperless.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DumpRepository  extends CrudRepository<User, Long> {
    @Query(nativeQuery = true, value = "SCRIPT TO ?1")
    List<String> dumpAllDataToFile(String filename);
}
