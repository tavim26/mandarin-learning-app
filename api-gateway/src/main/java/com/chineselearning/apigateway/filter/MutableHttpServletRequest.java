package com.chineselearning.apigateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

/**
 * Request wrapper that controls the identity headers (X-User-*) forwarded to downstream services.
 * HTTP header names are case-insensitive, so every lookup here is case-insensitive as well:
 * X-User-* headers sent by the client are hidden whatever their spelling, and only the values
 * set by the gateway through {@link #putHeader} are forwarded.
 */
public class MutableHttpServletRequest extends HttpServletRequestWrapper
{

    private static final String IDENTITY_HEADER_PREFIX = "x-user-";

    private final Map<String, String> customHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public MutableHttpServletRequest(HttpServletRequest request)
    {
        super(request);
    }

    public void putHeader(String name, String value)
    {
        customHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name)
    {
        if (customHeaders.containsKey(name))
        {
            return customHeaders.get(name);
        }
        if (isIdentityHeader(name))
        {
            return null;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name)
    {
        if (customHeaders.containsKey(name))
        {
            return Collections.enumeration(List.of(customHeaders.get(name)));
        }
        if (isIdentityHeader(name))
        {
            return Collections.emptyEnumeration();
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames()
    {
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        names.addAll(customHeaders.keySet());

        Enumeration<String> original = super.getHeaderNames();
        while (original.hasMoreElements())
        {
            String name = original.nextElement();
            if (!isIdentityHeader(name))
            {
                names.add(name);
            }
        }
        return Collections.enumeration(names);
    }

    private static boolean isIdentityHeader(String name)
    {
        return name != null && name.toLowerCase(Locale.ROOT).startsWith(IDENTITY_HEADER_PREFIX);
    }
}