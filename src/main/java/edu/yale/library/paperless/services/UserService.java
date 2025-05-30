package edu.yale.library.paperless.services;

import edu.yale.library.paperless.entities.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    User lookupLoggedInUser();

    long userCount();

    User findByUsername(String username);

    User save(User user);

    Iterable<User> findAll();

    List<User> findAllEnabled();

    User findById(long userId);

    byte[] retrieveAvatar(User user);

    User updateOrCreate(User user) throws UnauthorizedRequestException;

    void saveAll(List<User> users);

    User findByNetId(String netId);

    boolean deleteUser(User user);

    List<String> getAdminUsers();
}
