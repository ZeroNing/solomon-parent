package com.steven.solomon.service;

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
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.GetPresignedObjectUrlArgs.Builder;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

public class MinioService extends AbstractFileService {

  public MinioClient client;

  public MinioService(FileChoiceProperties mioProperties) {
    super(mioProperties);
    client = MinioClient.builder().credentials(mioProperties.getAccessKey(), mioProperties.getSecretKey()).endpoint(mioProperties.getEndpoint()).build();
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    return client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
  }

  @Override
  protected void upload(MultipartFile file, String bucketName, String filePath) throws Exception {
    client.putObject(
        PutObjectArgs.builder().bucket(bucketName).object(filePath).stream(
            file.getInputStream(), file.getSize(), -1)
            .contentType(FileTypeUtils.getFileType(file.getInputStream(),file.getName()))
            .build());
  }

  @Override
  protected void delete(String bucketName, String filePath) throws Exception {
    client.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(filePath).build());
  }

  @Override
  protected String shareUrl(String bucketName, String filePath, long expiry, TimeUnit unit) throws Exception {
    Builder builder = GetPresignedObjectUrlArgs.builder().bucket(bucketName);
    if(expiry >= Integer.MAX_VALUE){
      throw new BaseException(FileErrorCode.MORE_THAN_THE_SHARING_TIME);
    }
    return client.getPresignedObjectUrl(builder.expiry((int)expiry,unit).object(filePath).method(Method.GET).build());
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
  protected boolean checkObject(String bucketName, String objectName) throws Exception {
    GetObjectResponse response = client.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    if(ValidateUtils.isEmpty(response) || ValidateUtils.isEmpty(response.object())){
      return false;
    } else {
      return true;
    }
  }

  @Override
  protected boolean copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName)
      throws Exception {
    client.copyObject(CopyObjectArgs.builder().source(CopySource.builder().bucket(sourceBucket).object(sourceObjectName).build()).bucket(targetBucket).object(targetObjectName).build());
    return true;
  }

}
