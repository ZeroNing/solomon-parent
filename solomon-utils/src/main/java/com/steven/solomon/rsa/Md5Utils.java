package com.steven.solomon.rsa;

import cn.hutool.crypto.digest.MD5;
import java.io.IOException;
import java.math.BigInteger;
import org.springframework.web.multipart.MultipartFile;

public class Md5Utils {

  /**
   * 文件md5加密
   * @param file
   * @return 返回文件校验码
   */
  public static String getFileMd5Code(MultipartFile file) throws IOException {
    return getFileMd5(file.getBytes());
  }

  public static String getFileMd5(byte[] fileBytes){
    return new BigInteger(1, MD5.create().digest(fileBytes)).toString(16);
  }

  public static String digest(String text){
    return MD5.create().digestHex(text);
  }
}
