package edu.yale.library.paperless.controllers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class InvalidAuthenticationAttemptException extends Exception {
    public InvalidAuthenticationAttemptException(String message) {
        super(message);
    }
}
