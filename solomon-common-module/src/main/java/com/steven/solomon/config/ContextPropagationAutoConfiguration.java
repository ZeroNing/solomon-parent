package com.steven.solomon.config;

import com.steven.solomon.context.ContextPropagatingTaskDecorator;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.context.TenantRequestBinder;
import com.steven.solomon.filter.ReactiveRequestContextWebFilter;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.server.WebFilter;

@AutoConfiguration
@ConditionalOnProperty(prefix = "solomon.context.propagation", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class ContextPropagationAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public TaskDecorator solomonContextPropagatingTaskDecorator() {
    return new ContextPropagatingTaskDecorator();
  }

  @Bean
  @ConditionalOnClass(WebFilter.class)
  @ConditionalOnMissingBean(ReactiveRequestContextWebFilter.class)
  public ReactiveRequestContextWebFilter solomonReactiveRequestContextWebFilter(
      List<TenantRequestBinder> tenantRequestBinders,
      TenantModeResolver tenantModeResolver) {
    return new ReactiveRequestContextWebFilter(tenantRequestBinders, tenantModeResolver);
  }
}
