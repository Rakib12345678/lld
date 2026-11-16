package com.rockydrive.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the (optional) live Firebase Cloud Messaging integration. FCM stays disabled
 * by default so the module builds/runs/test without any live Firebase credentials; the no-op
 * fallback ({@code NoOpFcmPushService}) is active until this is explicitly turned on.
 */
@ConfigurationProperties(prefix = "notification.fcm")
public class FcmProperties {

    /** Master switch for the real Firebase-backed push implementation. Default: false. */
    private boolean enabled = false;

    /** Path to the Firebase service-account JSON credentials file, required only when enabled. */
    private String credentialsPath;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCredentialsPath() {
        return credentialsPath;
    }

    public void setCredentialsPath(String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }
}
