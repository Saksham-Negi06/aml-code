package com.aml.config.security;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    
    public String generateToken(String username, String role) {
        return "mock-token-for-" + username;
    }

    public boolean validateToken(String token) {
        return token != null && token.startsWith("mock-token-for-");
    }

    public String getUsernameFromToken(String token) {
        if (token != null && token.startsWith("mock-token-for-")) {
            return token.substring("mock-token-for-".length());
        }
        return "anonymous";
    }
}
