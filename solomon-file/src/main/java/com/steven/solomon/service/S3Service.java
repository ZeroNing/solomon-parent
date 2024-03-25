package com.steven.solomon.service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.web.multipart.MultipartFile;

public abstract class S3Service extends AbstractFileService {

  protected AmazonS3 client;

  public S3Service(){
    super();
  }

  public S3Service(FileChoiceProperties properties) {
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

  @Override
  protected void upload(MultipartFile file, String bucketName, String filePath) throws Exception {
    client.putObject(bucketName,filePath,file.getInputStream(), null);
  }

  @Override
  protected void delete(String bucketName, String filePath) throws Exception {
    client.deleteObject(bucketName,filePath);
  }

  @Override
  protected String shareUrl(String bucketName, String filePath, long expiry, TimeUnit unit) throws Exception {
    return client.generatePresignedUrl(bucketName,filePath,new Date(System.currentTimeMillis()+unit.toMillis(expiry))).toString();
  }

  @Override
  protected InputStream getObject(String bucketName, String filePath) throws Exception {
    return client.getObject(bucketName, filePath).getObjectContent();
  }

  @Override
  protected void createBucket(String bucketName) throws Exception {
    client.createBucket(bucketName);
  }

  @Override
  protected boolean checkObjectExist(String bucketName, String objectName) throws Exception {
    return client.doesObjectExist(bucketName,objectName);
  }

  @Override
  protected boolean copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName) throws Exception {
    client.copyObject(sourceBucket,sourceObjectName,targetBucket,targetObjectName);
    return true;
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    return client.doesBucketExistV2(bucketName);
  }

  @Override
  public List<String> listObjects(String bucketName,String key) throws Exception {
    if(ValidateUtils.isEmpty(bucketName) || !bucketExists(bucketName)){
      return new ArrayList<>();
    }
    ObjectListing response = ValidateUtils.isEmpty(key) ? client.listObjects(bucketName) : client.listObjects(bucketName,key);
    return Lambda.toList(response.getObjectSummaries(),data->data.getKey());
  }


  @Override
  public String initiateMultipartUploadTask(String bucketName,String objectName) throws Exception {
    InitiateMultipartUploadRequest initRequest  = new InitiateMultipartUploadRequest(bucketName, objectName);
    InitiateMultipartUploadResult  initResponse = client.initiateMultipartUpload(initRequest);
    return initResponse.getUploadId();
  }

  @Override
  protected void abortMultipartUpload(String uploadId, String bucketName, String filePath) {
    client.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName,filePath,uploadId));
  }

  @Override
  protected void multipartUpload(MultipartFile file, String bucketName, long fileSize, String uploadId, String filePath)
      throws Exception {
    // 分割文件并上传分片
    long           filePosition = 0;
    List<PartETag> partETags    = new ArrayList<>();
    for (int i = 1; filePosition < fileSize; i++) {
      // 计算当前分片大小
      partSize = Math.min(partSize, (fileSize - filePosition));

      // 创建上传请求
      UploadPartRequest uploadRequest = new UploadPartRequest()
          .withBucketName(bucketName).withKey(filePath)
          .withUploadId(uploadId).withPartNumber(i)
          .withInputStream(file.getInputStream())
          .withPartSize(partSize)
          .withFileOffset(filePosition);

      // 上传分片并添加到列表
      partETags.add(client.uploadPart(uploadRequest).getPartETag());

      filePosition += partSize;
    }
    // 完成分片上传
    CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
        bucketName, filePath, uploadId, partETags);

    client.completeMultipartUpload(compRequest);
  }


  @Override
  public void deleteBucket(String bucketName) throws Exception {
    if(ValidateUtils.isEmpty(bucketName)){
      logger.error("deleteBucket方法中,请求参数为空,删除桶失败");
    }
    client.deleteBucket(bucketName);
  }

  @Override
  public List<String> getBucketList() throws Exception {
    List<Bucket> bucketList = client.listBuckets();
    List<String> bucketNameList = new ArrayList<>();
    if(ValidateUtils.isNotEmpty(bucketList)){
      for(Bucket bucket : bucketList){
        bucketNameList.add(bucket.getName());
      }
    }
    return bucketNameList;
  }
}
