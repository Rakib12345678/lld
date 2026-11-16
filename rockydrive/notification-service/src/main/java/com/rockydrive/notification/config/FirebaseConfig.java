package com.rockydrive.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Initializes the {@link FirebaseApp} used for FCM pushes. Only active when
 * {@code notification.fcm.enabled=true}, so no live Firebase project/credentials are required to
 * build, test, or run this module by default.
 */
@Configuration
@ConditionalOnProperty(prefix = "notification.fcm", name = "enabled", havingValue = "true")
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseApp firebaseApp(FcmProperties fcmProperties) throws IOException {
        String credentialsPath = fcmProperties.getCredentialsPath();
        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalStateException(
                    "notification.fcm.enabled=true requires notification.fcm.credentials-path to be set");
        }
        try (FileInputStream credentialsStream = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();
            log.info("Initializing FirebaseApp for FCM pushes from {}", credentialsPath);
            return FirebaseApp.getApps().isEmpty() ? FirebaseApp.initializeApp(options) : FirebaseApp.getInstance();
        }
    }
}
