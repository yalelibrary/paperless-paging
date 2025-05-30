package edu.yale.library.paperless.entities;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;

/**
 * Lookup
 */
@Entity
@Getter
@Setter
public class TaskProblem extends BaseEntity {
    private String name;
    @Column(name="problem_value")
    private String value;
    private String problemType;
}
