package edu.yale.library.paperless.services;

import edu.yale.library.alma.api.client.ApiClientException;
import org.springframework.transaction.annotation.Transactional;

public interface TaskLoader {
    @Transactional
    void loadTasks(String libraryCode, String circDeskCode, boolean reloadDetails) throws ApiClientException;

    @Transactional
    void loadAllTasks(boolean reloadDetails) throws ApiClientException;

    @Transactional
    void clearOldClosedTasks();
}
