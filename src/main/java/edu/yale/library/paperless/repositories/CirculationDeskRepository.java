package edu.yale.library.paperless.repositories;
import edu.yale.library.paperless.entities.CirculationDesk;
import edu.yale.library.paperless.entities.Library;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CirculationDeskRepository extends CrudRepository<CirculationDesk, Long> {
    Optional<CirculationDesk> findByLibraryAndCode(Library library, String code);
    Optional<CirculationDesk> findByLibrary_CodeAndCode(String libraryCode, String code);
}
