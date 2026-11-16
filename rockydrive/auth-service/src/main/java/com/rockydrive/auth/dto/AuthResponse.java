package com.rockydrive.auth.dto;

import com.rockydrive.common.dto.UserDTO;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String tokenType,
        UserDTO user
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresInSeconds, UserDTO user) {
        return new AuthResponse(accessToken, refreshToken, expiresInSeconds, "Bearer", user);
    }
}
