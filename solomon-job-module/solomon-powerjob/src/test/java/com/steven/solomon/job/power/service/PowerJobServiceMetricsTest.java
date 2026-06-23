package com.steven.solomon.job.power.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.job.power.properties.PowerJobRegisterProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tech.powerjob.common.enums.Protocol;
import tech.powerjob.worker.autoconfigure.PowerJobProperties;

class PowerJobServiceMetricsTest {

  private HttpServer server;

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void shouldRecordAdminRequestMetrics() throws Exception {
    PowerJobService service = new PowerJobService(new PowerJobRegisterProperties(), powerJobProperties());
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    service.setMeterRegistry(registry);

    Method method = PowerJobService.class.getDeclaredMethod(
        "recordAdminRequest", io.micrometer.core.instrument.Timer.Sample.class, String.class, String.class, String.class);
    method.setAccessible(true);
    method.invoke(service, null, "POST", "/job/save", "success");

    assertThat(registry.counter("solomon.job.admin.request.total",
            "provider", "powerjob",
            "method", "POST",
            "path", "/job/save",
            "outcome", "success").count())
        .isEqualTo(1);
  }

  @Test
  void shouldRetryAdminRequestWhenConfigured() throws Exception {
    AtomicInteger attempts = new AtomicInteger();
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/job/save", exchange -> {
      int attempt = attempts.incrementAndGet();
      byte[] body = (attempt == 1
          ? "{\"success\":false,\"message\":\"temporary\"}"
          : "{\"success\":true,\"data\":{\"id\":1}}").getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(attempt == 1 ? 500 : 200, body.length);
      try (OutputStream outputStream = exchange.getResponseBody()) {
        outputStream.write(body);
      }
    });
    server.start();

    PowerJobRegisterProperties registerProperties = new PowerJobRegisterProperties();
    registerProperties.setAdminApiMaxAttempts(2);
    PowerJobProperties properties = powerJobProperties();
    properties.getWorker().setServerAddress("127.0.0.1:" + server.getAddress().getPort());
    PowerJobService service = new PowerJobService(registerProperties, properties);
    Method execute = PowerJobService.class.getDeclaredMethod("execute", String.class, String.class,
        cn.hutool.http.Method.class, cn.hutool.http.ContentType.class, java.util.Map.class);
    execute.setAccessible(true);

    Object result = execute.invoke(service, null,
        "http://127.0.0.1:" + server.getAddress().getPort() + "/job/save",
        cn.hutool.http.Method.POST, cn.hutool.http.ContentType.JSON, Collections.emptyMap());

    assertThat(result).isEqualTo("{\"id\":1}");
    assertThat(attempts).hasValue(2);
  }

  @Test
  void shouldOpenCircuitAfterConfiguredFailures() throws Exception {
    AtomicInteger attempts = new AtomicInteger();
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/job/save", exchange -> {
      attempts.incrementAndGet();
      byte[] body = "{\"success\":false,\"message\":\"down\"}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(500, body.length);
      try (OutputStream outputStream = exchange.getResponseBody()) {
        outputStream.write(body);
      }
    });
    server.start();

    PowerJobRegisterProperties registerProperties = new PowerJobRegisterProperties();
    registerProperties.setAdminApiCircuitFailureThreshold(1);
    registerProperties.setAdminApiCircuitOpenDurationMillis(60000);
    PowerJobProperties properties = powerJobProperties();
    properties.getWorker().setServerAddress("127.0.0.1:" + server.getAddress().getPort());
    PowerJobService service = new PowerJobService(registerProperties, properties);
    Method execute = PowerJobService.class.getDeclaredMethod("execute", String.class, String.class,
        cn.hutool.http.Method.class, cn.hutool.http.ContentType.class, java.util.Map.class);
    execute.setAccessible(true);

    assertThatThrownBy(() -> execute.invoke(service, null,
        "http://127.0.0.1:" + server.getAddress().getPort() + "/job/save",
        cn.hutool.http.Method.POST, cn.hutool.http.ContentType.JSON, Collections.emptyMap()))
        .hasCauseInstanceOf(BaseException.class);
    assertThatThrownBy(() -> execute.invoke(service, null,
        "http://127.0.0.1:" + server.getAddress().getPort() + "/job/save",
        cn.hutool.http.Method.POST, cn.hutool.http.ContentType.JSON, Collections.emptyMap()))
        .hasCauseInstanceOf(BaseException.class);

    assertThat(attempts).hasValue(1);
  }

  private PowerJobProperties powerJobProperties() {
    PowerJobProperties properties = new PowerJobProperties();
    properties.getWorker().setProtocol(Protocol.HTTP);
    properties.getWorker().setServerAddress("127.0.0.1:7700");
    properties.getWorker().setAppName("demo-app");
    return properties;
  }
}
