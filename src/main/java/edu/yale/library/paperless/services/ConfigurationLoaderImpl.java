package edu.yale.library.paperless.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.yale.library.alma.api.client.ApiClient;
import edu.yale.library.alma.api.client.ApiClientException;
import edu.yale.library.alma.api.client.configuration.ConfigurationApi;
import edu.yale.library.paperless.entities.CirculationDesk;
import edu.yale.library.paperless.entities.Library;
import edu.yale.library.paperless.entities.Location;
import edu.yale.library.paperless.repositories.CirculationDeskRepository;
import edu.yale.library.paperless.repositories.LibraryRepository;
import edu.yale.library.paperless.repositories.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class ConfigurationLoaderImpl implements ConfigurationLoader {

    private final CirculationDeskRepository circulationDeskRepository;
    private final LocationRepository locationRepository;
    private final LibraryRepository libraryRepository;
    boolean checkForUnloadedLocations = false;

    public ConfigurationLoaderImpl(CirculationDeskRepository circulationDeskRepository, LocationRepository locationRepository, LibraryRepository libraryRepository) {
        this.circulationDeskRepository = circulationDeskRepository;
        this.locationRepository = locationRepository;
        this.libraryRepository = libraryRepository;
    }

    @Override
    @Transactional
    public void loadConfiguration() throws ApiClientException {
        Set<String> loadedLocations = new HashSet<>();
        ConfigurationApi configurationApi = ApiClient.getInstance().getConfigurationApi();
        for (JsonNode libraryNode : configurationApi.loadLibraries().path("library")) {
            String libraryCode = libraryNode.path("code").asText();
            Library library = libraryRepository.findByCode(libraryCode).orElseGet(() -> {
                String name = libraryNode.path("name").asText();
                String almaId = libraryNode.path("id").asText();
                String path = libraryNode.path("path").asText();
                String description = libraryNode.path("description").asText();
                String campus = libraryNode.path("campus").path("value").asText();
                String campusDescription = libraryNode.path("campus").path("desc").asText();
                Library newLibrary = new Library(libraryCode, name, almaId, path, description, campus, campusDescription);
                return libraryRepository.save(newLibrary);
            });
            JsonNode circDeskNodes = configurationApi.loadCircDesks(libraryCode).path("circ_desk");
            for (JsonNode circDeskNode : circDeskNodes) {
                String circDeskCode = circDeskNode.path("code").asText();
                JsonNode locations = circDeskNode.path("location");
                CirculationDesk circulationDesk = circulationDeskRepository.findByLibraryAndCode(library, circDeskCode).orElseGet(() -> {
                    String circDeskName = circDeskNode.path("name").asText();
                    boolean primary = circDeskNode.path("primary").asBoolean();
                    boolean readingRoomDesk = circDeskNode.path("reading_room_desk").asBoolean();
                    CirculationDesk newCircDesk = new CirculationDesk();
                    newCircDesk.setName(String.format("%s - %s", circDeskName, library.getName()));
                    newCircDesk.setLibrary(library);
                    newCircDesk.setCode(circDeskCode);
                    newCircDesk.setLocations(new ArrayList<>());
                    return circulationDeskRepository.save(newCircDesk);
                });
                for (JsonNode locationNode : locations) {
                    String locationCode = locationNode.path("location_code").asText();
                    Location location = locationRepository.findByLibrary_CodeAndCode(libraryCode, locationCode).orElseGet(() -> {
                        Location newLocation = new Location();
                        String locationName = locationNode.path("location_name").asText();
                        newLocation.setCode(locationCode);
                        newLocation.setLibrary(library);
                        newLocation.setName(locationName);
                        newLocation.setCirculationDesks(new ArrayList<>());
                        return newLocation;
                    });
                    loadedLocations.add(libraryCode + "->" + locationCode);
                    if (!location.getCirculationDesks().contains(circulationDesk)) {
                        location.getCirculationDesks().add(circulationDesk);
                    }
                    locationRepository.save(location);
                    if (!circulationDesk.getLocations().contains(location)) {
                        circulationDesk.getLocations().add(location);
                    }
                }
                circulationDeskRepository.save(circulationDesk);
            }
            if (checkForUnloadedLocations) {
                JsonNode locations = configurationApi.loadLocations(libraryCode).path("location");

                for (JsonNode locationNode : locations) {
                    String locationCode = locationNode.path("code").asText();

                    if (!loadedLocations.contains(libraryCode + "->" + locationCode)) {
                        Location location = locationRepository.findByLibrary_CodeAndCode(libraryCode, locationCode).orElseGet(() -> {
                            Location newLocation = new Location();
                            String locationName = locationNode.path("name").asText();
                            newLocation.setCode(locationCode);
                            newLocation.setLibrary(library);
                            newLocation.setName(locationName);
                            newLocation.setCirculationDesks(new ArrayList<>());
                            return newLocation;
                        });
                        locationRepository.save(location);
                    }
                }
            }
        }
    }
}
