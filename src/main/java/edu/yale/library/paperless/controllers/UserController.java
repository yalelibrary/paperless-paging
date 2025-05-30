package edu.yale.library.paperless.controllers;

import edu.yale.library.paperless.entities.User;
import edu.yale.library.paperless.services.UnauthorizedRequestException;
import edu.yale.library.paperless.services.UserTaskBatchServiceImpl;
import edu.yale.library.paperless.services.UserService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api")
public class UserController {

    private UserService userService;
    private UserTaskBatchServiceImpl userTaskBatchService;

    public UserController(UserService userService, UserTaskBatchServiceImpl userTaskBatchService) {
        this.userService = userService;
        this.userTaskBatchService = userTaskBatchService;
    }

    @RolesAllowed("ADMIN")
    @RequestMapping(method = RequestMethod.GET, value = "/users")
    public Iterable<User> listUsers() throws UnauthorizedRequestException {
        return userService.findAll();
    }

    @RolesAllowed("ADMIN")
    @RequestMapping(method = RequestMethod.PUT, value = "/users")
    public User saveUsers(@RequestBody User user) throws UnauthorizedRequestException {
        return userService.updateOrCreate(user);
    }

    @RolesAllowed("ADMIN")
    @RequestMapping(method = RequestMethod.DELETE, value = "/users/{user-id}")
    public boolean deleteUsers(@PathVariable("user-id") long userId) throws UnauthorizedRequestException {
        User user = userService.findById(userId);
        if (userService.deleteUser(user)) {
            return true;
        } else {
            throw new UnauthorizedRequestException("Unable to delete the user!");
        }
    }

    @RolesAllowed("ASSIGN")
    @RequestMapping(method = RequestMethod.GET, value = "/users/assignable")
    public List<User> assignableUsers() throws UnauthorizedRequestException {
        User loggedInUser = userService.lookupLoggedInUser();
        return userService.findAllEnabled().stream().filter(
                (user) ->
                        user.isRetrieve() && user.getCircDesks().stream().anyMatch(loggedInUser.getCircDesks()::contains) &&
                                userTaskBatchService.currentBatches(user).size() == 0
        ).collect(Collectors.toList());
    }

    @RolesAllowed("ASSIGN")
    @RequestMapping(method = RequestMethod.GET, value = "/users/active-batch")
    public List<User> usersWithActiveBatches() throws UnauthorizedRequestException {
        User loggedInUser = userService.lookupLoggedInUser();
        return StreamSupport.stream(userService.findAll().spliterator(), false).filter(
                (user) ->
                        user.isRetrieve() && user.getCircDesks().stream().anyMatch(loggedInUser.getCircDesks()::contains) &&
                                userTaskBatchService.currentBatches(user).size() > 0
        ).collect(Collectors.toList());
    }



    @RolesAllowed("TASK")
    @RequestMapping(method = RequestMethod.GET, value = "/users/who-am-i")
    public User whoAmI() throws UnauthorizedRequestException {
        User loggedInUser = userService.lookupLoggedInUser();
        return loggedInUser;
    }



    @RolesAllowed({"ASSIGN", "TASK", "ADMIN"})
    @RequestMapping(method = RequestMethod.GET, value = "/users/{user-id}/avatar")
    public byte[] userAvatarBinary(@PathVariable("user-id") long userId) throws UnauthorizedRequestException {
        User user = userService.findById(userId);
        return userService.retrieveAvatar(user);
    }

    @RolesAllowed({"ASSIGN", "TASK", "ADMIN"})
    @RequestMapping(method = RequestMethod.GET, value = "/users/{user-id}/avatar/data-url")
    public String userAvatarDataUrl(@PathVariable("user-id") long userId) throws UnauthorizedRequestException {
        User user = userService.findById(userId);
        byte[] data = userService.retrieveAvatar(user);
        if (data == null) {
            return "";
        }
        String encodedString = Base64
                .getEncoder()
                .encodeToString(data);
        String mimeType = "image/jpg";
        try {
            InputStream is = new BufferedInputStream(new ByteArrayInputStream(data));
            mimeType = URLConnection.guessContentTypeFromStream(is);
        } catch (Exception e) {
            //logger
        }
        return "data:" + mimeType + ";base64," + encodedString;
    }

}
