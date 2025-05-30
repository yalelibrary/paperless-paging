package edu.yale.library.paperless.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static edu.yale.library.paperless.config.SecurityConfiguration.getAuthorizationManagerRequestMatcherRegistryCustomizer;

@Configuration
@Profile("test")
public class TestSecurityConfiguration {

    public final static String TEST_USERNAME = "user";
    public final static String TEST_PASSWORD = "password";
    public final static String TEST_ADMIN_USERNAME = "admin";
    public final static String TEST_ADMIN_PASSWORD = "adminpw";
    public final static String TEST_LADYBIRD_USERNAME = "ladybird";
    public final static String TEST_LADYBIRD_PASSWORD = "ladybirdpw";
    public final static String TEST_DCS_USERNAME = "dcs";
    public final static String TEST_DCS_PASSWORD = "dcspw";
    public final static String TEST_AVIARY_USERNAME = "aviary";
    public final static String TEST_AVIARY_PASSWORD = "aviarypw";

    @Bean
    @Profile("test")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(getAuthorizationManagerRequestMatcherRegistryCustomizer())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser(TEST_USERNAME)
                .password("{noop}" + TEST_PASSWORD)
                .roles("USER");
        auth
                .inMemoryAuthentication()
                .withUser(TEST_ADMIN_USERNAME)
                .password("{noop}" + TEST_ADMIN_PASSWORD)
                .roles("ADMIN");
        auth
                .inMemoryAuthentication()
                .withUser(TEST_LADYBIRD_USERNAME)
                .password("{noop}" + TEST_LADYBIRD_PASSWORD)
                .roles("VIEWLADYBIRD");
        auth
                .inMemoryAuthentication()
                .withUser(TEST_DCS_USERNAME)
                .password("{noop}" + TEST_DCS_PASSWORD)
                .roles("DCS");
        auth
                .inMemoryAuthentication()
                .withUser(TEST_AVIARY_USERNAME)
                .password("{noop}" + TEST_AVIARY_PASSWORD)
                .roles("AVIARY");


    }

}
