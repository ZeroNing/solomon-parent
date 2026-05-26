package com.steven.solomon.model;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.Serializable;
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
}
