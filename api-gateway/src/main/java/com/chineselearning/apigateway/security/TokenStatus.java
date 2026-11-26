package com.chineselearning.apigateway.security;

public record TokenStatus(boolean active, int tokenVersion) {}