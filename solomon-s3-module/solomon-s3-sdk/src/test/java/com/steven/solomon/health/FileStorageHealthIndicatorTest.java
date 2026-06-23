package com.steven.solomon.health;

import static org.assertj.core.api.Assertions.assertThat;

import com.steven.solomon.enums.FileChoiceEnum;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.model.FileUploadRequest;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.DefaultService;
import com.steven.solomon.service.FileServiceInterface;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.multipart.MultipartFile;

class FileStorageHealthIndicatorTest {

  @Test
  void shouldReportDownWhenOnlyDefaultServiceIsActive() {
    FileChoiceProperties properties = new FileChoiceProperties();
    FileServiceInterface service = new DefaultService(properties, null, null);

    Health health = new FileStorageHealthIndicator(properties, service).health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails())
        .containsEntry("provider", "DEFAULT")
        .containsEntry("reason", "No object storage provider implementation is active");
  }

  @Test
  void shouldReportUpWhenProviderServiceIsActive() {
    FileChoiceProperties properties = new FileChoiceProperties();
    properties.setChoice(FileChoiceEnum.MINIO);
    properties.setEndpoint("http://localhost:9000");
    properties.setAccessKey("access");
    properties.setSecretKey("secret");
    properties.setBucketName("bucket-a");

    Health health = new FileStorageHealthIndicator(properties, new TestFileService()).health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails())
        .containsEntry("provider", "MINIO")
        .containsEntry("bucket", "bucket-a")
        .containsEntry("endpointConfigured", true);
  }

  private static class TestFileService implements FileServiceInterface {

    @Override
    public FileUpload upload(FileUploadRequest request) {
      return null;
    }

    @Override
    public FileUpload upload(MultipartFile file, String bucketName, boolean isUseOriginalName) {
      return null;
    }

    @Override
    public FileUpload upload(MultipartFile file, String bucketName) {
      return null;
    }

    @Override
    public FileUpload upload(InputStream is, String bucketName, String fileName, boolean isUseOriginalName) {
      return null;
    }

    @Override
    public FileUpload upload(InputStream is, String bucketName, String fileName) {
      return null;
    }

    @Override
    public FileUpload upload(String bucketName, BufferedImage bi, String fileName) {
      return null;
    }

    @Override
    public void deleteFile(String fileName, String bucketName) {
    }

    @Override
    public String share(String fileName, String bucketName, long expiry) {
      return null;
    }

    @Override
    public InputStream download(String fileName, String bucketName) {
      return null;
    }

    @Override
    public boolean bucketExists(String bucketName) {
      return true;
    }

    @Override
    public void makeBucket(String bucketName) {
    }

    @Override
    public boolean objectExist(String bucketName, String objectName) {
      return true;
    }

    @Override
    public boolean copyObject(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName) {
      return true;
    }

    @Override
    public InputStream generateThumbnail(String bucketName, String objectName, String filePath, boolean isUpload, int width, int height) {
      return null;
    }

    @Override
    public List<String> listObjects(String bucketName, String key) {
      return Collections.emptyList();
    }

    @Override
    public void deleteBucket(String bucketName) {
    }

    @Override
    public List<String> getBucketList() {
      return Collections.emptyList();
    }
  }
}
