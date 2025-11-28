package com.steven.solomon.service;

import com.aliyun.oss.ClientConfiguration;
import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.Protocol;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

public class S3Service extends AbstractFileService {

  protected S3Client client;

  private S3Presigner presigner;

  public S3Service(FileNamingRulesGenerationService fileNamingRulesGenerationService){
    super(fileNamingRulesGenerationService);
  }

  public S3Service(FileChoiceProperties properties, FileNamingRulesGenerationService fileNamingRulesGenerationService, ClamAvUtils clamAvUtils) {
    super(properties,fileNamingRulesGenerationService,clamAvUtils);
    AwsBasicCredentials credentials = AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey());
    client = S3Client.builder()
            .endpointOverride(URI.create(properties.getEndpoint()))
            .region(Region.of(properties.getRegionName()))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(properties.getPathStyleAccessEnabled()).chunkedEncodingEnabled(false).build())
            .httpClient(ApacheHttpClient.builder()
                    .connectionTimeout(Duration.ofSeconds(properties.getConnectionTimeout()))  // 连接超时
                    .socketTimeout(Duration.ofSeconds(properties.getSocketTimeout()))      // 读取数据超时
                    .build())
            .build();

    presigner = S3Presigner.builder()
            .endpointOverride(URI.create(properties.getEndpoint()))
            .region(Region.of(properties.getRegionName()))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
  }

  @Override
  protected void upload(MultipartFile file, String bucketName, String filePath) throws Exception {
    byte[] fileBytes = file.getBytes();

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(filePath)
            .contentLength((long) fileBytes.length)
            .build();
    client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
  }

  @Override
  protected void delete(String bucketName, String filePath) throws Exception {
    client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(filePath).build());
  }

  @Override
  protected String shareUrl(String bucketName, String filePath, long expiry) throws Exception {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(filePath)
            .build();
    GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
            .getObjectRequest(getObjectRequest)
            .signatureDuration(Duration.ofSeconds(expiry))
            .build();

    return presigner.presignGetObject(getObjectPresignRequest).url().toString();
  }

  @Override
  protected InputStream getObject(String bucketName, String filePath) throws Exception {
    return client.getObject(GetObjectRequest.builder().bucket(bucketName).key(filePath).build());
  }

  @Override
  protected void createBucket(String bucketName) throws Exception {
    client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
  }

  @Override
  protected boolean checkObjectExist(String bucketName, String objectName) throws Exception {
    try{
      client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(objectName).build());
      return true;
    } catch (Throwable e){
      return false;
    }
  }

  @Override
  protected void copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName) throws Exception {
    client.copyObject(CopyObjectRequest.builder()
            .sourceBucket(sourceBucket)
            .sourceKey(sourceObjectName)
            .destinationBucket(targetBucket)
            .destinationKey(targetObjectName)
            .build());
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    try{
      client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
      return true;
    } catch (Throwable e){
      return false;
    }
  }

  @Override
  public List<String> listObjects(String bucketName,String key) throws Exception {
    if(ValidateUtils.isEmpty(bucketName) || !bucketExists(bucketName)){
      return new ArrayList<>();
    }
    ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
            .bucket(bucketName);
    if (ValidateUtils.isNotEmpty(key)) {
      requestBuilder.prefix(key);
    }
    ListObjectsV2Request request = requestBuilder.build();
    ListObjectsV2Response response = client.listObjectsV2(request);
    return Lambda.toList(response.contents(),S3Object::key);
  }


  @Override
  public String initiateMultipartUploadTask(String bucketName,String objectName) throws Exception {
    CreateMultipartUploadRequest initRequest = CreateMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(objectName)
            .build();
    return client.createMultipartUpload(initRequest).uploadId();
  }

  @Override
  protected void abortMultipartUpload(String uploadId, String bucketName, String filePath) {
    client.abortMultipartUpload(AbortMultipartUploadRequest.builder().bucket(bucketName).key(filePath).uploadId(uploadId).build());
  }

  @Override
  protected void multipartUpload(MultipartFile file, String bucketName, long fileSize, String uploadId, String filePath,int partCount)
      throws Exception {
    // 分割文件并上传分片
    List<CompletedPart> partETags = new ArrayList<>();
    for (int i = 0; i < partCount; i++) {
      InputStream inputStream = file.getInputStream();
      // 跳到每个分块的开头
      long skipBytes = partSize * i;
      inputStream.skip(skipBytes);

      // 计算每个分块的大小
      long size = partSize < fileSize - skipBytes ?
                  partSize : fileSize - skipBytes;
      byte[] buffer = new byte[(int) size];
      inputStream.read(buffer);

      UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
              .bucket(bucketName)
              .key(filePath)
              .uploadId(uploadId)
              .partNumber(i + 1)
              .contentLength(size)
              .build();
      // 上传分片并添加到列表
      UploadPartResponse uploadPartResponse = client.uploadPart(uploadPartRequest, RequestBody.fromBytes(buffer));
      partETags.add(CompletedPart.builder()
              .partNumber(i + 1)
              .eTag(uploadPartResponse.eTag())
              .build());
    }

    // 完成分片上传
    CompleteMultipartUploadRequest compRequest =CompleteMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(filePath)
            .uploadId(uploadId)
            .multipartUpload(CompletedMultipartUpload.builder().parts(partETags).build()).build();
    client.completeMultipartUpload(compRequest);
  }


  @Override
  public void deleteBucket(String bucketName) throws Exception {
    if(ValidateUtils.isEmpty(bucketName)){
      logger.error("deleteBucket方法中,请求参数为空,删除桶失败");
    }
    client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
  }

  @Override
  public List<String> getBucketList() throws Exception {
    List<Bucket> bucketList = client.listBuckets().buckets();
    List<String> bucketNameList = new ArrayList<>();
    if(ValidateUtils.isNotEmpty(bucketList)){
      for(Bucket bucket : bucketList){
        bucketNameList.add(bucket.name());
      }
    }
    return bucketNameList;
  }
}
