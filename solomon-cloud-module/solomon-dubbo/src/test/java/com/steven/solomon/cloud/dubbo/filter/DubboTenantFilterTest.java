package com.steven.solomon.cloud.dubbo.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DubboTenantFilterTest {

  @AfterEach
  void tearDown() {
    ExceptionUtil.requestId.remove();
    RequestHeaderHolder.remove();
  }

  @Test
  void consumerShouldWriteRequestContextAttachments() {
    RpcContext.getServiceContext().setUrl(URL.valueOf("dubbo://127.0.0.1/demo?side=consumer"));
    Invocation invocation = mock(Invocation.class);
    @SuppressWarnings("unchecked")
    Invoker<Object> invoker = mock(Invoker.class);
    Result result = mock(Result.class);
    when(invoker.invoke(invocation)).thenReturn(result);
    ExceptionUtil.requestId.set("request-dubbo");
    RequestHeaderHolder.setTimeZone("Asia/Shanghai");
    RequestHeaderHolder.setTenantCode("tenant-dubbo");
    RequestHeaderHolder.setTenantId("tenant-id");
    RequestHeaderHolder.setTenantName("tenant-name");

    new DubboTenantFilter().invoke(invoker, invocation);

    verify(invocation).setAttachment(BaseCode.REQUEST_ID, "request-dubbo");
    verify(invocation).setAttachment(BaseCode.TIMEZONE, "Asia/Shanghai");
    verify(invocation).setAttachment(BaseCode.TENANT_CODE, "tenant-dubbo");
    verify(invocation).setAttachment(BaseCode.TENANT_ID, "tenant-id");
    verify(invocation).setAttachment(BaseCode.TENANT_NAME, "tenant-name");
  }
}
