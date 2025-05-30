package edu.yale.library.paperless.config;

import edu.yale.library.alma.api.client.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ConfigureAlmaClient {

    public ConfigureAlmaClient(@Value("${alma.api.url}") String baseUrl, @Value("${alma.api.token}") String apiKey) {
        log.info("Setting up client: " + baseUrl + " with API KEY");
        ApiClient.configureDefaultApiBaseUrl(baseUrl);
        ApiClient.configureDefaultApiKey(apiKey);
    }

}