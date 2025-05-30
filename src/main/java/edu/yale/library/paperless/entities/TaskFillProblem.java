package edu.yale.library.paperless.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;


/***
 * Lookup
 */
@Entity
@Getter
@Setter
public class TaskFillProblem extends BaseEntity {
    private String name;
    private String problemCode;
    private boolean secondSearch;
}
