package com.steven.solomon.context;

import org.springframework.core.task.TaskDecorator;

public class ContextPropagatingTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    return RequestContextSnapshot.capture().wrap(runnable);
  }
}
