package com.steven.solomon.service;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.code.FileErrorCode;
import com.steven.solomon.enums.StorageCapability;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.file.MockMultipartFile;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.model.FileUploadRequest;
import com.steven.solomon.naming.rules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.utils.logger.LoggerUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务公共骨架。
 *
 * <p>该类沉淀上传、分片上传调度、病毒扫描、缩略图、路径拼接等供应商无关逻辑；
 * 具体供应商只需要实现存储桶、对象、分享链接等平台差异能力。</p>
 */
public abstract class AbstractFileService implements FileServiceInterface{

  protected static Logger logger = LoggerUtils.logger(AbstractFileService.class);

  protected FileNamingRulesGenerationService fileNamingRulesGenerationService;

  protected FileChoiceProperties properties;

  protected Long partSize;

  protected ClamAvUtils clamAvUtils;

  protected MeterRegistry meterRegistry;

  public AbstractFileService(FileChoiceProperties properties,FileNamingRulesGenerationService fileNamingRulesGenerationService,ClamAvUtils clamAvUtils) {
    this.fileNamingRulesGenerationService = fileNamingRulesGenerationService;
    this.properties = properties;
    this.partSize = (long) (this.properties.getPartSize() * 1024 * 1024);
    this.clamAvUtils = clamAvUtils;
  }

  public AbstractFileService(FileNamingRulesGenerationService fileNamingRulesGenerationService) {
      this.fileNamingRulesGenerationService = fileNamingRulesGenerationService;
      this.partSize = (long) (5 * 1024 * 1024);
  }

  public AbstractFileService() {
    super();
  }

