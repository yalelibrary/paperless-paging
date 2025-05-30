package edu.yale.library.paperless.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity(name = "user_account")
public class User extends BaseEntity implements UserDetails {

    @Column(unique=true)
    private String username;

    private String firstName;

    private String lastName;

    private String email;

    @JsonIgnore
    private String password;

    @Column(unique=true)
    private String netId;

    private boolean retrieve;

    private boolean assign;

    private boolean admin;

    @JsonIgnore
    private boolean accountNonExpired;

    @JsonIgnore
    private boolean credentialsNonExpired;

    @JsonIgnore
    private boolean accountNonLocked;

    private boolean enabled;

    @Transient
    private String avatar;

    @ManyToMany
    private List<CirculationDesk> circDesks = new ArrayList<>();

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> ret = new ArrayList<>();
        if ( retrieve ) ret.add((GrantedAuthority) () -> "ROLE_TASK");
        if ( assign ) ret.add((GrantedAuthority) () -> "ROLE_ASSIGN");
        if ( admin ) ret.add((GrantedAuthority) () -> "ROLE_ADMIN");
        return ret;
    }

    @JsonSetter
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @JsonIgnore
    public String getAvatar() {
        return this.avatar;
    }
}