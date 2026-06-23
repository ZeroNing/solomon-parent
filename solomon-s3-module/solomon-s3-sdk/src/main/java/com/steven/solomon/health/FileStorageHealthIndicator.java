package com.steven.solomon.health;

import com.steven.solomon.enums.FileChoiceEnum;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.DefaultService;
import com.steven.solomon.service.FileServiceInterface;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class FileStorageHealthIndicator implements HealthIndicator {

  private final FileChoiceProperties properties;
  private final FileServiceInterface fileService;

  public FileStorageHealthIndicator(FileChoiceProperties properties, FileServiceInterface fileService) {
    this.properties = properties;
    this.fileService = fileService;
  }

  @Override
  public Health health() {
    FileChoiceEnum choice = properties.getChoice();
    Health.Builder builder = choice == FileChoiceEnum.DEFAULT || fileService instanceof DefaultService
        ? Health.down()
        : Health.up();
    builder
        .withDetail("provider", choice == null ? "unknown" : choice.name())
        .withDetail("service", fileService == null ? "missing" : fileService.getClass().getName())
        .withDetail("bucket", safe(properties.getBucketName()))
        .withDetail("endpointConfigured", hasText(properties.getEndpoint()))
        .withDetail("bucketCheckOnStartup", properties.getCheckBucketOnStartup());
    if (choice == FileChoiceEnum.DEFAULT || fileService instanceof DefaultService) {
      builder.withDetail("reason", "No object storage provider implementation is active");
    }
    return builder.build();
  }

  private String safe(String value) {
    return hasText(value) ? value : "none";
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
