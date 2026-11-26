package com.chineselearning.userservice.config;

import com.chineselearning.userservice.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService)
    {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new GatewayHeadersAuthenticationFilter(), AnonymousAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // public
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // any authenticated user (must come before the {userId} patterns below)
                        .requestMatchers(HttpMethod.GET, "/api/users/me", "/api/users/students/search").authenticated()

                        // the user's own data
                        .requestMatchers(HttpMethod.GET, "/api/users/students/{userId}", "/api/users/teachers/{userId}")
                        .access(selfOrAdmin())
                        .requestMatchers(HttpMethod.PUT,
                                "/api/users/students/{userId}/nickname",
                                "/api/users/teachers/{userId}/title",
                                "/api/users/{userId}/email",
                                "/api/users/{userId}/password")
                        .access(selfOrAdmin())

                        // everything else under /api/users: list, search, create, rename, ban, reset, delete
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        .anyRequest().denyAll()
                );

        return http.build();
    }

    /** Allows the request when the {userId} in the path is the authenticated user, or the user is an admin. */
    private static AuthorizationManager<RequestAuthorizationContext> selfOrAdmin()
    {
        return (authenticationSupplier, context) -> {
            Authentication auth = authenticationSupplier.get();
            if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated())
            {
                return new AuthorizationDecision(false);
            }

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (isAdmin)
            {
                return new AuthorizationDecision(true);
            }

            String requestedId = context.getVariables().get("userId");
            return new AuthorizationDecision(String.valueOf(auth.getPrincipal()).equals(requestedId));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception
    {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }
}