package com.steven.solomon.rsa;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.util.io.pem.PemReader;

public class RSAUtils {

    private static String handleRSA(RSA rsa, String text, KeyType keyType, boolean encrypt) {
        byte[] data = encrypt ? rsa.encrypt(text, keyType) : rsa.decrypt(Base64.decode(text), keyType);
        return encrypt ? Base64.encode(data) : StrUtil.str(data, CharsetUtil.CHARSET_UTF_8);
    }

    private static String handleRSA(String text, String key, KeyType keyType, boolean encrypt) {
        RSA rsa = null;
        if (KeyType.PrivateKey == keyType) {
            rsa = new RSA(key, null);
        } else {
            rsa = new RSA(null, key);
        }
        return handleRSA(rsa, text, keyType, encrypt);
    }

    /**
     * 从PEM文件中加载密钥
     */
    private static byte[] loadKeyBytesFromPem(String pemFile) throws Exception {
        try (PemReader reader = new PemReader(new FileReader(pemFile))) {
            return reader.readPemObject().getContent();
        }
    }

    /**
     * 从PEM文件中加载私钥
     */
    private static PrivateKey loadPrivateKeyOfPem(String pemFile) throws Exception {
        byte[] privateKeyBytes = loadKeyBytesFromPem(pemFile);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * 从PEM文件中加载公钥
     */
    private static PublicKey loadPublicKeyOfPem(String pemFile) throws Exception {
        byte[] publicKeyBytes = loadKeyBytesFromPem(pemFile);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * 使用PEM文件加密或解密数据
     */
    private static String handleRSAWithPemFile(String data, String pemFile, KeyType keyType, boolean encrypt) throws Exception {
        RSA rsa = new RSA();
        if (KeyType.PrivateKey == keyType) {
            rsa.setPrivateKey(loadPrivateKeyOfPem(pemFile));
        } else {
            rsa.setPublicKey(loadPublicKeyOfPem(pemFile));
        }

        return handleRSA(rsa, data, keyType, encrypt);
    }

    public static String encryptPemFile(String data, String pemFile, KeyType keyType) throws Exception {
        return handleRSAWithPemFile(data, pemFile, keyType, true);
    }

    public static String decryptPemFile(String data, String pemFile, KeyType keyType) throws Exception {
        return handleRSAWithPemFile(data, pemFile, keyType, false);
    }

    /**
     * 加密
     */
    public static String encrypt(String text, String key, KeyType keyType) {
        return handleRSA(text, key, keyType, true);
    }

    /**
     * 解密
     */
    public static String decrypt(String text, String key, KeyType keyType) {
        return handleRSA(text, key, keyType, false);
    }
}
