package edu.yale.library.paperless.config;

import edu.yale.library.alma.api.client.ApiClientException;
import edu.yale.library.paperless.services.ConfigurationLoader;
import edu.yale.library.paperless.services.DataLoadManager;
import edu.yale.library.paperless.services.TaskLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class LoadDataScheduler {

    private final DataLoadManager dataLoadManager;

    @Scheduled( fixedDelay = 300000)
    @Profile("!test")
    public void refreshTasks() {
        try {
            dataLoadManager.loadAllTasks(false);
        } catch (ApiClientException e) {
            log.error("Error loading tasks on schedule", e);
        }
    }

    @Scheduled( cron = "0 6,22 * * * *")
    @Profile("!test")
    public void refreshConfiguration() {
        try {
            dataLoadManager.loadConfiguration();
        } catch (ApiClientException e) {
            log.error("Error loading configuration on schedule", e);
        }
    }
}
