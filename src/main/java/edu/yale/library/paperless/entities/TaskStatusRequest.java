package edu.yale.library.paperless.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusRequest {
    private long taskVersion;
    private TaskStatus status;
}