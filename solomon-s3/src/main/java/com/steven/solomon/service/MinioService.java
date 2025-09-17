package com.steven.solomon.service;

import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.code.FileErrorCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.utils.FileTypeUtils;
import com.steven.solomon.verification.ValidateUtils;
import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.GetPresignedObjectUrlArgs.Builder;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import org.springframework.web.multipart.MultipartFile;
/**
 * Minio文件实现类
 */
public class MinioService extends AbstractFileService {

  public MinioClient client;

  public MinioService(FileChoiceProperties properties, FileNamingRulesGenerationService fileNamingRulesGenerationService, ClamAvUtils clamAvUtils) {
    super(properties,fileNamingRulesGenerationService,clamAvUtils);

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(properties.getConnectionTimeout(), TimeUnit.MILLISECONDS)  // 设置连接超时时间为10秒
            .readTimeout(properties.getSocketTimeout(), TimeUnit.MILLISECONDS)
            .build();

    MinioClient.Builder builder = MinioClient.builder().credentials(properties.getAccessKey(), properties.getSecretKey()).endpoint(properties.getEndpoint());
    if(ValidateUtils.isNotEmpty(properties.getRegionName())){
      builder.region(properties.getRegionName());
    }
    builder.httpClient(okHttpClient);
    client = builder.build();
  }

  @Override
  public FileUpload multipartUpload(MultipartFile file, String bucketName, boolean isUseOriginalName) throws Exception {
    return upload(file,bucketName,isUseOriginalName);
  }

  @Override
  protected void multipartUpload(MultipartFile file, String bucketName, long fileSize, String uploadId, String filePath,int partCount)
      throws Exception {
    this.upload(file,bucketName,filePath);
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    return client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
  }

  @Override
  protected void upload(MultipartFile file, String bucketName, String filePath) throws Exception {
    client.putObject(
        PutObjectArgs.builder().bucket(bucketName).object(filePath).stream(
            file.getInputStream(), file.getSize(), partSize)
            .contentType(FileTypeUtils.getFileType(file.getInputStream()))
            .build());
  }

  @Override
  protected void delete(String bucketName, String filePath) throws Exception {
    client.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(filePath).build());
  }

  @Override
  protected String shareUrl(String bucketName, String filePath, long expiry) throws Exception  {
    Builder builder = GetPresignedObjectUrlArgs.builder().bucket(bucketName);
    if(expiry >= Integer.MAX_VALUE){
      throw new BaseException(FileErrorCode.MORE_THAN_THE_SHARING_TIME);
    }
    return client.getPresignedObjectUrl(builder.expiry((int)expiry,TimeUnit.SECONDS).object(filePath).method(Method.GET).build());
  }

  @Override
  protected InputStream getObject(String bucketName, String filePath) throws Exception {
    StatObjectResponse statObject =client.statObject(StatObjectArgs.builder().bucket(bucketName).object(filePath).build());
    if (statObject != null && statObject.size() > 0) {
      return client.getObject(GetObjectArgs.builder().bucket(bucketName).object(filePath).build());
    } else {
      return null;
    }
  }

  @Override
  protected void createBucket(String bucketName) throws Exception {
    client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
  }

  @Override
  protected boolean checkObjectExist(String bucketName, String objectName) throws Exception {
    List<String> objectList = listObjects(bucketName,objectName);
    return ValidateUtils.isNotEmpty(objectList);
  }

  @Override
  protected void copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName)
      throws Exception {
    client.copyObject(CopyObjectArgs.builder().source(CopySource.builder().bucket(sourceBucket).object(sourceObjectName).build()).bucket(targetBucket).object(targetObjectName).build());
  }

  @Override
  protected void abortMultipartUpload(String uploadId, String bucketName, String filePath) {

  }

  @Override
  public List<String> listObjects(String bucketName,String key) throws Exception {
    if(ValidateUtils.isEmpty(bucketName) || !bucketExists(bucketName)){
      return new ArrayList<>();
    }
    Iterable<Result<Item>> response = ValidateUtils.isEmpty(key) ? client.listObjects(ListObjectsArgs.builder().bucket(bucketName).build()) : client.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(key).build());
    List<String> objectNames = new ArrayList<>();
    try {
      for (Result<Item> result : response) {
        objectNames.add(result.get().objectName());
      }
    }catch (Exception e){
      return objectNames;
    }
    return objectNames;
  }

  @Override
  public String initiateMultipartUploadTask(String bucketName, String objectName) throws Exception {
    return null;
  }

  @Override
  public void deleteBucket(String bucketName) throws Exception {
    if(ValidateUtils.isEmpty(bucketName)){
      logger.error("deleteBucket方法中,请求参数为空,删除桶失败");
    }
    client.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
  }

  @Override
  public List<String> getBucketList() throws Exception {
    List<Bucket> listBucketsResponse = client.listBuckets();
    List<String> bucketNameList      = new ArrayList<>();
    if(ValidateUtils.isNotEmpty(listBucketsResponse)){
      for(Bucket bucket : listBucketsResponse){
        bucketNameList.add(bucket.name());
      }
    }
    return bucketNameList;
  }

  @Override
  public boolean isMultipartUpload(){
    return false;
  }
}
