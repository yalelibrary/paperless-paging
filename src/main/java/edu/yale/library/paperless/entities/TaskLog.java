package edu.yale.library.paperless.entities;

import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
@Getter
@Setter
@Table(indexes = {@Index(columnList = "itemBarcode"), @Index(columnList = "task_id")})
public class TaskLog extends BaseEntity {

    @ManyToOne
    private Task task;

    @ManyToOne
    private User user;

    private String logInformation;

    private String itemBarcode;

}
