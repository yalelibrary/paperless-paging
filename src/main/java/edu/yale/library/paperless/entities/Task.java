package edu.yale.library.paperless.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Table(indexes = {
        @Index(columnList = "almaRequestId, itemBarcode", unique = true),
        @Index(columnList = "libraryCode, circDeskCode"),
        @Index(columnList = "itemBarcode"),
        @Index(columnList = "originalItemBarcode"),
        @Index(columnList = "almaBibMmsId"),
        @Index(columnList = "patronCleared"),
        @Index(columnList = "open")})
public class Task extends BaseEntity {

    private String almaRequestId;

    private String requestType;

    private String requestSubType;

    private String libraryCode;

    private String circDeskCode;

    private String almaItemPid;

    private String almaHoldingId;

    private String almaBibMmsId;

    private String callNumber;

    private String callNumberDisplay;

    private String callNumberNormalized;

    private String callNumberType;

    private String holdingLocation;

    private String itemPermLocation;

    private String itemTempLocation;

    private String itemBarcode;

    private String originalItemBarcode;

    private String destination;

    private String locationDisplay;

    private String pickupLocation;

    private String pickupLocationDisplay;

    @Column(length = 6000)
    private String title;

    private String author;

    private String enumeration;

    private String patronRequestDate;

    private String dateProcessed;

    private String patronComment;

    private String patronBarcode;

    private String patronEmail;

    private String requester;

    private String requesterLink;

    private String taskLocation;

    private Timestamp taskStatusDateTime;

    private Timestamp lastDischargeDateTime;

    private String taskStatusOpid;

    private String publicationYear;

    private String publisher;

    private String physicalDescription;

    @Column(columnDefinition = "boolean default false")
    private boolean patronCleared;

    @Transient
    private TaskStatus incomingStatus;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private boolean open;

    @ManyToOne(optional = true)
    @JsonIgnore
    private UserTaskBatch currentBatch;

    @ManyToOne(optional = true)
    private TaskProblem taskProblem;

    @ManyToMany
    private List<TaskFillProblem> taskFillProblems;

    @Column(columnDefinition="TEXT")
    private String notes;

    private String requestedResourceMd5;

    public String getSortTitle() {
        String ret = getTitle().replaceAll("[^\\pL\\pN\\s]", "");
        ret = ret.replaceAll("(?i)^The |^A |^An ", "");
        return ret.toUpperCase();
    }

}
