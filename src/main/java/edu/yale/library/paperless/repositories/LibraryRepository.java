package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.Library;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LibraryRepository  extends CrudRepository<Library, Long> {
    Optional<Library> findByCode(String libraryCode);
}
