package com.steven.solomon.service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;
/**
 * 七牛云文件实现类
 */
public class KODOService extends S3Service {

  public KODOService(FileChoiceProperties properties) {
    super(properties);
    AWSCredentials        credentials = new BasicAWSCredentials(properties.getAccessKey(), properties.getSecretKey());
    AmazonS3ClientBuilder builder     = AmazonS3ClientBuilder.standard();
    builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
    if(ValidateUtils.isNotEmpty(properties.getEndpoint()) && ValidateUtils.isNotEmpty(properties.getRegionName())){
      builder.withEndpointConfiguration(new EndpointConfiguration(properties.getEndpoint(),properties.getRegionName()));
    }
    ClientConfiguration awsClientConfig = new ClientConfiguration();
    boolean             isHttps         = properties.getEndpoint().contains("https");
    awsClientConfig.setProtocol(isHttps ? Protocol.HTTPS : Protocol.HTTP);
    builder.setClientConfiguration(awsClientConfig);
    client = builder.build();
  }

}
