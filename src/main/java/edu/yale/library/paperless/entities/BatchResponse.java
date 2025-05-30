package edu.yale.library.paperless.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Table(indexes =  {@Index(columnList = "task_id"), @Index(columnList = "user_task_batch_id")})
public class BatchResponse extends BaseEntity {

    @ManyToOne
    UserTaskBatch userTaskBatch;

    @ManyToOne
    Task task;

    @Enumerated(EnumType.STRING)
    TaskStatus status;

    @ManyToMany
    List<TaskFillProblem> taskFillProblemList;

    @Column(columnDefinition="TEXT")
    String notes;

}
