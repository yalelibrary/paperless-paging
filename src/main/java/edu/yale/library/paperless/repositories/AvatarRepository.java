package edu.yale.library.paperless.repositories;

import edu.yale.library.paperless.entities.Avatar;
import edu.yale.library.paperless.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface AvatarRepository extends CrudRepository<Avatar, Long> {
    void deleteByUserEquals(User user);
    Avatar findByUser(User user);
}
