package edu.yale.library.paperless.entities;

import lombok.Getter;

@Getter
public enum TaskStatus {

    New("Call Slip request", "new", 0),
    FOS("Found on shelf", "fos", 1),
    NOS("Not found on shelf", "nos", 1),
    FOS_2x("Found on shelf, 2nd search", "fos_2x", 2),
    NOS_2x("Not found on shelf, 2nd search", "nos_2x", 2);

    private int searchIndex;
    private String description;
    private String code;

    private TaskStatus(String description, String code, int searchIndex) {
        this.description = description;
        this.code = code;
        this.searchIndex = searchIndex;
    }


}
