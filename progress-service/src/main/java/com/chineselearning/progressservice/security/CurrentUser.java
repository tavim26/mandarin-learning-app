package com.chineselearning.progressservice.security;

public record CurrentUser(Long id, String role) {

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    public boolean isTeacher() {
        return "TEACHER".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}