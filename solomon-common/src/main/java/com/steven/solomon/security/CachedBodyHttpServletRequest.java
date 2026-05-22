package com.steven.solomon.security;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 可重复读取 body 的请求包装器。
 *
 * <p>签名校验和防重复提交都需要读取请求 body。
 * Servlet 原始输入流只能读取一次，所以这里在过滤器入口缓存 body，
 * 后续控制器仍然可以正常读取请求内容。</p>
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

  private final byte[] cachedBody;

  /**
   * 创建包装请求并缓存 body。
   */
  public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
    super(request);
    this.cachedBody = request.getInputStream().readAllBytes();
  }

  public byte[] getCachedBody() {
    return cachedBody.clone();
  }

  @Override
  public ServletInputStream getInputStream() {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return byteArrayInputStream.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException("Async read is not supported");
      }

      @Override
      public int read() {
        return byteArrayInputStream.read();
      }
    };
  }

  @Override
  public BufferedReader getReader() {
    Charset charset = getCharacterEncoding() == null
        ? StandardCharsets.UTF_8
        : Charset.forName(getCharacterEncoding());
    return new BufferedReader(new InputStreamReader(getInputStream(), charset));
  }
}
