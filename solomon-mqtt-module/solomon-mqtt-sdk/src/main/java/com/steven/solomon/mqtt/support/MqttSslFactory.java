package com.steven.solomon.mqtt.support;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * MQTT SSL 工具。
 */
public final class MqttSslFactory {

  private MqttSslFactory() {
  }

  /**
   * 创建信任所有证书的 SocketFactory，仅在明确关闭证书校验时使用。
   */
  public static SSLSocketFactory trustAllSocketFactory() {
    try {
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, new TrustManager[]{new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
      }}, new SecureRandom());
      return sslContext.getSocketFactory();
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new IllegalStateException("初始化 MQTT SSL 配置失败", e);
    }
  }
}
