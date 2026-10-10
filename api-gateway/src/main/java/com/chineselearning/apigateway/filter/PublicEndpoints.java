package com.chineselearning.apigateway.filter;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

/**
 * Endpoints that can be called without a JWT.
 */
final class PublicEndpoints
{

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/register",
            "/api/auth/login"
    );

    // Uploaded lesson materials are loaded by the browser through <img>, <audio> and <video> tags,
    // which cannot send an Authorization header. File names are random UUIDs.
    private static final String PUBLIC_FILES_PREFIX = "/api/content/files/";

    private PublicEndpoints() {}

    static boolean matches(HttpServletRequest request)
    {
        String path = request.getRequestURI();

        if (PUBLIC_PATHS.contains(path))
        {
            return true;
        }

        return "GET".equals(request.getMethod()) && path.startsWith(PUBLIC_FILES_PREFIX);
    }
}