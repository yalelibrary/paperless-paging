package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.Location;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends CrudRepository<Location, Long> {

    Optional<Location> findByLibrary_CodeAndCode(String libraryCode, String locationCode);
    boolean existsByLibrary_CodeAndCode(String libraryCode, String locationCode);
}
