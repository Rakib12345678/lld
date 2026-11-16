package com.rockydrive.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * deviceId/fcmToken are optional - when present, login upserts the device in the same call so
 * clients don't need a second round trip to POST /auth/devices right after logging in.
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        String deviceId,
        String fcmToken
) {
}
