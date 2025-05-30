package edu.yale.library.paperless.services;

import edu.yale.library.alma.api.client.ApiClientException;
import org.springframework.transaction.annotation.Transactional;

public interface ConfigurationLoader {
    void loadConfiguration() throws ApiClientException;
}
