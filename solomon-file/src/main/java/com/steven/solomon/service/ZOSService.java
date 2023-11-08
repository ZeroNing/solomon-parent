package com.steven.solomon.service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PresignedUrlUploadRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.BosObject;
import com.baidubce.services.bos.model.PutObjectRequest;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.springframework.web.multipart.MultipartFile;

/**
 * 天翼云文件实现类
 */
public class ZOSService extends S3Service {

  public ZOSService(FileChoiceProperties properties) {
    super(properties);
    AWSCredentials credentials = new BasicAWSCredentials(properties.getAccessKey(), properties.getSecretKey());
    AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
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
