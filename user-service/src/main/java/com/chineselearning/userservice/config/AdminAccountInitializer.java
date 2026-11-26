package com.chineselearning.userservice.config;

import com.chineselearning.userservice.entity.Role;
import com.chineselearning.userservice.repository.CredentialRepository;
import com.chineselearning.userservice.service.UserService;
import com.chineselearning.userservice.util.Emails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the initial administrator account on startup from ADMIN_EMAIL and ADMIN_PASSWORD,
 * since admin accounts cannot be created through the API.
 */
@Component
public class AdminAccountInitializer implements ApplicationRunner
{
    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final CredentialRepository credentialRepository;
    private final UserService userService;
    private final String adminEmail;
    private final String adminPassword;

    public AdminAccountInitializer(CredentialRepository credentialRepository,
                                   UserService userService,
                                   @Value("${application.admin.email:}") String adminEmail,
                                   @Value("${application.admin.password:}") String adminPassword)
    {
        this.credentialRepository = credentialRepository;
        this.userService = userService;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        if (adminEmail.isBlank() || adminPassword.isBlank())
        {
            log.info("ADMIN_EMAIL / ADMIN_PASSWORD not set, skipping admin account creation");
            return;
        }

        String email = Emails.normalize(adminEmail);
        if (credentialRepository.existsByEmail(email))
        {
            return;
        }

        userService.createAccount(email, adminPassword, "Administrator", Role.ADMIN);
        log.info("Created initial admin account {}", email);
    }
}