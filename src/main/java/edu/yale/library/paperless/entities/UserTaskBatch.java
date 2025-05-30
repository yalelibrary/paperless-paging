package edu.yale.library.paperless.entities;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class UserTaskBatch extends BaseEntity {

    private Timestamp startTime;
    private Timestamp endTime;

    @Transient
    private Timestamp mostRecentResponse;

    @ManyToOne
    private User user;

    @ManyToOne
    private User assigningUser;

    @ManyToOne
    private User closingUser;

    @ManyToMany
    @Cascade(CascadeType.PERSIST)
    @OrderBy("callNumber")
    private List<Task> tasks = new ArrayList<>();

}
