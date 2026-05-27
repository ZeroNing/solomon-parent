package com.steven.solomon.client;

import com.steven.solomon.properties.FileChoiceProperties;
import java.net.URI;
import java.time.Duration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Amazon S3 客户端工厂。
 */
public final class AmazonS3ClientFactory {

  private AmazonS3ClientFactory() {
  }

  /**
   * 创建 S3 同步客户端。
   */
  public static S3Client createClient(FileChoiceProperties properties) {
    AwsBasicCredentials credentials = credentials(properties);
    return S3Client.builder()
        .endpointOverride(URI.create(properties.getEndpoint()))
        .region(Region.of(properties.getRegionName()))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .serviceConfiguration(S3Configuration.builder()
            .pathStyleAccessEnabled(properties.getPathStyleAccessEnabled())
            .chunkedEncodingEnabled(false)
            .build())
        .httpClient(ApacheHttpClient.builder()
            .connectionTimeout(Duration.ofMillis(properties.getConnectionTimeout()))
            .socketTimeout(Duration.ofMillis(properties.getSocketTimeout()))
            .build())
        .build();
  }

  /**
   * 创建预签名客户端。
   */
  public static S3Presigner createPresigner(FileChoiceProperties properties) {
    return S3Presigner.builder()
        .endpointOverride(URI.create(properties.getEndpoint()))
        .region(Region.of(properties.getRegionName()))
        .credentialsProvider(StaticCredentialsProvider.create(credentials(properties)))
        .build();
  }

  private static AwsBasicCredentials credentials(FileChoiceProperties properties) {
    return AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey());
  }
}
