package edu.yale.library.paperless.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.yale.library.paperless.entities.CirculationDesk;
import edu.yale.library.paperless.entities.User;
import edu.yale.library.paperless.repositories.CirculationDeskRepository;
import edu.yale.library.paperless.services.UnauthorizedRequestException;
import edu.yale.library.paperless.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static edu.yale.library.paperless.services.StringHelper.isBlank;


@RequiredArgsConstructor
@Log4j2
public class CognitoFilter extends GenericFilterBean {

    private final UserService userService;
    private final CirculationDeskRepository circulationDeskRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.getContext();
        if(context.getAuthentication() != null) {
            Object principal = context.getAuthentication().getPrincipal();
            if (principal instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User)principal;
                if (oauth2User.getAttribute("identities") != null) {
                    Map<String, String> identities = (Map<String, String>) ((List) oauth2User.getAttribute("identities")).get(0);
                    String userId = identities.get("userId");
                    String provider = identities.get("providerName");
                    String email = oauth2User.getAttribute("email");
                    String name = oauth2User.getAttribute("name");
                    String familyName = oauth2User.getAttribute("family_name");
                    String sub = oauth2User.getAttribute("sub");
                    List<String> groups = oauth2User.getAttribute("cognito:groups");
                    String username = oauth2User.getAttribute("cognito:username");
                    if (provider.equals("Yale-University-Login")) {
                        User user = userService.findByUsername(userId);
                        if (user == null) {
                            if (provider.equals("Yale-University-Login")) { //groups.contains("paperless-paging-assigner")
                                final User newUser = new User();
                                newUser.setNetId(userId);
                                if (isBlank(name)) {
                                    name = userId;
                                }
                                if (isBlank(familyName)) {
                                    familyName = userId;
                                }
                                newUser.setFirstName(name);
                                newUser.setLastName(familyName);
                                newUser.setEmail(email);
                                newUser.setRetrieve(true);
                                newUser.setEnabled(true);
                                if (groups.contains("paperless-paging-admin") || userService.getAdminUsers().contains(userId)) {
                                    newUser.setAssign(true);
                                    newUser.setAdmin(true);
                                    for (CirculationDesk circulationDesk : circulationDeskRepository.findAll()) {
                                        newUser.getCircDesks().add(circulationDesk);
                                    }
                                }
                                try {
                                    user = userService.updateOrCreate(newUser);
                                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                                    context.setAuthentication(authentication);
                                } catch (UnauthorizedRequestException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                if (groups.contains("paperless-paging-admin") || userService.getAdminUsers().contains(userId)) {
                                    if (!user.isAssign() || !user.isAdmin()) {
                                        user.setAssign(true);
                                        user.setAdmin(true);
                                        userService.save(user);
                                    }
                                }
                            }
                        } else {
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                            context.setAuthentication(authentication);
                        }
                    } else {
                        log.info("Non Yale User Login Attempt: " + username + ", " + sub);
                        context.setAuthentication(null);
                    }
                } else {
                    log.info("No Identities");
                    context.setAuthentication(null);
                }
            }
        }
        chain.doFilter(request, response);
    }

}