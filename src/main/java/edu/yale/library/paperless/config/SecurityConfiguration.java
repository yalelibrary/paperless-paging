package edu.yale.library.paperless.config;

import edu.yale.library.paperless.repositories.CirculationDeskRepository;
import edu.yale.library.paperless.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizationSuccessHandler;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
@Profile("!test & !demo")
public class SecurityConfiguration {

    private final UserService userService;
    private final CirculationDeskRepository circDeskRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           @Value("${spring.security.oauth2.client.registration.cognito.clientId}") String clientId,
                                           @Value("${cognito.logout-uri}") String logoutUri,
                                           @Value("${cognito.logout-redirect-uri}") String logoutRedirect) throws Exception {
        http.csrf(csrf -> csrf.csrfTokenRepository
                                (CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .authorizeHttpRequests(getAuthorizationManagerRequestMatcherRegistryCustomizer())
                .oauth2Login(httpSecurityOAuth2LoginConfigurer -> httpSecurityOAuth2LoginConfigurer.successHandler((request, response, authentication) ->
                        response.sendRedirect("/api/entry-point")))
                .oidcLogout((logout) -> logout
                        .backChannel(Customizer.withDefaults())
                )
                .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer.logoutSuccessHandler(
                        new LogoutHandler(logoutUri, logoutRedirect, clientId)).logoutRequestMatcher(new AntPathRequestMatcher("/logout")));
        http.addFilterAfter(new CognitoFilter(userService, circDeskRepository), SwitchUserFilter.class);
        http.addFilterAfter(new CsrfCookieFilter(), OAuth2LoginAuthenticationFilter.class);
        return http.build();
    }

    public static Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> getAuthorizationManagerRequestMatcherRegistryCustomizer() {
        return authz -> authz.requestMatchers("/")
                .permitAll()
                .requestMatchers("/check-login", "/health-check")
                .permitAll()
                .requestMatchers(request -> request.getMethod().equals("OPTIONS"))
                .permitAll()
                .anyRequest()
                .authenticated();
    }

    // See: https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-spa
    final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
        private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
            this.delegate.handle(request, response, csrfToken);
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
                return super.resolveCsrfTokenValue(request, csrfToken);
            }
            return this.delegate.resolveCsrfTokenValue(request, csrfToken);
        }
    }

    final class CsrfCookieFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
            csrfToken.getToken();
            filterChain.doFilter(request, response);
        }
    }
    final class LogoutHandler extends SimpleUrlLogoutSuccessHandler {
        private final String logoutUrl;
        private final String logoutRedirectUrl;
        private final String clientId;

        public LogoutHandler(String logoutUrl, String logoutRedirectUrl, String clientId) {
            this.logoutUrl = logoutUrl;
            this.logoutRedirectUrl = logoutRedirectUrl;
            this.clientId = clientId;
        }

        @Override
        protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) {
            return UriComponentsBuilder
                    .fromUri(URI.create(logoutUrl))
                    .queryParam("client_id", clientId)
                    .queryParam("logout_uri", logoutRedirectUrl)
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUriString();
        }
    }

}
