package com.steven.solomon.service;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.code.FileErrorCode;
import com.steven.solomon.properties.FileChoiceProperties;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.springframework.web.multipart.MultipartFile;

public class DefaultService extends AbstractFileService {


  public DefaultService(FileChoiceProperties properties) {
    super(properties);
  }

  @Override
  protected void upload(MultipartFile file, String bucketName, String filePath) throws Exception {
    throw new BaseException(FileErrorCode.NO_STORAGE_IMPLEMENTATION);
  }

  @Override
  protected void delete(String bucketName, String filePath) throws Exception {
    throw new BaseException(FileErrorCode.NO_STORAGE_IMPLEMENTATION);
  }

  @Override
  protected String shareUrl(String bucketName, String filePath, long expiry, TimeUnit unit) throws Exception {
    throw new BaseException(FileErrorCode.NO_STORAGE_IMPLEMENTATION);
  }

  @Override
  protected InputStream getObject(String bucketName, String filePath) throws Exception {
    throw new BaseException(FileErrorCode.NO_STORAGE_IMPLEMENTATION);
  }

  @Override
  protected void createBucket(String bucketName) throws Exception {
    throw new BaseException(FileErrorCode.NO_STORAGE_IMPLEMENTATION);
  }


  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    throw new BaseException(FileErrorCode.NO_STORAGE_IMPLEMENTATION);
  }
}
