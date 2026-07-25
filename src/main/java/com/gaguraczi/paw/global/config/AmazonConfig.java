package com.gaguraczi.paw.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Getter
public class AmazonConfig {

    private final String bucket;
    private final String cdnUrl;
    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String locationPath;

    /**
     * Creates Amazon S3 configuration from the supplied connection and storage settings.
     *
     * @param bucket       the S3 bucket name
     * @param cdnUrl       the CDN base URL
     * @param accessKey    the AWS access key
     * @param secretKey    the AWS secret key
     * @param region       the AWS region
     * @param locationPath the storage path within the bucket
     */
    public AmazonConfig(
            @Value("${cloud.aws.s3.bucket}") String bucket,
            @Value("${cloud.aws.s3.cdn-url}") String cdnUrl,
            @Value("${cloud.aws.credentials.access-key}") String accessKey,
            @Value("${cloud.aws.credentials.secret-key}") String secretKey,
            @Value("${cloud.aws.region}") String region,
            @Value("${cloud.aws.s3.path.location}") String locationPath
    ) {
        this.bucket = bucket;
        this.cdnUrl = cdnUrl;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.locationPath = locationPath;
    }

    /**
     * Creates an Amazon S3 client configured for the configured region and credentials.
     *
     * @return the configured Amazon S3 client
     */
    @Bean S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }
}
