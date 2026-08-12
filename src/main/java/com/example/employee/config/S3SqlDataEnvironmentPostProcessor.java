package com.example.employee.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class S3SqlDataEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(S3SqlDataEnvironmentPostProcessor.class);

    private static final String PROPERTY_PREFIX = "employee.sql-init.s3";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        S3SqlDataProperties properties = Binder.get(environment)
                .bind(PROPERTY_PREFIX, Bindable.of(S3SqlDataProperties.class))
                .orElseGet(S3SqlDataProperties::new);

        if (!properties.enabled()) {
            return;
        }

        properties.validate();
        Path downloadedSql = downloadDataSql(properties);
        environment.getPropertySources().addFirst(new MapPropertySource(
                "s3SqlDataInitialization",
                Map.of("spring.sql.init.data-locations", downloadedSql.toUri().toString())
        ));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private Path downloadDataSql(S3SqlDataProperties properties) {
        try {
            Path target = Path.of(properties.downloadPath()).toAbsolutePath();
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            log.info("Downloading SQL initialization script from s3://{}/{} to {}",
                    properties.bucket(), properties.key(), target);

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(properties.key())
                    .build();

            try (S3Client s3Client = createS3Client(properties);
                 ResponseInputStream<GetObjectResponse> dataSql = s3Client.getObject(request)) {
                Files.copy(dataSql, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Downloaded SQL initialization script from s3://{}/{} to {}",
                    properties.bucket(), properties.key(), target);
            return target;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write SQL initialization script downloaded from S3", ex);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to download SQL initialization script from S3", ex);
        }
    }

    private S3Client createS3Client(S3SqlDataProperties properties) {
        return S3Client.builder()
                .region(Region.of(properties.region()))
                .build();
    }

    public static class S3SqlDataProperties {

        private boolean enabled;

        private String bucket;

        private String key;

        private String region;

        private String downloadPath = System.getProperty("java.io.tmpdir") + "/employee-service/data.sql";

        public boolean enabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String bucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String key() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String region() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String downloadPath() {
            return downloadPath;
        }

        public void setDownloadPath(String downloadPath) {
            this.downloadPath = downloadPath;
        }

        private void validate() {
            requireText(bucket, "bucket");
            requireText(key, "key");
            requireText(region, "region");
            requireText(downloadPath, "download-path");
        }

        private void requireText(String value, String propertyName) {
            if (!StringUtils.hasText(value)) {
                throw new IllegalStateException("Missing required property " + PROPERTY_PREFIX + "." + propertyName);
            }
        }
    }
}
