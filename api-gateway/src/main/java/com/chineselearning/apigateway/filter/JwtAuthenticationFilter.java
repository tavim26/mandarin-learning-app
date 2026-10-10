package com.chineselearning.apigateway.filter;

import com.chineselearning.apigateway.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter
{

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil)
    {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        // Identity headers sent by the client are never trusted: the wrapper hides them,
        // and for authenticated requests they are set again from the validated token.
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);

        if (PublicEndpoints.matches(request))
        {
            filterChain.doFilter(mutableRequest, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            sendUnauthorized(response, "Invalid JWT Token");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token))
        {
            sendUnauthorized(response, "Expired or corrupt JWT TOKEN");
            return;
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        String email = jwtUtil.extractEmail(token);

        if (userId == null || role == null || email == null)
        {
            sendUnauthorized(response, "Incomplete JWT Token");
            return;
        }

        mutableRequest.putHeader("X-User-Id", String.valueOf(userId));
        mutableRequest.putHeader("X-User-Role", role);
        mutableRequest.putHeader("X-User-Email", email);

        filterChain.doFilter(mutableRequest, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException
    {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}