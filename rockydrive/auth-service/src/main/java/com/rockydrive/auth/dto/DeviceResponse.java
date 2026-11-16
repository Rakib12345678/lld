package com.rockydrive.auth.dto;

import java.time.Instant;

public record DeviceResponse(String deviceId, Instant lastSeenAt) {
}
