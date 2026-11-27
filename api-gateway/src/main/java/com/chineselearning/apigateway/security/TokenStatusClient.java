package com.chineselearning.apigateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Looks up whether a user's tokens are still valid. Results are cached briefly so the
 * gateway does not call user-service on every request; a revocation therefore takes
 * effect within CACHE_TTL.
 */
@Component
public class TokenStatusClient
{
    private static final Duration CACHE_TTL = Duration.ofSeconds(15);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(2);

    private final RestClient restClient;
    private final ConcurrentHashMap<Long, CachedStatus> cache = new ConcurrentHashMap<>();

    public TokenStatusClient(@Value("${application.user-service.url}") String userServiceUrl)
    {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        this.restClient = RestClient.builder()
                .baseUrl(userServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }


    public Optional<TokenStatus> getStatus(long userId)
    {
        Instant now = Instant.now();
        CachedStatus cached = cache.get(userId);
        if (cached != null && cached.expiresAt().isAfter(now))
        {
            return cached.status();
        }

        Optional<TokenStatus> status = fetch(userId);
        cache.put(userId, new CachedStatus(status, now.plus(CACHE_TTL)));
        return status;
    }

    private Optional<TokenStatus> fetch(long userId)
    {
        try
        {
            TokenStatus body = restClient.get()
                    .uri("/internal/users/{id}/token-status", userId)
                    .retrieve()
                    .body(TokenStatus.class);
            return Optional.ofNullable(body);
        }
        catch (HttpClientErrorException.NotFound e)
        {
            return Optional.empty();
        }
        catch (RestClientException e)
        {
            throw new UserServiceUnavailableException(e);
        }
    }

    private record CachedStatus(Optional<TokenStatus> status, Instant expiresAt) {}
}