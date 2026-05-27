package com.steven.solomon.model;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传请求。
 *
 * <p>用于统一描述 MultipartFile、InputStream、BufferedImage 三种上传入口，减少业务侧方法选择成本。</p>
 */
public class FileUploadRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Spring MVC 上传文件。 */
  private MultipartFile file;

  /** 普通文件输入流。 */
  private InputStream inputStream;

  /** 内存图片。 */
  private BufferedImage image;

  /** 存储桶名称。 */
  private String bucketName;

  /** 原始文件名或目标文件名。 */
  private String fileName;

  /** 是否使用原始文件名。 */
  private boolean useOriginalName;

  /** 内容类型。 */
  private String contentType;

  /** 用户元数据。 */
  private Map<String, String> metadata = new LinkedHashMap<>();

  /** 对象标签。 */
  private Map<String, String> tags = new LinkedHashMap<>();

  /** 是否允许覆盖同名文件。 */
  private boolean overwrite = true;

  /** 访问控制策略，供应商不支持时忽略。 */
  private String acl;

  /** 存储类型，供应商不支持时忽略。 */
  private String storageClass;

  /** 上传回调配置，供应商不支持时忽略。 */
  private String callback;

  public static FileUploadRequest create() {
    return new FileUploadRequest();
  }

  public static FileUploadRequest multipart(MultipartFile file) {
    return create().file(file);
  }

  public static FileUploadRequest stream(InputStream inputStream, String fileName) {
    return create().inputStream(inputStream).fileName(fileName);
  }

  public static FileUploadRequest image(BufferedImage image, String fileName) {
    return create().image(image).fileName(fileName);
  }

  public FileUploadRequest file(MultipartFile file) {
    this.file = file;
    return this;
  }

  public FileUploadRequest inputStream(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }

  public FileUploadRequest image(BufferedImage image) {
    this.image = image;
    return this;
  }

  public FileUploadRequest bucketName(String bucketName) {
    this.bucketName = bucketName;
    return this;
  }

  public FileUploadRequest fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  public FileUploadRequest useOriginalName(boolean useOriginalName) {
    this.useOriginalName = useOriginalName;
    return this;
  }

  public FileUploadRequest contentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  public FileUploadRequest metadata(String key, String value) {
    this.metadata.put(key, value);
    return this;
  }

  public FileUploadRequest metadata(Map<String, String> metadata) {
    this.metadata.clear();
    if (metadata != null) {
      this.metadata.putAll(metadata);
    }
    return this;
  }

  public FileUploadRequest tag(String key, String value) {
    this.tags.put(key, value);
    return this;
  }

  public FileUploadRequest tags(Map<String, String> tags) {
    this.tags.clear();
    if (tags != null) {
      this.tags.putAll(tags);
    }
    return this;
  }

  public FileUploadRequest overwrite(boolean overwrite) {
    this.overwrite = overwrite;
    return this;
  }

  public FileUploadRequest acl(String acl) {
    this.acl = acl;
    return this;
  }

  public FileUploadRequest storageClass(String storageClass) {
    this.storageClass = storageClass;
    return this;
  }

  public FileUploadRequest callback(String callback) {
    this.callback = callback;
    return this;
  }

  public MultipartFile getFile() {
    return file;
  }

  public FileUploadRequest setFile(MultipartFile file) {
    return file(file);
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public FileUploadRequest setInputStream(InputStream inputStream) {
    return inputStream(inputStream);
  }

  public BufferedImage getImage() {
    return image;
  }

  public FileUploadRequest setImage(BufferedImage image) {
    return image(image);
  }

  public String getBucketName() {
    return bucketName;
  }

  public FileUploadRequest setBucketName(String bucketName) {
    return bucketName(bucketName);
  }

  public String getFileName() {
    return fileName;
  }

  public FileUploadRequest setFileName(String fileName) {
    return fileName(fileName);
  }

  public boolean isUseOriginalName() {
    return useOriginalName;
  }

  public FileUploadRequest setUseOriginalName(boolean useOriginalName) {
    return useOriginalName(useOriginalName);
  }

  public String getContentType() {
    return contentType;
  }

  public FileUploadRequest setContentType(String contentType) {
    return contentType(contentType);
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public FileUploadRequest setMetadata(Map<String, String> metadata) {
    return metadata(metadata);
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public FileUploadRequest setTags(Map<String, String> tags) {
    return tags(tags);
  }

  public boolean isOverwrite() {
    return overwrite;
  }

  public FileUploadRequest setOverwrite(boolean overwrite) {
    return overwrite(overwrite);
  }

  public String getAcl() {
    return acl;
  }

  public FileUploadRequest setAcl(String acl) {
    return acl(acl);
  }

  public String getStorageClass() {
    return storageClass;
  }

  public FileUploadRequest setStorageClass(String storageClass) {
    return storageClass(storageClass);
  }

  public String getCallback() {
    return callback;
  }

  public FileUploadRequest setCallback(String callback) {
    return callback(callback);
  }
}
