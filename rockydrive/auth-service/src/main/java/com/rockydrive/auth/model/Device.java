package com.rockydrive.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/**
 * A device a user has logged in from. notification-service reads these rows to know which FCM
 * token to push to for a given user.
 */
@Entity
@Table(name = "devices", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"}))
public class Device {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "fcm_token", nullable = false)
    private String fcmToken;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    protected Device() {
    }

    public Device(User user, String deviceId, String fcmToken) {
        this.user = user;
        this.deviceId = deviceId;
        this.fcmToken = fcmToken;
        this.lastSeenAt = Instant.now();
    }

    public void touch(String fcmToken) {
        this.fcmToken = fcmToken;
        this.lastSeenAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }
}
