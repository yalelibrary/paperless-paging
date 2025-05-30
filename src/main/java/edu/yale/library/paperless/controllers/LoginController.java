package edu.yale.library.paperless.controllers;

import edu.yale.library.paperless.controllers.exceptions.InvalidAuthenticationAttemptException;
import edu.yale.library.paperless.entities.User;
import edu.yale.library.paperless.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class LoginController {

    private final UserService userService;

    @Value("${client.landing.url.tasks:/../tasks}")
    private String tasksLanding;

    @Value("${client.landing.url.assign:/../assign}")
    private String assignLanding;

    @Value("${client.landing.url.admin:/../admin}")
    private String adminLanding;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/check-login", produces = "application/json")
    public ResponseEntity<?> lookupLoggedInUser() {
        User user = userService.lookupLoggedInUser();
        if ( user == null ) {
            return new ResponseEntity<>(new ResponseMessage("Not Logged In"), HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    @GetMapping(value = "/api/entry-point")
    public String entryPoint( ) throws InvalidAuthenticationAttemptException {
        User user = userService.lookupLoggedInUser();
        if ( user.isAdmin() ) {
            return "redirect:" + adminLanding;
        } else if (user.isAssign()) {
            return "redirect:" + assignLanding;
        } else {
            return "redirect:" + tasksLanding;
        }
    }

    @GetMapping(value = "/api/logout")
    public String logout(HttpServletRequest request) throws ServletException {
        return "redirect:/logout";
    }

    @ExceptionHandler(InvalidAuthenticationAttemptException.class)
    ResponseEntity<String> handleAuthenticationError(final InvalidAuthenticationAttemptException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }
}
