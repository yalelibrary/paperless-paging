package edu.yale.library.paperless.services;

public class UnauthorizedRequestException extends Exception {
    public UnauthorizedRequestException(String s) {
        super(s);
    }

    public UnauthorizedRequestException() {
        super("Unauthorized request");
    }
}
