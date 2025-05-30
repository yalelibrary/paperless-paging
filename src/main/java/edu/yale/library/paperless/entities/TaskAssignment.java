package edu.yale.library.paperless.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(indexes = {@Index(columnList = "assigner_id"), @Index(columnList = "taskStatus"), @Index(columnList = "task_id")})
public class TaskAssignment extends BaseEntity {
    @ManyToOne
    private Task task;

    @ManyToOne
    private User assigner;

    @ManyToOne
    private User retriever;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

    private Timestamp assignedDateTime;

    private String itemBarcode;
}