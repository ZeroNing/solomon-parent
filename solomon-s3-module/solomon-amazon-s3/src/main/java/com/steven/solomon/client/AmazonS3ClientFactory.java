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
 *
 * <p>负责创建 AWS SDK 的 {@link S3Client} 和 {@link S3Presigner} 实例，
 * 统一管理端点、凭证、区域和 HTTP 客户端等配置。</p>
 */
public final class AmazonS3ClientFactory {

  private AmazonS3ClientFactory() {
  }

  /**
   * 创建 S3 同步客户端。
   *
   * <p>配置项包括：</p>
   * <ul>
   *   <li>端点覆盖（Endpoint Override）</li>
   *   <li>区域设置</li>
   *   <li>访问凭证（Access Key / Secret Key）</li>
   *   <li>路径样式访问（Path-Style）</li>
   *   <li>连接超时和 Socket 超时</li>
   *   <li>禁用分块编码（Chunked Encoding）</li>
   * </ul>
   *
   * @param properties 文件存储配置属性
   * @return S3 同步客户端
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
   * 创建预签名 URL 生成器。
   *
   * <p>用于生成带过期时间的对象分享链接。</p>
   *
   * @param properties 文件存储配置属性
   * @return S3 预签名生成器
   */
  public static S3Presigner createPresigner(FileChoiceProperties properties) {
    return S3Presigner.builder()
        .endpointOverride(URI.create(properties.getEndpoint()))
        .region(Region.of(properties.getRegionName()))
        .credentialsProvider(StaticCredentialsProvider.create(credentials(properties)))
        .build();
  }

  /**
   * 从配置中创建 AWS 基础凭证。
   *
   * @param properties 文件存储配置属性
   * @return AWS 基础凭证
   */
  private static AwsBasicCredentials credentials(FileChoiceProperties properties) {
    return AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey());
  }
}
