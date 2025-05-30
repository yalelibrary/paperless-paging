package edu.yale.library.paperless.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserAccountTest {

    User user;

    @BeforeEach
    void init() {
        user = new User();
    }

    @Test
    void getAuthorities() {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(0, authorities.size());
        user.setAdmin(false);
        user.setAssign(true);
        user.setRetrieve(true);
        authorities = user.getAuthorities();
        assertEquals(2, authorities.size());
        user.setAdmin(true);
        authorities = user.getAuthorities();
        assertEquals(3, authorities.size());
        for (GrantedAuthority g : authorities) {
            assertNotNull(g.getAuthority());
        }
    }

}