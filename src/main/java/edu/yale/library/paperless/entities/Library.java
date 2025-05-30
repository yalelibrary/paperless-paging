package edu.yale.library.paperless.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Table(indexes = {@Index(columnList = "name")})
public class Library extends BaseEntity {

    private String code;
    private String name;
    private String almaId;
    private String path;
    private String description;
    private String campus;
    private String campusDescription;

    @ToString.Exclude
    @OneToMany(mappedBy="library")
    private Set<Location> locations = new HashSet<>();

    public Library(String code, String name, String almaId, String path, String description, String campus, String campusDescription) {
        this.code = code;
        this.name = name;
        this.almaId = almaId;
        this.path = path;
        this.description = description;
        this.campus = campus;
        this.campusDescription = campusDescription;
    }
}
