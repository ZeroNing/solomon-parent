package com.steven.solomon.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steven.solomon.holder.RequestHeaderHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class ApiSecurityProtectionFilter extends OncePerRequestFilter {

  private static final String TENANT_FALLBACK = "default";

  private final SecurityProtectionProperties properties;

  private final ProtectionStore protectionStore;

  private final ObjectMapper objectMapper;

  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public ApiSecurityProtectionFilter(SecurityProtectionProperties properties,
      ProtectionStore protectionStore,
      ObjectMapper objectMapper) {
    this.properties = properties;
    this.protectionStore = protectionStore;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    if (!properties.isEnabled()) {
      return true;
    }
    String path = request.getRequestURI();
    if (properties.getExcludePaths() != null) {
      for (String pattern : properties.getExcludePaths()) {
        if (pathMatcher.match(pattern, path)) {
          return true;
        }
      }
    }
    return !isRateLimitEnabled(request) && !isSignatureEnabled(request) && !isDuplicateSubmitEnabled(request);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
    bindTenantHeaders(cachedRequest);

    try {
      protect(cachedRequest);
      filterChain.doFilter(cachedRequest, response);
    } catch (SecurityProtectionException e) {
      writeError(response, e.getStatus(), e.getCode(), e.getMessage());
    }
  }

  private void protect(CachedBodyHttpServletRequest request) {
    if (isRateLimitEnabled(request)) {
      verifyRateLimit(request);
    }
    if (isSignatureEnabled(request)) {
      verifySignature(request);
    }
    if (isDuplicateSubmitEnabled(request)) {
      verifyDuplicateSubmit(request);
    }
  }

  private void bindTenantHeaders(HttpServletRequest request) {
    setIfPresent(properties.getTenantIdHeader(), request, RequestHeaderHolder::setTenantId);
    setIfPresent(properties.getTenantCodeHeader(), request, RequestHeaderHolder::setTenantCode);
    setIfPresent(properties.getTenantNameHeader(), request, RequestHeaderHolder::setTenantName);
  }

  private void setIfPresent(String header, HttpServletRequest request, HeaderSetter setter) {
    if (!StringUtils.hasText(header)) {
      return;
    }
    String value = request.getHeader(header);
    if (StringUtils.hasText(value)) {
      setter.set(value);
    }
  }

  private boolean isDuplicateSubmitEnabled(HttpServletRequest request) {
    return properties.getDuplicateSubmit().isEnabled()
        && matchPath(properties.getDuplicateSubmit().getPaths(), request.getRequestURI());
  }

  private boolean isSignatureEnabled(HttpServletRequest request) {
    return properties.getSignature().isEnabled()
        && matchPath(properties.getSignature().getPaths(), request.getRequestURI());
  }

  private boolean isRateLimitEnabled(HttpServletRequest request) {
    return properties.getRateLimit().isEnabled()
        && matchPath(properties.getRateLimit().getPaths(), request.getRequestURI());
  }

  private boolean matchPath(Iterable<String> patterns, String path) {
    if (patterns == null) {
      return true;
    }
    boolean hasPattern = false;
    for (String pattern : patterns) {
      hasPattern = true;
      if (pathMatcher.match(pattern, path)) {
        return true;
      }
    }
    return !hasPattern;
  }

  private void verifyDuplicateSubmit(CachedBodyHttpServletRequest request) {
    SecurityProtectionProperties.DuplicateSubmit config = properties.getDuplicateSubmit();
    String token = request.getHeader(config.getTokenHeader());
    String unique = StringUtils.hasText(token) ? token : request.getMethod() + ':' + request.getRequestURI()
        + ':' + nullToEmpty(request.getQueryString());
    if (config.isIncludeBodyHash()) {
      unique += ':' + sha256Hex(request.getCachedBody());
    }
    String key = "duplicate:" + tenantKey() + ':' + unique;
    boolean success = protectionStore.putIfAbsent(key, Math.max(1, config.getExpireSeconds()) * 1000);
    if (!success) {
      throw new SecurityProtectionException(409, "DUPLICATE_SUBMIT", "Duplicate submit rejected");
    }
  }

  private void verifySignature(CachedBodyHttpServletRequest request) {
    SecurityProtectionProperties.Signature config = properties.getSignature();
    String timestamp = requiredHeader(request, config.getTimestampHeader());
    String nonce = requiredHeader(request, config.getNonceHeader());
    String sign = requiredHeader(request, config.getSignHeader());
    String secret = resolveSecret(config, request.getHeader(config.getAppKeyHeader()));

    long timestampMillis;
    try {
      timestampMillis = Long.parseLong(timestamp);
    } catch (NumberFormatException e) {
      throw new SecurityProtectionException(401, "INVALID_TIMESTAMP", "Invalid timestamp");
    }

    long now = System.currentTimeMillis();
    long ttlMillis = Math.max(1, config.getTimestampTtlSeconds()) * 1000;
    if (Math.abs(now - timestampMillis) > ttlMillis) {
      throw new SecurityProtectionException(401, "EXPIRED_TIMESTAMP", "Expired timestamp");
    }

    String nonceKey = "nonce:" + tenantKey() + ':' + nonce;
    if (!protectionStore.putIfAbsent(nonceKey, Math.max(1, config.getNonceTtlSeconds()) * 1000)) {
      throw new SecurityProtectionException(401, "REPLAY_REQUEST", "Replay request rejected");
    }

    String payload = canonicalPayload(request, timestamp, nonce);
    String expected = hmacSha256Hex(secret, payload);
    if (!constantTimeEquals(expected, sign)) {
      throw new SecurityProtectionException(401, "INVALID_SIGNATURE", "Invalid signature");
    }
  }

  private void verifyRateLimit(HttpServletRequest request) {
    SecurityProtectionProperties.RateLimit config = properties.getRateLimit();
    String client = config.isIncludeClientIp() ? ':' + clientIp(request) : "";
    String key = "rate:" + tenantKey() + ':' + request.getMethod() + ':' + request.getRequestURI() + client;
    boolean allowed = protectionStore.allow(key, Math.max(1, config.getPermits()),
        Math.max(1, config.getWindowSeconds()) * 1000);
    if (!allowed) {
      throw new SecurityProtectionException(429, "RATE_LIMITED", "Rate limit exceeded");
    }
  }

  private String canonicalPayload(CachedBodyHttpServletRequest request, String timestamp, String nonce) {
    return request.getMethod().toUpperCase()
        + '\n' + request.getRequestURI()
        + '\n' + nullToEmpty(request.getQueryString())
        + '\n' + sha256Hex(request.getCachedBody())
        + '\n' + timestamp
        + '\n' + nonce
        + '\n' + tenantKey();
  }

  private String resolveSecret(SecurityProtectionProperties.Signature config, String appKey) {
    String tenant = tenantKey();
    String secret = config.getTenantSecrets().get(tenant);
    if (!StringUtils.hasText(secret) && StringUtils.hasText(appKey)) {
      secret = config.getTenantSecrets().get(appKey);
    }
    if (!StringUtils.hasText(secret)) {
      secret = config.getDefaultSecret();
    }
    if (!StringUtils.hasText(secret)) {
      throw new SecurityProtectionException(401, "SIGN_SECRET_MISSING", "Signature secret missing");
    }
    return secret;
  }

  private String requiredHeader(HttpServletRequest request, String headerName) {
    String value = request.getHeader(headerName);
    if (!StringUtils.hasText(value)) {
      throw new SecurityProtectionException(401, "SECURITY_HEADER_MISSING", "Missing header: " + headerName);
    }
    return value;
  }

  private String tenantKey() {
    String tenantCode = RequestHeaderHolder.getTenantCode();
    if (StringUtils.hasText(tenantCode)) {
      return tenantCode;
    }
    String tenantId = RequestHeaderHolder.getTenantId();
    return StringUtils.hasText(tenantId) ? tenantId : TENANT_FALLBACK;
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (StringUtils.hasText(forwarded)) {
      return forwarded.split(",")[0].trim();
    }
    String realIp = request.getHeader("X-Real-IP");
    return StringUtils.hasText(realIp) ? realIp : request.getRemoteAddr();
  }

  private String hmacSha256Hex(String secret, String payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new SecurityProtectionException(500, "SIGNATURE_CALCULATE_ERROR", "Signature calculate error");
    }
  }

  private String sha256Hex(byte[] bytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(bytes));
    } catch (Exception e) {
      throw new SecurityProtectionException(500, "BODY_DIGEST_ERROR", "Body digest error");
    }
  }

  private boolean constantTimeEquals(String expected, String actual) {
    return MessageDigest.isEqual(
        expected.toLowerCase().getBytes(StandardCharsets.UTF_8),
        actual.toLowerCase().getBytes(StandardCharsets.UTF_8));
  }

  private String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private void writeError(HttpServletResponse response, int status, String code, String message)
      throws IOException {
    response.setStatus(status);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    Map<String, Object> body = new HashMap<>();
    body.put("status", status);
    body.put("errorCode", code);
    body.put("message", message);
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }

  private interface HeaderSetter {

    void set(String value);
  }
}
