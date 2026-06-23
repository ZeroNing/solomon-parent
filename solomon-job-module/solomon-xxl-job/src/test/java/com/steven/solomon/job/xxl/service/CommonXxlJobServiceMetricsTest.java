package com.steven.solomon.job.xxl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.job.xxl.entity.XxlJobInfo;
import com.steven.solomon.job.xxl.properties.XxlJobProperties;
import com.steven.solomon.job.xxl.properties.XxlJobRegisterProperties;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CommonXxlJobServiceMetricsTest {

  private HttpServer server;

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void shouldRecordAdminRequestMetrics() throws Exception {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/jobinfo/save", exchange -> {
      byte[] body = "{\"code\":200,\"msg\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, body.length);
      try (OutputStream outputStream = exchange.getResponseBody()) {
        outputStream.write(body);
      }
    });
    server.start();

    XxlJobProperties properties = new XxlJobProperties();
    properties.setAdminAddresses("http://127.0.0.1:" + server.getAddress().getPort());
    TestXxlJobService service = new TestXxlJobService(properties, new XxlJobRegisterProperties());
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    service.setMeterRegistry(registry);

    service.doExecute("jobinfo/save", Collections.singletonMap("executorHandler", "demoJob"));

    assertThat(registry.counter("solomon.job.admin.request.total",
            "provider", "xxl-job",
            "method", "POST",
            "path", "/jobinfo/save",
            "outcome", "success").count())
        .isEqualTo(1);
    assertThat(registry.find("solomon.job.admin.request.duration").timer()).isNotNull();
  }

  @Test
  void shouldRetryAdminRequestWhenConfigured() throws Exception {
    AtomicInteger attempts = new AtomicInteger();
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/jobinfo/save", exchange -> {
      int attempt = attempts.incrementAndGet();
      byte[] body = (attempt == 1 ? "{\"code\":500,\"msg\":\"temporary\"}" : "{\"code\":200,\"msg\":\"ok\"}")
          .getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(attempt == 1 ? 500 : 200, body.length);
      try (OutputStream outputStream = exchange.getResponseBody()) {
        outputStream.write(body);
      }
    });
    server.start();

    XxlJobProperties properties = new XxlJobProperties();
    properties.setAdminAddresses("http://127.0.0.1:" + server.getAddress().getPort());
    XxlJobRegisterProperties registerProperties = new XxlJobRegisterProperties();
    registerProperties.setAdminApiMaxAttempts(2);
    TestXxlJobService service = new TestXxlJobService(properties, registerProperties);

    String body = service.doExecute("jobinfo/save", Collections.singletonMap("executorHandler", "demoJob"));

    assertThat(body).contains("\"code\":200");
    assertThat(attempts).hasValue(2);
  }

  @Test
  void shouldOpenCircuitAfterConfiguredFailures() throws Exception {
    AtomicInteger attempts = new AtomicInteger();
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/jobinfo/save", exchange -> {
      attempts.incrementAndGet();
      byte[] body = "{\"code\":500,\"msg\":\"down\"}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(500, body.length);
      try (OutputStream outputStream = exchange.getResponseBody()) {
        outputStream.write(body);
      }
    });
    server.start();

    XxlJobProperties properties = new XxlJobProperties();
    properties.setAdminAddresses("http://127.0.0.1:" + server.getAddress().getPort());
    XxlJobRegisterProperties registerProperties = new XxlJobRegisterProperties();
    registerProperties.setAdminApiCircuitFailureThreshold(1);
    registerProperties.setAdminApiCircuitOpenDurationMillis(60000);
    TestXxlJobService service = new TestXxlJobService(properties, registerProperties);

    assertThatThrownBy(() -> service.doExecute("jobinfo/save", Collections.emptyMap()))
        .isInstanceOf(BaseException.class);
    assertThatThrownBy(() -> service.doExecute("jobinfo/save", Collections.emptyMap()))
        .isInstanceOf(BaseException.class);

    assertThat(attempts).hasValue(1);
  }

  private static class TestXxlJobService extends CommonXxlJobService {

    private TestXxlJobService(XxlJobProperties profile, XxlJobRegisterProperties registerProperties) {
      super(profile, registerProperties, null);
    }

    private String doExecute(String path, Map<String, Object> params) throws Exception {
      return execute(null, adminAddresses + path, params);
    }
  }
}
