package com.steven.solomon.service;

import com.steven.solomon.code.FileErrorCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.utils.FileTypeUtils;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

public class MinioService implements FileServiceInterface {

  public FileChoiceProperties mioProperties;

  public MinioClient client;

  @Autowired
  private FileNamingRulesGenerationService fileNamingRulesGenerationService;

  public MinioService() {
  }

  public MinioService(FileChoiceProperties mioProperties) {
    this.mioProperties = mioProperties;
    client = MinioClient.builder().credentials(mioProperties.getAccessKey(), mioProperties.getSecretKey()).endpoint(mioProperties.getEndpoint()).build();
  }

  @Override
  public FileUpload upload(MultipartFile file, String bucketName) throws Exception {
    //创建桶
    makeBucket(bucketName);
    String name = fileNamingRulesGenerationService.getFileName(file);
    String       filePath = getFilePath(name,mioProperties);
    //上传
    client.putObject(PutObjectArgs.builder().bucket(bucketName).object(filePath).stream(
        file.getInputStream(), file.getSize(), -1)
        .contentType(file.getContentType()).build());
    return new FileUpload(bucketName,name,file.getInputStream());
  }

  @Override
  public FileUpload upload(String bucketName, BufferedImage bi, String fileName) throws Exception {
    //创建桶
    makeBucket(bucketName);

    ByteArrayOutputStream bs    = new ByteArrayOutputStream();
    ImageOutputStream     imOut = ImageIO.createImageOutputStream(bs);
    ImageIO.write(bi, "jpg", imOut);
    String       filePath = getFilePath(fileName,mioProperties);

    client.putObject(
        PutObjectArgs.builder().bucket(bucketName).object(filePath).stream(
            new ByteArrayInputStream(bs.toByteArray()), new ByteArrayInputStream(bs.toByteArray()).available(), -1)
            .contentType(FileTypeUtils.getFileType(new ByteArrayInputStream(bs.toByteArray())))
            .build());
    return new FileUpload(bucketName,fileName,new ByteArrayInputStream(bs.toByteArray()));
  }

  @Override
  public void deleteFile(String fileName, String bucketName) throws Exception {
    boolean flag = bucketExists(bucketName);
    if (!flag) {
      return;
    }
    String filePath = getFilePath(fileName,mioProperties);
    client.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(filePath).build());
  }

  @Override
  public String share(String fileName, String bucketName, long expiry, TimeUnit unit) throws Exception {
    Builder builder = GetPresignedObjectUrlArgs.builder().bucket(bucketName);
    if(expiry >= Integer.MAX_VALUE){
      throw new BaseException(FileErrorCode.MORE_THAN_THE_SHARING_TIME);
    }
    return client.getPresignedObjectUrl(builder.expiry((int)expiry,unit).object(fileName).method(Method.GET).build());
  }

  @Override
  public InputStream download(String fileName, String bucketName) throws Exception {
    boolean flag = bucketExists(bucketName);
    if(!flag){
      return null;
    }
    String filePath = getFilePath(fileName,mioProperties);
    StatObjectResponse statObject =client.statObject(StatObjectArgs.builder().bucket(bucketName).object(filePath).build());
    if (statObject != null && statObject.size() > 0) {
      return client.getObject(GetObjectArgs.builder().bucket(bucketName).object(filePath).build());
    } else {
      return null;
    }
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    return client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
  }

  @Override
  public void makeBucket(String bucketName) throws Exception {
    boolean flag = bucketExists(bucketName);
    if(flag){
      return;
    }
    client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
  }

}
