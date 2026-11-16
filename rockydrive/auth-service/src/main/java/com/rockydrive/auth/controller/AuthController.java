package com.rockydrive.auth.controller;

import com.rockydrive.auth.dto.AuthResponse;
import com.rockydrive.auth.dto.DeviceRegistrationRequest;
import com.rockydrive.auth.dto.DeviceResponse;
import com.rockydrive.auth.dto.LoginRequest;
import com.rockydrive.auth.dto.RefreshRequest;
import com.rockydrive.auth.dto.RegisterRequest;
import com.rockydrive.auth.security.JwtAuthFilter;
import com.rockydrive.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    /** Protected by {@link JwtAuthFilter}; the caller's id is read off the request attribute it sets. */
    @PostMapping("/devices")
    public DeviceResponse registerDevice(@Valid @RequestBody DeviceRegistrationRequest request,
                                          HttpServletRequest servletRequest) {
        UUID userId = (UUID) servletRequest.getAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE);
        return authService.registerDevice(userId, request.deviceId(), request.fcmToken());
    }
}
