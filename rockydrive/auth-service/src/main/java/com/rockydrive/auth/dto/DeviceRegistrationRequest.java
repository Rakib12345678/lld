package com.rockydrive.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceRegistrationRequest(
        @NotBlank String deviceId,
        @NotBlank String fcmToken
) {
}
