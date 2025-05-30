package edu.yale.library.paperless.entities;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CirculationDesk extends BaseEntity {

    private String code;
    private String name;

    @ManyToMany
    private List<Location> locations;

    @ManyToOne
    private Library library;

}
