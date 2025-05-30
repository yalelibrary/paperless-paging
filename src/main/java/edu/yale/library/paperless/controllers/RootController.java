package edu.yale.library.paperless.controllers;

import edu.yale.library.paperless.entities.User;
import edu.yale.library.paperless.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
//
@Controller
@RequiredArgsConstructor
public class RootController {

    private final UserService userService;
    @RequestMapping({"/"})
    public String getLanding(HttpServletRequest request) {
        User user = userService.lookupLoggedInUser();
        if (user != null) {
            if (user.isAdmin()) {
                return "redirect:/assign";
            } else if (user.isAssign()) {
                return "redirect:/assign";
            } else {
                return "redirect:/tasks";
            }
        }
        else return "index.html";
    }

    @GetMapping(value = "/health-check", produces = "text/plain")
    public ResponseEntity<?> healthCheck() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }


    @RequestMapping(value = { "/assign", "/admin","/tasks", "/barcode", "/mmsid","/barcode/", "/mmsid/" })
    public String getIndex() {
        return "/index.html";
    }
    @RequestMapping(value = { "/barcode/{id}", "/mmsid/{id}"  })
    public String getIndex( @PathVariable String id) {
        return "/index.html";
    }
}
