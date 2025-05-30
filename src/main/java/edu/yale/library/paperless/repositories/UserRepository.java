package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
    User findByNetId(String netId);
    List<User> findByEnabledIsTrueOrderByLastNameAscFirstNameAscNetId();
    List<User> findAllByOrderByLastNameAscFirstNameAscNetId();
//    List<User> findAllOrderByName();
}
