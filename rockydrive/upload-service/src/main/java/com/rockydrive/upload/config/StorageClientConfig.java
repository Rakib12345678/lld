package com.rockydrive.upload.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Two clients, same credentials, different endpoints: {@code S3Client} talks to MinIO over the
 * internal/docker-network address for bucket operations, while {@code S3Presigner} signs URLs
 * against the public-facing endpoint so the browser/client can actually reach them.
 */
@Configuration
@EnableConfigurationProperties({StorageProperties.class, JwtProperties.class})
public class StorageClientConfig {

    @Bean
    public S3Client s3Client(StorageProperties props) {
        return S3Client.builder()
                .endpointOverride(URI.create(props.endpoint()))
                .region(Region.of(props.region()))
                .credentialsProvider(credentialsProvider(props))
                .forcePathStyle(props.forcePathStyle())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(StorageProperties props) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(props.publicEndpoint()))
                .region(Region.of(props.region()))
                .credentialsProvider(credentialsProvider(props))
                .build();
    }

    private StaticCredentialsProvider credentialsProvider(StorageProperties props) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.accessKey(), props.secretKey()));
    }
}
