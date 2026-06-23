package com.steven.solomon.mq.autoconfigure;

import com.steven.solomon.mq.idempotency.InMemoryMessageIdempotencyStore;
import com.steven.solomon.mq.idempotency.MessageIdempotencyExecutor;
import com.steven.solomon.mq.idempotency.MessageIdempotencyStore;
import com.steven.solomon.mq.properties.MqIdempotencyProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MqIdempotencyProperties.class)
@ConditionalOnProperty(prefix = "solomon.mq.idempotency", name = "enabled", havingValue = "true")
public class MqIdempotencyAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public MessageIdempotencyStore messageIdempotencyStore() {
    return new InMemoryMessageIdempotencyStore();
  }

  @Bean
  @ConditionalOnMissingBean
  public MessageIdempotencyExecutor messageIdempotencyExecutor(
      MessageIdempotencyStore store,
      MqIdempotencyProperties properties) {
    return new MessageIdempotencyExecutor(
        store,
        properties.getProcessingTtl(),
        properties.getConsumedTtl());
  }
}
