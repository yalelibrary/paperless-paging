package edu.yale.library.paperless.config;

import edu.yale.library.paperless.repositories.CirculationDeskRepository;
import edu.yale.library.paperless.repositories.LibraryRepository;
import edu.yale.library.paperless.repositories.LocationRepository;
import edu.yale.library.paperless.services.ConfigurationLoader;
import edu.yale.library.paperless.services.DataLoadManager;
import edu.yale.library.paperless.services.TaskLoader;
import edu.yale.library.paperless.test.AlmaApiMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql("/sql/truncate-config.sql")
class DataSeedTest {

    DataSeed dataSeed;

    @Autowired
    LibraryRepository libraryRepository;
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    CirculationDeskRepository circulationDeskRepository;

    @Autowired
    ConfigurationLoader configurationLoader;

    @Autowired
    DataLoadManager dataLoadManager;

    @BeforeEach
    void setUp() {
        dataSeed = new DataSeed(dataLoadManager);
        AlmaApiMocks.mockConfigurationsApi();
        AlmaApiMocks.mockTaskApi();
    }

    @Test
    void run() throws Exception {
        dataSeed.run(null);
        assertEquals(7, libraryRepository.count());
        assertEquals(755, locationRepository.count());
        assertEquals(12, circulationDeskRepository.count());
    }
}