package com.chineselearning.contentservice.security;

public record CurrentUser(Long id, String role)
{
    public boolean isAdmin()
    {
        return "ADMIN".equals(role);
    }

    public boolean canEditContent()
    {
        return isAdmin() || "TEACHER".equals(role);
    }
}