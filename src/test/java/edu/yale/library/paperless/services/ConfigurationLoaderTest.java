package edu.yale.library.paperless.services;

import edu.yale.library.alma.api.client.ApiClientException;
import edu.yale.library.paperless.repositories.CirculationDeskRepository;
import edu.yale.library.paperless.repositories.LibraryRepository;
import edu.yale.library.paperless.repositories.LocationRepository;
import edu.yale.library.paperless.test.AlmaApiMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql("/sql/truncate-config.sql")
class ConfigurationLoaderTest {

    @Autowired
    LibraryRepository libraryRepository;
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    CirculationDeskRepository circulationDeskRepository;

    @Autowired
    ConfigurationLoader configurationLoader;

    @BeforeEach
    void setUp() {
        AlmaApiMocks.mockConfigurationsApi();
    }

    @Test
    void loadConfiguration() throws ApiClientException {
        configurationLoader.loadConfiguration();
        assertEquals(7, libraryRepository.count());
        assertEquals(755, locationRepository.count());
        assertEquals(12, circulationDeskRepository.count());
    }
}