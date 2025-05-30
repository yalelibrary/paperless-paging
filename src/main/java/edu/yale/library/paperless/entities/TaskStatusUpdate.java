package edu.yale.library.paperless.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;

@Entity
@Getter
@Setter
public class TaskStatusUpdate extends BaseEntity {

    @ManyToOne
    Task task;

    @ManyToOne
    UserTaskBatch userTaskBatch;

    @ManyToOne
    User user;

    @Enumerated(EnumType.STRING)
    TaskStatus oldStatus;

    @Enumerated(EnumType.STRING)
    TaskStatus newStatus;

}
