package com.aml.auth.dto;

public record LoginResponse(String token, String tokenType, long expiresIn, String username, String role) {
}