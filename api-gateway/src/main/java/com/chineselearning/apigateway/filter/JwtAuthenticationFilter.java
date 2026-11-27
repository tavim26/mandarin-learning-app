package com.chineselearning.apigateway.filter;

import com.chineselearning.apigateway.security.TokenStatus;
import com.chineselearning.apigateway.security.TokenStatusClient;
import com.chineselearning.apigateway.security.UserServiceUnavailableException;
import com.chineselearning.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter
{
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final TokenStatusClient tokenStatusClient;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenStatusClient tokenStatusClient)
    {
        this.jwtUtil = jwtUtil;
        this.tokenStatusClient = tokenStatusClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {
        if (request.getRequestURI().indexOf('%') >= 0)
        {
            sendError(response, HttpStatus.BAD_REQUEST, "Encoded characters are not allowed in the request path");
            return;
        }

        // Identity headers sent by the client are never trusted: the wrapper hides them.
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);

        if (PublicEndpoints.matches(request))
        {
            filterChain.doFilter(mutableRequest, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX))
        {
            sendError(response, HttpStatus.UNAUTHORIZED, "Missing bearer token");
            return;
        }

        Claims claims;
        try
        {
            // Parse and verify once; the old code verified the signature four times per request.
            claims = jwtUtil.extractAllClaims(authHeader.substring(BEARER_PREFIX.length()));
        }
        catch (JwtException | IllegalArgumentException e)
        {
            log.debug("JWT rejected: {}", e.getMessage());
            sendError(response, HttpStatus.UNAUTHORIZED, "Expired or invalid token");
            return;
        }

        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);
        String email = claims.getSubject();
        Integer tokenVersion = claims.get("ver", Integer.class);

        if (userId == null || role == null || email == null || tokenVersion == null)
        {
            sendError(response, HttpStatus.UNAUTHORIZED, "Incomplete token");
            return;
        }

        Optional<TokenStatus> status;
        try
        {
            status = tokenStatusClient.getStatus(userId);
        }
        catch (UserServiceUnavailableException e)
        {
            log.warn("Token status check failed for user {}", userId, e);
            sendError(response, HttpStatus.SERVICE_UNAVAILABLE, "Authentication service unavailable");
            return;
        }

        if (status.isEmpty() || !status.get().active() || status.get().tokenVersion() != tokenVersion)
        {
            sendError(response, HttpStatus.UNAUTHORIZED, "Token has been revoked");
            return;
        }

        mutableRequest.putHeader("X-User-Id", String.valueOf(userId));
        mutableRequest.putHeader("X-User-Role", role);
        mutableRequest.putHeader("X-User-Email", email);

        filterChain.doFilter(mutableRequest, response);
    }

    private static void sendError(HttpServletResponse response, HttpStatus status, String message) throws IOException
    {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}