  @Autowired(required = false)
  public void setMeterRegistry(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public FileUpload upload(FileUploadRequest request) throws Exception {
    requireCapability(StorageCapability.UPLOAD);
    if (ObjectUtil.isEmpty(request)) {
      throw new BaseException(FileErrorCode.INVALID_UPLOAD_REQUEST);
    }
    if (ObjectUtil.isNotEmpty(request.getFile())) {
      return uploadFileRequest(request, request.getFile());
    }
    if (ObjectUtil.isNotEmpty(request.getInputStream())) {
      MockMultipartFile file = new MockMultipartFile(request.getFileName(), request.getFileName(), MediaType.MULTIPART_FORM_DATA_VALUE, request.getInputStream());
      return uploadFileRequest(request, file);
    }
    if (ObjectUtil.isNotEmpty(request.getImage())) {
      return upload(request.getBucketName(), request.getImage(), request.getFileName());
    }
    throw new BaseException(FileErrorCode.INVALID_UPLOAD_REQUEST);
  }

  @Override
  public FileUpload upload(InputStream is, String bucketName,String fileName) throws Exception {
    return upload(is,bucketName,fileName,false);
  }

  @Override
  public FileUpload upload(InputStream is, String bucketName,String fileName,boolean isUseOriginalName) throws Exception {
    return upload(new MockMultipartFile(fileName,fileName, MediaType.MULTIPART_FORM_DATA_VALUE, is),bucketName,isUseOriginalName);
  }

  @Override
  public FileUpload upload(MultipartFile file, String bucketName) throws Exception {
    return upload(file,bucketName,false);
  }

  @Override
  public FileUpload upload(MultipartFile file,String bucketName,boolean isUseOriginalName) throws Exception{
    return uploadFileRequest(FileUploadRequest.multipart(file).bucketName(bucketName).useOriginalName(isUseOriginalName), file);
  }

  /**
   * 使用统一请求对象执行上传，保留 contentType、metadata、tags 等高级参数。
   */
  protected FileUpload uploadFileRequest(FileUploadRequest request, MultipartFile file) throws Exception {
    return recordStorageOperation("upload", request.getBucketName(), () -> uploadFileRequestInternal(request, file));
  }

  private FileUpload uploadFileRequestInternal(FileUploadRequest request, MultipartFile file) throws Exception {
    requireCapability(StorageCapability.UPLOAD);
    clamAvUtils.scanFile(file.getInputStream(), BaseExceptionCode.FILE_HIGH_RISK);
    // 上传前保证存储桶存在，减少业务侧重复判断。
    makeBucket(request.getBucketName());
    String       filePath = getFilePath(!request.isUseOriginalName() ? fileNamingRulesGenerationService.getFileName(file): file.getOriginalFilename(),properties);
    if (!request.isOverwrite() && checkObjectExist(request.getBucketName(), filePath)) {
      throw new IllegalStateException("文件已存在且当前上传请求不允许覆盖: " + filePath);
    }
    long fileSize = file.getSize();
    if (isMultipartUpload()) {
      if (fileSize >= partSize) {
        return multipartUpload(file,request.getBucketName(),request.isUseOriginalName());
      } else {
        return uploadPreparedFile(request, file, filePath);
      }
    } else {
      return uploadPreparedFile(request, file, filePath);
    }
  }

  /**
   * 供应商可覆盖该方法读取高级上传参数，默认走老的上传实现。
   */
  protected FileUpload uploadPreparedFile(FileUploadRequest request, MultipartFile file, String filePath) throws Exception {
    this.upload(file, request.getBucketName(), filePath);
    return new FileUpload(request.getBucketName(),filePath,file.getInputStream());
  }

  @Override
  public FileUpload upload(String bucketName, BufferedImage bi, String fileName) throws Exception {
    // 内存图片也走统一上传链路，确保命名、路径和返回值一致。
    makeBucket(bucketName);

    String       filePath = getFilePath(fileName,properties);

    ByteArrayOutputStream bs    = new ByteArrayOutputStream();
    ImageOutputStream     imOut = ImageIO.createImageOutputStream(bs);
    ImageIO.write(bi, "jpg", imOut);

    InputStream inputStream = new ByteArrayInputStream(bs.toByteArray());
    MockMultipartFile file =  new MockMultipartFile(fileName,inputStream);

    this.upload(file,bucketName,filePath);
    return new FileUpload(bucketName,filePath,new ByteArrayInputStream(bs.toByteArray()));
  }

  @Override
  public void deleteFile(String fileName, String bucketName) throws Exception {
    recordStorageOperation("delete", bucketName, () -> {
      requireCapability(StorageCapability.DELETE);
      if (!bucketExists(bucketName) || ObjectUtil.isEmpty(fileName)) {
        return null;
      }
      delete(bucketName,getFilePath(fileName,properties));
      return null;
    });
  }

  @Override
  public String share(String fileName, String bucketName, long expiry) throws Exception {
    return recordStorageOperation("share", bucketName, () -> {
      requireCapability(StorageCapability.SHARE_URL);
      return shareUrl(bucketName,getFilePath(fileName,properties),expiry);
    });
  }

  @Override
  public InputStream download(String fileName, String bucketName) throws Exception {
    return recordStorageOperation("download", bucketName, () -> {
      requireCapability(StorageCapability.DOWNLOAD);
      String filePath = getFilePath(fileName,properties);
      if (!objectExist(bucketName,filePath)) {
        throw new BaseException(BaseExceptionCode.FILE_IS_NOT_EXIST_EXCEPTION_CODE);
      }
      return getObject(bucketName,filePath);
    });
  }

  @Override
  public void makeBucket(String bucketName) throws Exception {
    requireCapability(StorageCapability.CREATE_BUCKET);
    if (bucketExists(bucketName)) {
      return;
    }
    if (!properties.getAutoCreateBucket()) {
      throw new IllegalStateException("桶不存在且当前配置不允许自动创建: " + bucketName);
    }
    this.createBucket(bucketName);
  }

  @Override
  public boolean copyObject(String sourceBucket,String targetBucket,String sourceObjectName,String targetObjectName) throws Exception{
    return recordStorageOperation("copy", sourceBucket, () -> {
      requireCapability(StorageCapability.COPY_OBJECT);
      if (!objectExist(sourceBucket,sourceObjectName)) {
        throw new BaseException(BaseExceptionCode.FILE_IS_NOT_EXIST_EXCEPTION_CODE);
      }
      copyFile(sourceBucket,targetBucket,getFilePath(sourceObjectName,properties),getFilePath(targetObjectName,properties));
      return true;
    });
  }

  @Override
  public boolean objectExist(String bucketName,String objectName) throws Exception{
    try {
      return checkObjectExist(bucketName,getFilePath(objectName,properties));
    } catch (Throwable e) {
      logger.error("检查文件出现异常",e);
      return false;
    }
  }

  @Override
  public InputStream generateThumbnail(String bucketName,String objectName,String filePath,boolean isUpload,int width,int height)throws Exception{
    requireCapability(StorageCapability.THUMBNAIL);
    makeBucket(bucketName);
    String extensionName = fileNamingRulesGenerationService.getExtensionName(objectName);
    objectName = objectName.substring(0,objectName.indexOf("."+extensionName));
    String thumbnailName = new StringBuilder(ObjectUtil.defaultIfNull(filePath,ObjectUtil.defaultIfNull(properties.getRootDirectory(), StrUtil.EMPTY))).append(objectName).append("_").append(width).append("_").append(height).append(".").append(extensionName).toString();
    if (!objectExist(bucketName,thumbnailName)) {
      MockMultipartFile file = null;
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        ImgUtil.scale(getObject(bucketName,objectName+"."+extensionName), baos, width, height, Color.decode("0xFFFFFF"));
        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
          bais.reset();
          try (InputStream is = new ByteArrayInputStream(bais.readAllBytes())) {
            if (isUpload) {
              file = new MockMultipartFile(thumbnailName,thumbnailName, MediaType.MULTIPART_FORM_DATA_VALUE, is);
              upload(file,bucketName,true);
              return file.getInputStream();
            } else {
              return is;
            }
          }
        }
      }
    } else {
      return getObject(bucketName,thumbnailName);
    }
  }

  public FileUpload multipartUpload(MultipartFile file, String bucketName, boolean isUseOriginalName) throws Exception {
    String filePath = getFilePath(!isUseOriginalName? fileNamingRulesGenerationService.getFileName(file): file.getName(),properties);
    String uploadId = initiateMultipartUploadTask(bucketName, filePath);

    long contentLength = file.getSize();
    int partCount = (int) (contentLength / partSize);
    if (contentLength % partSize != 0) {
      partCount++;
    }
    try{
      multipartUpload(file,bucketName,contentLength,uploadId,filePath,partCount);
    }catch (Exception e) {
      abortMultipartUpload(uploadId,bucketName,filePath);
      throw e;
    }
    return new FileUpload(bucketName,filePath,file.getInputStream());
  }

  /**
   * 分片上传
   * @param file 文件
   * @param bucketName 桶名
   * @param fileSize 文件大小
   * @param uploadId 上传id
   * @param filePath 文件名
   */
  protected abstract void multipartUpload(MultipartFile file, String bucketName,long fileSize,String uploadId,String filePath,int partCount) throws Exception;

  /**
   * 上传
   * @param file 文件
   * @param bucketName 桶名
   * @param filePath 文件名
   */
  protected abstract void upload(MultipartFile file, String bucketName,String filePath) throws Exception;

  /**
   * 删除
   * @param bucketName 桶名
   * @param filePath 文件名
   */
  protected abstract void delete(String bucketName,String filePath) throws Exception;

  /**
   * 分享url
   * @param bucketName 桶名
   * @param filePath 文件名
   * @param expiry 时间
   */
  protected abstract String shareUrl(String bucketName,String filePath,long expiry) throws Exception;

  /**
   * 获取文件流
   * @param bucketName 桶名
   * @param filePath 文件名
   */
  protected abstract InputStream getObject(String bucketName,String filePath) throws Exception;

  /**
   * 创建桶
   * @param bucketName 桶名
   */
  protected abstract void createBucket(String bucketName) throws Exception;

  /**
   * 判断文件是否存在
   * @param bucketName 桶名
   * @param objectName 文件名
   */
  protected abstract boolean checkObjectExist(String bucketName,String objectName) throws Exception;

  /**
   * 复制文件
   *
   * @param sourceBucket     来源桶
   * @param targetBucket     目标桶
   * @param sourceObjectName 来源文件名
   * @param targetObjectName 目标文件名
   */
  protected abstract void copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName) throws Exception;

  /**
   * 取消分片上传
   * @param uploadId 上传id
   * @param bucketName 桶名
   * @param filePath 文件名
   */
  protected abstract void abortMultipartUpload(String uploadId,String bucketName,String filePath) throws Exception;

  /**
   * 初始化分片任务
   * @param bucketName 桶名
   * @param objectName 文件名
   */
  protected abstract String initiateMultipartUploadTask(String bucketName,String objectName) throws Exception;

  public boolean isMultipartUpload() {
    return true;
  }

  @Override
  public Set<StorageCapability> capabilities() {
    Set<StorageCapability> capabilities = EnumSet.copyOf(FileServiceInterface.super.capabilities());
    if (isMultipartUpload()) {
      capabilities.add(StorageCapability.MULTIPART_UPLOAD);
    }
    return capabilities;
  }

  /**
   * 启动时按配置检查默认桶，降低第一次上传时才暴露配置问题的概率。
   */
  public void checkDefaultBucketOnStartup() throws Exception {
    if (properties.getCheckBucketOnStartup() && ObjectUtil.isNotEmpty(properties.getBucketName())) {
      makeBucket(properties.getBucketName());
    }
  }

  /**
   * 调用供应商能力前先校验，避免不支持能力时进入更深层异常。
   */
  protected <T> T recordStorageOperation(String operation, String bucketName, StorageOperation<T> operationCall)
      throws Exception {
    Timer.Sample sample = meterRegistry == null ? null : Timer.start(meterRegistry);
    String outcome = "success";
    try {
      return operationCall.call();
    } catch (Exception e) {
      outcome = "error";
      logger.error("Object storage operation failed, provider={}, operation={}, bucket={}",
          getClass().getSimpleName(), operation, safeBucket(bucketName), e);
      throw e;
    } finally {
      recordStorageMetrics(operation, bucketName, outcome, sample);
    }
  }

  private void recordStorageMetrics(String operation, String bucketName, String outcome, Timer.Sample sample) {
    if (meterRegistry == null) {
      return;
    }
    Tags tags = Tags.of(
        "provider", getClass().getSimpleName(),
        "operation", operation,
        "bucket", safeBucket(bucketName),
        "outcome", outcome);
    meterRegistry.counter("solomon.s3.operation.total", tags).increment();
    if (sample != null) {
      sample.stop(Timer.builder("solomon.s3.operation.duration").tags(tags).register(meterRegistry));
    }
  }

  private String safeBucket(String bucketName) {
    return StrUtil.isBlank(bucketName) ? "unknown" : bucketName;
  }

  @FunctionalInterface
  protected interface StorageOperation<T> {
    T call() throws Exception;
  }

  protected void requireCapability(StorageCapability capability) {
    if (!capabilities().contains(capability)) {
      throw new UnsupportedOperationException("当前对象存储实现不支持能力: " + capability);
    }
  }
}
