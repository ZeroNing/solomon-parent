package com.steven.solomon.rsa;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.steven.solomon.verification.ValidateUtils;

public class RSAUtils {

  private static String publicKey;

  private static String privateKey;

  public RSAUtils(String publicKey, String privateKey) {
    RSAUtils.publicKey  = publicKey;
    RSAUtils.privateKey = privateKey;
  }

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
