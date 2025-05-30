package edu.yale.library.paperless.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
@Getter
@Setter
public class UserBatchTaskFillProblem extends BaseEntity {

    @ManyToOne
    private UserTaskBatch userTaskBatch;

    @ManyToOne
    private Task task;

    @ManyToOne
    private TaskFillProblem taskFillProblem;

}
