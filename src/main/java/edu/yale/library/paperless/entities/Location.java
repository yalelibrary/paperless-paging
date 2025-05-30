package edu.yale.library.paperless.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.util.List;

@Entity
@Getter
@Setter
public class Location extends BaseEntity {

    @JsonIgnore
    @ManyToMany(mappedBy="locations")
    private List<CirculationDesk> circulationDesks;

    @JsonIgnore
    @ManyToOne
    private Library library;

    private String code;

    private String name;

}
