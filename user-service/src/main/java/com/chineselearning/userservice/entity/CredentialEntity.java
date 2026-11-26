package com.chineselearning.userservice.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "credentials")
public class CredentialEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Incremented to invalidate every JWT issued before the change.
    @ColumnDefault("0")
    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @OneToOne(mappedBy = "credential", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private UserEntity user;

    protected CredentialEntity() {}

    public CredentialEntity(String email, String passwordHash, Role role)
    {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public void attachUser(UserEntity user)
    {
        this.user = user;
        user.setCredential(this);
    }

    public void revokeTokens()
    {
        tokenVersion++;
    }

    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getTokenVersion() { return tokenVersion; }

    public UserEntity getUser() { return user; }
}