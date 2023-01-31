//package com.steven.solomon.minio.utils;
//
//import com.steven.solomon.minio.graphics2D.entity.MinIo;
//import com.steven.solomon.minio.properties.MinioProperties;
//import io.minio.BucketExistsArgs;
//import io.minio.GetObjectArgs;
//import io.minio.GetPresignedObjectUrlArgs;
//import io.minio.ListObjectsArgs;
//import io.minio.MakeBucketArgs;
//import io.minio.MinioClient;
//import io.minio.PutObjectArgs;
//import io.minio.RemoveBucketArgs;
//import io.minio.RemoveObjectArgs;
//import io.minio.RemoveObjectsArgs;
//import io.minio.Result;
//import io.minio.StatObjectArgs;
//import io.minio.StatObjectResponse;
//import io.minio.http.Method;
//import io.minio.messages.Bucket;
//import io.minio.messages.DeleteError;
//import io.minio.messages.DeleteObject;
//import io.minio.messages.Item;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import javax.imageio.ImageIO;
//import javax.imageio.stream.ImageOutputStream;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//
//@Component
//public class MinioUtils {
//
//  private final MinioClient minioClient;
//
//  private final MinioProperties minioProperties;
//
//  public MinioUtils(MinioClient minioClient, MinioProperties minioProperties) {
//    this.minioClient     = minioClient;
//    this.minioProperties = minioProperties;
//  }
//
//  /**
//   * 检查存储桶是否存在
//   *
//   * @param bucketName 存储桶名称
//   * @return
//   */
//  public boolean bucketExists(String bucketName) throws Exception {
//    bucketName = bucketName.toLowerCase();
//    boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
//    return found;
//  }
//
//  /**
//   * 创建存储桶
//   *
//   * @param bucketName 存储桶名称
//   */
//  public boolean makeBucket(String bucketName) throws Exception {
//    bucketName = bucketName.toLowerCase();
//    boolean flag = bucketExists(bucketName);
//    if (!flag) {
//      minioClient.makeBucket(
//          MakeBucketArgs.builder()
//              .bucket(bucketName)
//              .build());
//      return true;
//    } else {
//      return false;
//    }
//  }
//
//  /**
//   * 列出所有存储桶名称
//   *
//   * @return
//   */
//  public List<String> listBucketNames() throws Exception {
//    List<Bucket> bucketList     = listBuckets();
//    List<String> bucketListName = new ArrayList<>();
//    for (Bucket bucket : bucketList) {
//      bucketListName.add(bucket.name());
//    }
//    return bucketListName;
//  }
//
//  /**
//   * 列出所有存储桶
//   *
//   * @return
//   */
//  public List<Bucket> listBuckets() throws Exception {
//    return minioClient.listBuckets();
//  }
//
//  /**
//   * 删除存储桶
//   *
//   * @param bucketName 存储桶名称
//   * @return
//   */
//  public boolean removeBucket(String bucketName) throws Exception {
//    bucketName = bucketName.toLowerCase();
//    boolean flag = bucketExists(bucketName);
//    if (flag) {
//      Iterable<Result<Item>> myObjects = listObjects(bucketName);
//      for (Result<Item> result : myObjects) {
//        Item item = result.get();
//        // 有对象文件，则删除失败
//        if (item.size() > 0) {
//          return false;
//        }
//      }
//      // 删除存储桶，注意，只有存储桶为空时才能删除成功。
//      minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
//      flag = bucketExists(bucketName);
//      if (!flag) {
//        return true;
//      }
//    }
//    return false;
//  }
//
//  /**
//   * 列出存储桶中的所有对象名称
//   *
//   * @param bucketName 存储桶名称
//   * @return
//   */
//  public List<String> listObjectNames(String bucketName) throws Exception {
//    bucketName = bucketName.toLowerCase();
//    List<String> listObjectNames = new ArrayList<>();
//    boolean      flag            = bucketExists(bucketName);
//    if (flag) {
//      Iterable<Result<Item>> myObjects = listObjects(bucketName);
//      for (Result<Item> result : myObjects) {
//        Item item = result.get();
//        listObjectNames.add(item.objectName());
//      }
//    } else {
//      listObjectNames.add("存储桶不存在");
//    }
//    return listObjectNames;
//  }
//
//  /**
//   * 列出存储桶中的所有对象
//   *
//   * @param bucketName 存储桶名称
//   * @return
//   */
//  public Iterable<Result<Item>> listObjects(String bucketName) throws Exception {
//    bucketName = bucketName.toLowerCase();
//    boolean flag = bucketExists(bucketName);
//    if (flag) {
//      return minioClient.listObjects(
//          ListObjectsArgs.builder().bucket(bucketName).build());
//    }
//    return null;
//  }
//
//  /**
//   * 文件上传
//   *
//   * @param bucketName
//   * @param inputStream
//   */
//  public void putObject(String bucketName, InputStream  inputStream, String filename, String fileType)
//      throws Exception {
//    bucketName = bucketName.toLowerCase();
//    makeBucket(bucketName);
//    minioClient.putObject(
//        PutObjectArgs.builder().bucket(bucketName).object(filename).stream(
//            inputStream, -1, minioProperties.getFileSize())
//            .contentType(fileType)
//            .build());
//  }
//
//  /**
//   * 文件上传
//   *
//   * @param bucketName
//   * @param bi
//   */
//  public MinIo putObject(String bucketName, BufferedImage bi, String filename) throws Exception {
//    bucketName = bucketName.toLowerCase();
//    ByteArrayOutputStream bs    = new ByteArrayOutputStream();
//    ImageOutputStream     imOut = ImageIO.createImageOutputStream(bs);
//    ImageIO.write(bi, "jpg", imOut);
//    this.putObject(bucketName,new ByteArrayInputStream(bs.toByteArray()),filename,FileTypeUtils.getFileType(new ByteArrayInputStream(bs.toByteArray())));
//    return new MinIo(bucketName,filename,new ByteArrayInputStream(bs.toByteArray()));
//  }
//
//  /**
//   * 文件访问路径
//   *
//   * @param bucketName 存储桶名称
//   * @param objectName 存储桶里的对象名称
//   * @return
//   */
//  public String getObjectUrl(String bucketName, String objectName) throws Exception {
//    boolean flag = bucketExists(bucketName);
//    String  url  = "";
//    if (flag) {
//      url = minioClient.getPresignedObjectUrl(
//          GetPresignedObjectUrlArgs.builder()
//              .method(Method.GET)
//              .bucket(bucketName)
//              .object(objectName)
//              .expiry(2, TimeUnit.MINUTES)
//              .build());
//    }
//    return url;
//  }
//
//  /**
//   * 删除一个对象
//   *
//   * @param bucketName 存储桶名称
//   * @param objectName 存储桶里的对象名称
//   */
//  public boolean removeObject(String bucketName, String objectName) throws Exception {
//    bucketName = bucketName.toLowerCase();
//    boolean flag = bucketExists(bucketName);
//    if (flag) {
//      minioClient.removeObject(
//          RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
//      return true;
//    }
//    return false;
//  }
//
//  /**
//   * 以流的形式获取一个文件对象
//   *
//   * @param bucketName 存储桶名称
//   * @param objectName 存储桶里的对象名称
//   * @return
//   */
//  public InputStream getObject(String bucketName, String objectName) throws Exception {
//    bucketName = bucketName.toLowerCase();
//    boolean flag = bucketExists(bucketName);
//    if (flag) {
//      StatObjectResponse statObject = statObject(bucketName, objectName);
//      if (statObject != null && statObject.size() > 0) {
//        InputStream stream =
//            minioClient.getObject(
//                GetObjectArgs.builder()
//                    .bucket(bucketName)
//                    .object(objectName)
//                    .build());
//        return stream;
//      }
//    }
//    return null;
//  }
//
//  /**
//   * 获取对象的元数据
//   *
//   * @param bucketName 存储桶名称
//   * @param objectName 存储桶里的对象名称
//   * @return
//   */
//  public StatObjectResponse statObject(String bucketName, String objectName) throws Exception {
//    bucketName = bucketName.toLowerCase();
//    boolean flag = bucketExists(bucketName);
//    if (flag) {
//      StatObjectResponse stat =
//          minioClient.statObject(
//              StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
//      return stat;
//    }
//    return null;
//  }
//
//  /**
//   * 删除指定桶的多个文件对象,返回删除错误的对象列表，全部删除成功，返回空列表
//   *
//   * @param bucketName  存储桶名称
//   * @param objectNames 含有要删除的多个object名称的迭代器对象
//   * @return
//   */
//  public boolean removeObject(String bucketName, List<String> objectNames) throws Exception {
//    bucketName = bucketName.toLowerCase();
//    boolean flag = bucketExists(bucketName);
//    if (flag) {
//      List<DeleteObject> objects = new LinkedList<>();
//      for (int i = 0; i < objectNames.size(); i++) {
//        objects.add(new DeleteObject(objectNames.get(i)));
//      }
//      Iterable<Result<DeleteError>> results =
//          minioClient.removeObjects(
//              RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
//      for (Result<DeleteError> result : results) {
//        DeleteError error = result.get();
//        System.out.println(
//            "Error in deleting object " + error.objectName() + "; " + error.message());
//        return false;
//      }
//    }
//    return true;
//  }
//
//  /**
//   * 以流的形式获取一个文件对象（断点下载）
//   *
//   * @param bucketName 存储桶名称
//   * @param objectName 存储桶里的对象名称
//   * @param offset     起始字节的位置
//   * @param length     要读取的长度 (可选，如果无值则代表读到文件结尾)
//   * @return
//   */
//  public InputStream getObject(String bucketName, String objectName, long offset, Long length) throws Exception {
//    bucketName = bucketName.toLowerCase();
//    boolean flag = bucketExists(bucketName);
//    if (flag) {
//      StatObjectResponse statObject = statObject(bucketName, objectName);
//      if (statObject != null && statObject.size() > 0) {
//        InputStream stream =
//            minioClient.getObject(
//                GetObjectArgs.builder()
//                    .bucket(bucketName)
//                    .object(objectName)
//                    .offset(offset)
//                    .length(length)
//                    .build());
//        return stream;
//      }
//    }
//    return null;
//  }
//
//
//  /**
//   * 通过InputStream上传对象
//   *
//   * @param bucketName  存储桶名称
//   * @param objectName  存储桶里的对象名称
//   * @param inputStream 要上传的流
//   * @param contentType 要上传的文件类型 MimeTypeUtils.IMAGE_JPEG_VALUE
//   * @return
//   */
//  public boolean putObject(String bucketName, String objectName, InputStream inputStream, String contentType)
//      throws Exception {
//    boolean flag = bucketExists(bucketName);
//    if (flag) {
//      minioClient.putObject(
//          PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
//              inputStream, -1, minioProperties.getFileSize())
//              .contentType(contentType)
//              .build());
//      StatObjectResponse statObject = statObject(bucketName, objectName);
//      if (statObject != null && statObject.size() > 0) {
//        return true;
//      }
//    }
//    return false;
//  }
//
//  /**
//   * 通过InputStream上传对象
//   *
//   * @param bucketName  存储桶名称
//   * @param objectName  存储桶里的对象名称
//   * @param inputStream 要上传的流
//   * @return
//   */
//  public MinIo putObject(String bucketName, String objectName, InputStream inputStream)
//      throws Exception {
//    this.putObject(bucketName,objectName,inputStream,FileTypeUtils.getFileType(inputStream));
//    return new MinIo(bucketName,objectName,inputStream);
//  }
//
//  /**
//   * 通过InputStream上传对象
//   *
//   * @param bucketName  存储桶名称
//   * @param objectName  存储桶里的对象名称
//   * @param file        要上传的文件
//   * @return
//   */
//  public MinIo putObject(String bucketName, String objectName, MultipartFile file)
//      throws Exception {
//    String type = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
//    this.putObject(bucketName,objectName,file.getInputStream(),FileTypeUtils.getFileType(type));
//    return new MinIo(bucketName,objectName,file.getInputStream());
//  }
//
//  /**
//   * 分享
//   * @param bucket 存储桶名称
//   * @param objectName 存储桶里的对象名称
//   * @param expiry 过期时间
//   * @return
//   * @throws Exception
//   */
//  public String share(String bucket,String objectName,int expiry)  throws Exception{
//    return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket).expiry(expiry).object(objectName).method(Method.GET).build());
//  }
//}
