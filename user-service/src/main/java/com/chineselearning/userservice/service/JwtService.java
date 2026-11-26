package com.chineselearning.userservice.service;

import com.chineselearning.userservice.entity.CredentialEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService
{
    private final SecretKey signingKey;
    private final Duration expiration;

    public JwtService(@Value("${application.security.jwt.secret-key}") String secretKey,
                      @Value("${application.security.jwt.expiration}") long expirationMillis)
    {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.expiration = Duration.ofMillis(expirationMillis);
    }

    public String generateToken(CredentialEntity credential)
    {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(credential.getEmail())
                .claim("userId", credential.getId())
                .claim("role", credential.getRole().name())
                .claim("ver", credential.getTokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }
}