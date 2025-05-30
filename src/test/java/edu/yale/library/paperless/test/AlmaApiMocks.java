package edu.yale.library.paperless.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.yale.library.alma.api.client.ApiClient;
import edu.yale.library.alma.api.client.ApiClientException;
import edu.yale.library.alma.api.client.configuration.ConfigurationApi;
import edu.yale.library.alma.api.client.task.TaskApi;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class AlmaApiMocks {

    public static final String fixtureResources = "/fixtures/alma";
    public static final String TEST_API_BASE_URI = "http://test.api.server/path";
    public static final String TEST_API_KEY = "TEST_KEY_2039482384032098423";

    private static final ObjectMapper mapper = new ObjectMapper();


    public static void mockTaskApi() {
        ApiClient.getInstance().withTaskApi(new TaskApi() {
            @Override
            public JsonNode loadTasks(String libraryCode, String circulationDeskCode, int limit, int offset) throws ApiClientException {
                return getResourceJson("/tasks/"+libraryCode+"-"+circulationDeskCode+"-requested-resources.json");
            }
        });
    }

    public static void mockConfigurationsApi() {
        ApiClient.getInstance().withConfigurationApi(
                new ConfigurationApi() {
                    @Override
                    public JsonNode loadLibraries() throws ApiClientException {
                        return getResourceJson("/configuration/libraries.json");
                    }

                    @Override
                    public JsonNode loadLocations(String libraryCode) throws ApiClientException {
                        return getResourceJson("/configuration/" + libraryCode + "-locations.json");
                    }

                    @Override
                    public JsonNode loadCircDesks(String libraryCode) throws ApiClientException {
                        return getResourceJson("/configuration/" + libraryCode + "-circdesks.json");
                    }

                    @Override
                    public JsonNode loadMappingTables() throws ApiClientException {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public JsonNode loadMappingTable(String mappingTableCode) throws ApiClientException {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public JsonNode loadCodeTables() throws ApiClientException {
                        return null;
                    }

                    @Override
                    public JsonNode loadCodeTable(String codeTable) throws ApiClientException {
                        return null;
                    }

                    @Override
                    public JsonNode addLocation(String libraryCode, String locationCode, String locationName, String type) throws ApiClientException {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public JsonNode deleteLocation(String libraryCode, String locationCode) throws ApiClientException {
                        throw new UnsupportedOperationException();
                    }
                });
    }

    public static ResponseActions mockRequest(MockRestServiceServer mockServer, HttpMethod method, MediaType mediaType, String responseBody) throws IOException {
        ResponseActions responseActions = mockServer.expect(method(method));
        responseActions
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(mediaType)
                        .body(responseBody));
        return responseActions;
    }

    public static ResponseActions mockRequest(MockRestServiceServer mockServer, HttpMethod method, MediaType mediaType, Resource path) throws IOException {
        String responseBody = path.getContentAsString(StandardCharsets.UTF_8);
        return mockRequest(mockServer, method, mediaType, responseBody);
    }

    private static JsonNode getResourceJson(String resource) {
        String resourcePath = fixtureResources + resource;
        Resource path = new ClassPathResource(resourcePath);
        try {
            return mapper.readTree(path.getFile());
        } catch (IOException e) {
            try {
                return mapper.readTree("{}");
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
