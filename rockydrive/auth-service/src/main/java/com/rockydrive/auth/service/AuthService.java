package com.rockydrive.auth.service;

import com.rockydrive.auth.dto.AuthResponse;
import com.rockydrive.auth.dto.DeviceResponse;
import com.rockydrive.auth.dto.LoginRequest;
import com.rockydrive.auth.dto.RefreshRequest;
import com.rockydrive.auth.dto.RegisterRequest;
import com.rockydrive.auth.exception.EmailAlreadyRegisteredException;
import com.rockydrive.auth.exception.InvalidCredentialsException;
import com.rockydrive.auth.exception.InvalidTokenException;
import com.rockydrive.auth.model.Device;
import com.rockydrive.auth.model.RefreshToken;
import com.rockydrive.auth.model.User;
import com.rockydrive.auth.repository.DeviceRepository;
import com.rockydrive.auth.repository.RefreshTokenRepository;
import com.rockydrive.auth.repository.UserRepository;
import com.rockydrive.common.dto.UserDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        DeviceRepository deviceRepository,
                        PasswordEncoder passwordEncoder,
                        TokenService tokenService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.deviceRepository = deviceRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException(request.email());
        }
        User user = new User(request.email(), passwordEncoder.encode(request.password()), request.displayName());
        userRepository.save(user);
        return issueTokenPair(user, null);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (request.deviceId() != null && !request.deviceId().isBlank()) {
            upsertDevice(user, request.deviceId(), request.fcmToken());
        }
        return issueTokenPair(user, request.deviceId());
    }

    /**
     * Rotates a refresh token: the presented token is revoked (recording what it was replaced
     * by) and a brand-new access/refresh pair is issued. A presented token that is already
     * revoked, expired, or unknown is rejected outright - reuse of a revoked token is a signal
     * it may have been stolen.
     */
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String presentedHash = tokenService.hashToken(request.refreshToken());
        RefreshToken existing = refreshTokenRepository.findByTokenHash(presentedHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid"));

        if (!existing.isUsable()) {
            throw new InvalidTokenException("Refresh token has been revoked or expired");
        }

        User user = existing.getUser();
        if (request.deviceId() != null && !request.deviceId().isBlank()) {
            upsertDevice(user, request.deviceId(), request.fcmToken());
        }

        AuthResponse response = issueTokenPair(user, request.deviceId() != null ? request.deviceId() : existing.getDeviceId());
        existing.revoke(tokenService.hashToken(response.refreshToken()));
        return response;
    }

    @Transactional
    public DeviceResponse registerDevice(java.util.UUID userId, String deviceId, String fcmToken) {
        User user = userRepository.getReferenceById(userId);
        Device device = upsertDevice(user, deviceId, fcmToken);
        return new DeviceResponse(device.getDeviceId(), device.getLastSeenAt());
    }

    private Device upsertDevice(User user, String deviceId, String fcmToken) {
        Device device = deviceRepository.findByUserAndDeviceId(user, deviceId)
                .map(existing -> {
                    existing.touch(fcmToken);
                    return existing;
                })
                .orElseGet(() -> new Device(user, deviceId, fcmToken));
        return deviceRepository.save(device);
    }

    private AuthResponse issueTokenPair(User user, String deviceId) {
        String accessToken = tokenService.generateAccessToken(user);
        String rawRefreshToken = tokenService.generateRawRefreshToken();

        Instant now = Instant.now();
        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenService.hashToken(rawRefreshToken),
                deviceId,
                now,
                now.plus(tokenService.refreshTokenTtl())
        );
        refreshTokenRepository.save(refreshToken);

        UserDTO userDTO = new UserDTO(user.getId().toString(), user.getEmail(), user.getDisplayName(), user.getCreatedAt());
        return AuthResponse.of(accessToken, rawRefreshToken, tokenService.accessTokenTtlSeconds(), userDTO);
    }
}
