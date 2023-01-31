package com.steven.solomon.rsa;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.steven.solomon.verification.ValidateUtils;

public class RSAUtils {

  private static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDM8zJOu+U2v4XjO8LUw6errGsbhSOnOkx4s5D8Ahe7C85qeYcriG3Op5BtT0LESLgtmJ4hBwNFeqqHjT5KVfbqGr9gAvFew5ZGhrbWenO/pIcmDkgccmen3ab7mS3GUPMJmjo4bMSlgyCcW1Nx+NIGNHSPb0QK44Gk9MyeQxLKOQIDAQAB";

  private static String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMzzMk675Ta/heM7wtTDp6usaxuFI6c6THizkPwCF7sLzmp5hyuIbc6nkG1PQsRIuC2YniEHA0V6qoeNPkpV9uoav2AC8V7DlkaGttZ6c7+khyYOSBxyZ6fdpvuZLcZQ8wmaOjhsxKWDIJxbU3H40gY0dI9vRArjgaT0zJ5DEso5AgMBAAECgYAo0K/unR2/QvILeF3LGHHeTvZ/owqgJYyQJxaTEWmfbvD7JFumcEOJ+bXaBwRhaagAMJohMq/UmOK4HwlOLYN/7PyUi6ANue2Te4O6ILjazQskAnzKjxwatPOrNKvbmhohLtBH/XbIpwdcGOHDOkRR7f5sZ+dfvdpx7FsqlmEiNwJBAP4oCeLT9Hrl3JGF5wcDajqMGl8d9Zv7uLXxkqkqOmIuhDjcOcE9O4vYlIjG9XFvo2LBimjVgmpwVxqO8yZ3Ua8CQQDOb8hvcbi+Iqu0Q0Cqi99B8slu80nrlSNlwwn6AZlyq9wghaFFMbcnMJaww+9NmUiPD8Jq1VwcK07COJbeWySXAkAp4w1mDcqgKpwEe84MkNqEFa0O+hANihnyGoMyUBxZLBNsj5cRKvdSX/py5F91N5H057LY/j9FLYidfuvB7i5PAkEAlpNstPVTVsS9T6g6c+vAdj8cdKRax86p2iEwlr4x6jW3Q7WVE36W5KwJTngshgKHrWpGWly0agug0AQs75W1kwJBAO+IX5FvhR/KJeNLOPogYDs/9EIO7zRf4GVxWsDfwSyYxIHWdKsVIcToHkg6wVE8wwq0TB/TzdetbJvrQfa2iaw=";

  /**
   * 加密
   *
   * @param text 明文
   * @return
   */
  public static String encrypt(String text) {
    if(ValidateUtils.isEmpty(text)){
      return text;
    }
    RSA rsa = new RSA(privateKey, null);
    return rsa.encryptBase64(text, KeyType.PrivateKey);
  }

  /**
   * 解密
   *
   * @param text 密文
   * @return
   */
  public static String decrypt(String text) {
    try {
      if(ValidateUtils.isEmpty(text)){
        return text;
      }
      RSA rsa = new RSA(null, publicKey);
      return StrUtil.str(rsa.decrypt(text, KeyType.PublicKey), CharsetUtil.CHARSET_UTF_8);
    } catch (Exception e) {
      return text;
    }

  }
}
