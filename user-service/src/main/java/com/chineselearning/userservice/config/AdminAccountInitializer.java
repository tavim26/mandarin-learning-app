package com.chineselearning.userservice.config;

import com.chineselearning.userservice.domain.Credential;
import com.chineselearning.userservice.domain.User;
import com.chineselearning.userservice.domain.dao.ICredentialDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Creates the initial administrator account on startup.
 * Admin accounts cannot be created through public registration, so the first one
 * is configured with the ADMIN_EMAIL and ADMIN_PASSWORD environment variables.
 */
@Component
public class AdminAccountInitializer implements ApplicationRunner
{

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final ICredentialDao credentialDao;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminAccountInitializer(
            ICredentialDao credentialDao,
            PasswordEncoder passwordEncoder,
            @Value("${application.admin.email:}") String adminEmail,
            @Value("${application.admin.password:}") String adminPassword)
    {
        this.credentialDao = credentialDao;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args)
    {
        if (adminEmail.isBlank() || adminPassword.isBlank())
        {
            log.info("ADMIN_EMAIL / ADMIN_PASSWORD not set, skipping admin account creation");
            return;
        }

        if (credentialDao.existsByEmail(adminEmail))
        {
            return;
        }

        Credential credential = new Credential();
        credential.setEmail(adminEmail);
        credential.setPasswordHash(passwordEncoder.encode(adminPassword));
        credential.setRole("ADMIN");
        credential.setCreatedAt(LocalDateTime.now());

        User user = new User();
        user.setFullName("Administrator");
        credential.setUser(user);

        credentialDao.save(credential);
        log.info("Created initial admin account {}", adminEmail);
    }
}