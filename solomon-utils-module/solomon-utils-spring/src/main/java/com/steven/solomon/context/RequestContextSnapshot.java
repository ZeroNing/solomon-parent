package com.steven.solomon.context;

import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeader;
import com.steven.solomon.holder.RequestHeaderHolder;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class RequestContextSnapshot {

  private final String requestId;
  private final RequestHeader requestHeader;

  private RequestContextSnapshot(String requestId, RequestHeader requestHeader) {
    this.requestId = requestId;
    this.requestHeader = requestHeader;
  }

  public static RequestContextSnapshot capture() {
    return new RequestContextSnapshot(ExceptionUtil.requestId.get(), RequestHeaderHolder.snapshot());
  }

  public String getRequestId() {
    return requestId;
  }

  public RequestHeader getRequestHeader() {
    return requestHeader;
  }

  public Runnable wrap(Runnable task) {
    return () -> run(task);
  }

  public <T> Callable<T> wrap(Callable<T> task) {
    return () -> call(task);
  }

  public <T> Supplier<T> wrap(Supplier<T> supplier) {
    return () -> get(supplier);
  }

  public void run(Runnable task) {
    RequestContextSnapshot previous = capture();
    try {
      restore();
      task.run();
    } finally {
      previous.restore();
    }
  }

  public <T> T call(Callable<T> task) throws Exception {
    RequestContextSnapshot previous = capture();
    try {
      restore();
      return task.call();
    } finally {
      previous.restore();
    }
  }

  public <T> T get(Supplier<T> supplier) {
    RequestContextSnapshot previous = capture();
    try {
      restore();
      return supplier.get();
    } finally {
      previous.restore();
    }
  }

  public void restore() {
    if (requestId == null) {
      ExceptionUtil.requestId.remove();
    } else {
      ExceptionUtil.requestId.set(requestId);
    }
    RequestHeaderHolder.restore(requestHeader);
  }
}
