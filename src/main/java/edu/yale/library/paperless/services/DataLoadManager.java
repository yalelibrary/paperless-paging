package edu.yale.library.paperless.services;

import edu.yale.library.alma.api.client.ApiClientException;

public interface DataLoadManager {
    boolean loadAllTasks(boolean reloadDetails) throws ApiClientException;

    boolean loadConfiguration() throws ApiClientException;

    void createProblemList();

    void createFillProblemList();

    long secondsSinceLastChange();
}
