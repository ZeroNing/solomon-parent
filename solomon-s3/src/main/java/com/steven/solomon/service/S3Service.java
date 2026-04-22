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

  /**
   * 检查S3对象是否存在
   *
   * <p>通过发送HEAD请求检查对象是否存在。</p>
   * <p>⚠️ 异常处理策略：</p>
   * <ul>
   *   <li>{@link NoSuchKeyException} - 对象不存在，返回false</li>
   *   <li>{@link S3Exception} - 其他S3错误，记录警告日志并返回false</li>
   *   <li>其他异常 - 向上抛出，由调用方处理</li>
   * </ul>
   *
   * @param bucketName 桶名称
   * @param objectName 对象键名
   * @return {@code true}: 对象存在<br>{@code false}: 对象不存在或发生错误
   */
  @Override
  protected boolean checkObjectExist(String bucketName, String objectName) throws Exception {
    try{
      client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(objectName).build());
      return true;
    } catch (NoSuchKeyException e){
      return false;  // 对象不存在
    } catch (S3Exception e) {
      logger.warn("检查S3对象是否存在时发生异常: bucket={}, key={}, error={}", bucketName, objectName, e.getMessage());
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

  /**
   * 检查S3桶是否存在
   *
   * <p>通过发送HEAD请求检查桶是否存在。</p>
   * <p>⚠️ 异常处理策略：</p>
   * <ul>
   *   <li>{@link NoSuchBucketException} - 桶不存在，返回false</li>
   *   <li>{@link S3Exception} - 其他S3错误，记录警告日志并返回false</li>
   *   <li>其他异常 - 向上抛出，由调用方处理</li>
   * </ul>
   *
   * @param bucketName 桶名称
   * @return {@code true}: 桶存在<br>{@code false}: 桶不存在或发生错误
   */
  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    try{
      client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
      return true;
    } catch (NoSuchBucketException e){
      return false;  // 桶不存在
    } catch (S3Exception e) {
      logger.warn("检查S3桶是否存在时发生异常: bucket={}, error={}", bucketName, e.getMessage());
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
    logger.info("[S3] 开始分片上传任务: bucket={}, key={}", bucketName, objectName);
    
    long startTime = System.currentTimeMillis();
    CreateMultipartUploadRequest initRequest = CreateMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(objectName)
            .build();
    
    String uploadId = client.createMultipartUpload(initRequest).uploadId();
    
    logger.info("[S3] 分片上传任务创建成功: bucket={}, key={}, uploadId={}, cost={}ms", 
        bucketName, objectName, uploadId, System.currentTimeMillis() - startTime);
    return uploadId;
  }

  @Override
  protected void abortMultipartUpload(String uploadId, String bucketName, String filePath) {
    logger.warn("[S3] 中止分片上传任务: bucket={}, key={}, uploadId={}", bucketName, filePath, uploadId);
    client.abortMultipartUpload(AbortMultipartUploadRequest.builder().bucket(bucketName).key(filePath).uploadId(uploadId).build());
  }

  /**
   * 分片上传大文件
   *
   * <p>将大文件分割成多个分片进行上传，适用于上传大文件（通常>5MB）。</p>
   *
   * <h3>⚠️ InputStream处理的关键点：</h3>
   * <ol>
   *   <li><b>循环skip</b>：{@link InputStream#skip(long)}不保证一次性跳过所有字节，
   *       必须循环调用直到跳过足够的字节数</li>
   *   <li><b>循环read</b>：{@link InputStream#read(byte[], int, int)}不保证读满buffer，
   *       必须循环调用直到读取足够的数据</li>
   *   <li><b>资源清理</b>：使用try-with-resources确保每个分片的InputStream被正确关闭</li>
   * </ol>
   *
   * <h3>内存使用说明：</h3>
   * <p>每个分片会分配一个大小为partSize的buffer，请根据可用内存合理设置分片大小。</p>
   *
   * @param file 上传的文件
   * @param bucketName 桶名称
   * @param fileSize 文件总大小（字节）
   * @param uploadId 分片上传任务ID
   * @param filePath 对象键名
   * @param partCount 分片总数
   * @throws IllegalStateException 当skip或read操作无法完成时抛出
   * @throws Exception 当S3上传失败时抛出
   */
  @Override
  protected void multipartUpload(MultipartFile file, String bucketName, long fileSize, String uploadId, String filePath,int partCount)
      throws Exception {
    // 分割文件并上传分片
    logger.info("[S3] 开始分片上传: bucket={}, key={}, fileSize={}, partCount={}, partSize={}", 
        bucketName, filePath, fileSize, partCount, partSize);
    
    List<CompletedPart> partETags = new ArrayList<>();
    long uploadStartTime = System.currentTimeMillis();
    
    for (int i = 0; i < partCount; i++) {
      long partStartTime = System.currentTimeMillis();
      
      // 使用try-with-resources确保InputStream被正确关闭，避免资源泄漏
      try (InputStream inputStream = file.getInputStream()) {
        // ========== 跳过前序分片的字节 ==========
        // ⚠️ 关键：skip()方法不保证一次性跳过所有字节，必须循环调用
        // 例如：某些InputStream实现可能每次只跳过1字节
        long skipBytes = (long) partSize * i;
        long skipped = 0;
        while (skipped < skipBytes) {
          long n = inputStream.skip(skipBytes - skipped);
          if (n <= 0) {
            // skip返回0或负数表示无法继续跳过，可能是文件已结束
            logger.error("[S3] 分片跳过失败: part={}/{}, expected={}, actual={}", 
                i + 1, partCount, skipBytes, skipped);
            throw new IllegalStateException("无法跳过足够的字节，期望: " + skipBytes + ", 实际: " + skipped);
          }
          skipped += n;
        }

        // ========== 计算当前分片大小 ==========
        // 最后一个分片可能小于partSize
        long size = partSize < fileSize - skipBytes ? partSize : fileSize - skipBytes;
        byte[] buffer = new byte[(int) size];
        
        // ========== 读取分片数据 ==========
        // ⚠️ 关键：read()方法不保证读满buffer，必须循环调用
        // 例如：网络InputStream可能分多次返回数据
        int bytesRead = 0;
        while (bytesRead < size) {
          int n = inputStream.read(buffer, bytesRead, (int) size - bytesRead);
          if (n < 0) {
            // read返回-1表示文件已结束，但此时数据还未读满
            logger.error("[S3] 分片读取失败: part={}/{}, expected={}, actual={}", 
                i + 1, partCount, size, bytesRead);
            throw new IllegalStateException("意外的文件结束，期望: " + size + ", 实际: " + bytesRead);
          }
          bytesRead += n;
        }

        // ========== 上传分片到S3 ==========
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .uploadId(uploadId)
                .partNumber(i + 1)  // S3分片号从1开始
                .contentLength(size)
                .build();
        
        // 上传分片并记录ETag（用于完成分片上传时校验）
        UploadPartResponse uploadPartResponse = client.uploadPart(uploadPartRequest, RequestBody.fromBytes(buffer));
        partETags.add(CompletedPart.builder()
                .partNumber(i + 1)
                .eTag(uploadPartResponse.eTag())
                .build());
        
        long partCost = System.currentTimeMillis() - partStartTime;
        logger.debug("[S3] 分片上传完成: part={}/{}, size={}bytes, cost={}ms, eTag={}", 
            i + 1, partCount, size, partCost, uploadPartResponse.eTag());
      }
      // try-with-resources确保inputStream在此处自动关闭
    }

    // ========== 完成分片上传 ==========
    // 将所有分片合并成完整对象
    logger.info("[S3] 开始完成分片上传: bucket={}, key={}, uploadId={}, parts={}", 
        bucketName, filePath, uploadId, partETags.size());
    
    CompleteMultipartUploadRequest compRequest =CompleteMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(filePath)
            .uploadId(uploadId)
            .multipartUpload(CompletedMultipartUpload.builder().parts(partETags).build()).build();
    client.completeMultipartUpload(compRequest);
    
    long totalCost = System.currentTimeMillis() - uploadStartTime;
    logger.info("[S3] 分片上传完成: bucket={}, key={}, totalParts={}, totalCost={}ms", 
        bucketName, filePath, partCount, totalCost);
  }


  @Override
  public void deleteBucket(String bucketName) throws Exception {
    if(ValidateUtils.isEmpty(bucketName)){
      logger.error("[S3] 删除桶失败，参数为空: bucketName=null");
      return;
    }
    
    logger.info("[S3] 开始删除桶: bucket={}", bucketName);
    long startTime = System.currentTimeMillis();
    
    try {
      client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
      logger.info("[S3] 桶删除成功: bucket={}, cost={}ms", bucketName, System.currentTimeMillis() - startTime);
    } catch (Exception e) {
      logger.error("[S3] 桶删除失败: bucket={}, error={}", bucketName, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public List<String> getBucketList() throws Exception {
    logger.debug("[S3] 开始获取桶列表");
    
    List<Bucket> bucketList = client.listBuckets().buckets();
    List<String> bucketNameList = new ArrayList<>();
    
    if(ValidateUtils.isNotEmpty(bucketList)){
      for(Bucket bucket : bucketList){
        bucketNameList.add(bucket.name());
      }
    }
    
    logger.info("[S3] 获取桶列表成功: count={}", bucketNameList.size());
    return bucketNameList;
  }
}
