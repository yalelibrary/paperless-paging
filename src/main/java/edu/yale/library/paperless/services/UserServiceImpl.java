package edu.yale.library.paperless.services;

//import edu.yale.library.paperless.controllers.exceptions.UnauthorizedRequestException;
import edu.yale.library.paperless.entities.Avatar;
import edu.yale.library.paperless.entities.User;
import edu.yale.library.paperless.repositories.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static edu.yale.library.paperless.services.StringHelper.isBlank;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final CirculationDeskRepository circulationDeskRepository;
    private final AvatarRepository avatarRepository;
    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final UserTaskBatchRepository userTaskBatchRepository;
    @Getter
    @Value("${paperless.admins:}")
    private List<String> adminUsers;

    @Override
    public User lookupLoggedInUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            if (principal.getClass().isAssignableFrom(User.class)) {
                User userFromDb = userRepository.findById(((User) authentication.getPrincipal()).getId()).orElse(null);
                return userFromDb;
            }
        } catch (Exception e) {
            logger.warn("Unable to get logged in user", e);
        }
        return null;
    }

    @Override
    public long userCount() {
        return userRepository.count();
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Iterable<User> findAll() {
        return userRepository.findAllByOrderByLastNameAscFirstNameAscNetId();
    }

    @Override
    public List<User> findAllEnabled() {
        return userRepository.findByEnabledIsTrueOrderByLastNameAscFirstNameAscNetId();
    }

    @Override
    public User findById(long userId) {
        return userRepository.findById(userId).orElse(null);
    }


    @Override
    @Transactional
    public byte[] retrieveAvatar(User user) {
        Avatar avatar = avatarRepository.findByUser(user);
        if (avatar != null) {
            return avatar.getImage();
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public User updateOrCreate(User user) throws UnauthorizedRequestException {
        if (isBlank(user.getFirstName()) || isBlank(user.getNetId()) ||
                isBlank(user.getLastName()))
            throw new UnauthorizedRequestException("Attempt to save user with blank values.");
        user.setNetId(user.getNetId().toLowerCase());
        String avatarStr = user.getAvatar();
        if (user.getId() != null && user.getId() > 0) {
            User savedUser = userRepository.findById(user.getId()).orElse(null);
            savedUser.setEnabled(user.isEnabled());
            savedUser.setAccountNonExpired(user.isEnabled());
            savedUser.setAccountNonLocked(user.isEnabled());
            savedUser.setCredentialsNonExpired(user.isEnabled());
            savedUser.setNetId(user.getNetId());
            savedUser.setFirstName(user.getFirstName());
            savedUser.setLastName(user.getLastName());
            savedUser.setAdmin(user.isAdmin());
            savedUser.setRetrieve(user.isRetrieve());
            savedUser.setAssign(user.isAssign());
            savedUser.setCircDesks(user.getCircDesks());
            user = savedUser;
        } else {
            user.setAccountNonExpired(user.isEnabled());
            user.setAccountNonLocked(user.isEnabled());
            user.setCredentialsNonExpired(user.isEnabled());
        }

        // username is always NetId
        user.setUsername(user.getNetId());
        // load circ desks
        if (user.getCircDesks() != null) {
            user.setCircDesks(user.getCircDesks().stream().map(
                    circulationDesk -> circulationDeskRepository.findById(circulationDesk.getId()).orElse(circulationDesk)).collect(Collectors.toList()));
        }
        user = save(user);

        if (avatarStr != null && avatarStr.length() > 50) {
            Avatar avatar = avatarRepository.findByUser(user);
            if (avatar == null) {
                avatar = new Avatar();
                avatar.setUser(user);
            }
            avatar.setFromDataUrl(avatarStr);
            avatarRepository.save(avatar);
        } else {
            if (user.getId() != null && user.getId() != 0) avatarRepository.deleteByUserEquals(user);
        }

        return user;
    }

    @Override
    public void saveAll(List<User> users) {
        userRepository.saveAll(users);
    }

    @Override
    public User findByNetId(String netId) {
        return userRepository.findByNetId(netId);
    }

    @Override
    @Transactional
    public boolean deleteUser(User user) {
        if (!userTaskBatchRepository.batchExistUser(user)) {
            userRepository.delete(user);
            return true;
        }
        return false;
    }
}
