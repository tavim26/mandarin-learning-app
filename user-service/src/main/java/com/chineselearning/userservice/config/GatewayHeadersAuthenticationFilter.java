package com.chineselearning.userservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Builds the authenticated user from the identity headers set by the API gateway
 * after it has validated the JWT.
 */
public class GatewayHeadersAuthenticationFilter extends OncePerRequestFilter
{
    private static final Set<String> ROLES = Set.of("STUDENT", "TEACHER", "ADMIN");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");

        if (userId != null && role != null && ROLES.contains(role))
        {
            try {
                Long id = Long.valueOf(userId);
                var authentication = new UsernamePasswordAuthenticationToken(id, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

            } catch (NumberFormatException ignored) {
                // the authorization rules will reject the request
            }
        }

        chain.doFilter(request, response);
    }
}