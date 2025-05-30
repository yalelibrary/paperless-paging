package edu.yale.library.paperless.config;

import edu.yale.library.paperless.entities.CirculationDesk;
import edu.yale.library.paperless.entities.User;
import edu.yale.library.paperless.repositories.CirculationDeskRepository;
import edu.yale.library.paperless.services.UnauthorizedRequestException;
import edu.yale.library.paperless.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Supplier;

import static edu.yale.library.paperless.config.SecurityConfiguration.getAuthorizationManagerRequestMatcherRegistryCustomizer;
import static edu.yale.library.paperless.services.StringHelper.isBlank;


/*******
 * This is just for demonstration mode.
 *
 */
@Configuration
@RequiredArgsConstructor
@Profile("demo")
public class DemoSecurityConfiguration {

    private final UserService userService;
    private final CirculationDeskRepository circDeskRepository;
    private User demoUser, demoRetrieverUser, demoAdminUser;

    public final static String DEMO_USERNAME = "user";
    public final static String DEMO_PASSWORD = "password";
    public final static String DEMO_ADMIN_USERNAME = "admin";
    public final static String DEMO_ADMIN_PASSWORD = "password";
    public final static String DEMO_RETRIEVE_USERNAME = "retriever";
    public final static String DEMO_RETRIEVE_PASSWORD = "password";

    @Bean
    @Profile("demo")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(getAuthorizationManagerRequestMatcherRegistryCustomizer())
                .formLogin(httpSecurityFormLoginConfigurer -> {
                    httpSecurityFormLoginConfigurer.successHandler((request, response, authentication) -> response.sendRedirect("/api/entry-point"));
                   // Customizer.withDefaults().customize(httpSecurityFormLoginConfigurer);
                })
                .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer.logoutSuccessUrl("/"));
        http.addFilterAfter(new DemoFilter(), SwitchUserFilter.class);
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser(DEMO_USERNAME)
                .password("{noop}" + DEMO_PASSWORD)
                .roles("USER");
        auth
                .inMemoryAuthentication()
                .withUser(DEMO_RETRIEVE_USERNAME)
                .password("{noop}" + DEMO_RETRIEVE_PASSWORD)
                .roles("USER");
        auth
                .inMemoryAuthentication()
                .withUser(DEMO_ADMIN_USERNAME)
                .password("{noop}" + DEMO_ADMIN_PASSWORD)
                .roles("USER");
    }

    class DemoFilter extends GenericFilterBean {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            SecurityContext context = SecurityContextHolder.getContext();
            if(context.getAuthentication() != null) {
                Object principal = context.getAuthentication().getPrincipal();
                if (principal instanceof UserDetails) {
                    prepareDemoUsers();
                    User user = userService.findByUsername(((UserDetails) principal).getUsername());
                    if (user != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        context.setAuthentication(authentication);
                    }
                }
            }
            chain.doFilter(request, response);
        }
    }


    private void prepareDemoUsers() {
        if (demoUser != null) {
            addAllCircDesks(demoUser);
            addAllCircDesks(demoAdminUser);
            addAllCircDesks(demoRetrieverUser);
        } else {
            demoUser = new User();
            demoUser.setUsername(DEMO_USERNAME);
            demoUser.setNetId("lj234");
            demoUser.setFirstName("Lauren");
            demoUser.setLastName("Jones");
            demoUser.setEmail("lj@noreply.com");
            demoUser.setRetrieve(true);
            demoUser.setEnabled(true);
            demoUser.setAssign(true);
            demoUser.setAdmin(false);
            demoUser.setPassword("{noop}" + DEMO_PASSWORD);

            demoRetrieverUser = new User();
            demoRetrieverUser.setUsername(DEMO_RETRIEVE_USERNAME);
            demoRetrieverUser.setNetId("cj234");
            demoRetrieverUser.setFirstName("Cindy");
            demoRetrieverUser.setLastName("Jones");
            demoRetrieverUser.setEmail("cj@noreply.com");
            demoRetrieverUser.setRetrieve(true);
            demoRetrieverUser.setEnabled(true);
            demoRetrieverUser.setAssign(false);
            demoRetrieverUser.setAdmin(false);
            demoRetrieverUser.setPassword("{noop}" + DEMO_RETRIEVE_PASSWORD);

            demoAdminUser = new User();
            demoAdminUser.setUsername(DEMO_ADMIN_USERNAME);
            demoAdminUser.setNetId("jv2342");
            demoAdminUser.setFirstName("Viviana");
            demoAdminUser.setLastName("Jones");
            demoAdminUser.setEmail("vj@noreply.com");
            demoAdminUser.setRetrieve(true);
            demoAdminUser.setEnabled(true);
            demoAdminUser.setAssign(true);
            demoAdminUser.setAdmin(true);
            demoAdminUser.setPassword("{noop}" + DEMO_ADMIN_PASSWORD);

            addAllCircDesks(demoUser);
            addAllCircDesks(demoAdminUser);
            addAllCircDesks(demoRetrieverUser);

            userService.saveAll(Arrays.asList(demoUser, demoAdminUser, demoRetrieverUser));
        }
    }

    private void addAllCircDesks(User demoUser) {
        if (demoUser.getCircDesks().size() < circDeskRepository.count()) {
            demoUser.getCircDesks().clear();
            for (CirculationDesk circulationDesk : circDeskRepository.findAll()) {
                demoUser.getCircDesks().add(circulationDesk);
            }
            userService.save(demoUser);
        }
    }

}
