package edu.yale.library.paperless.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Table(indexes = {@Index(columnList = "barcode")})
public class Barcode extends BaseEntity {

    private String barcode;

    @ManyToOne
    private Task task;
}
