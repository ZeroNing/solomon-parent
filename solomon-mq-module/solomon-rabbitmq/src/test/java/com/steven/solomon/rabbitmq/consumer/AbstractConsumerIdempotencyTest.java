package com.steven.solomon.rabbitmq.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.steven.solomon.mq.idempotency.InMemoryMessageIdempotencyStore;
import com.steven.solomon.mq.idempotency.MessageIdempotencyExecutor;
import com.steven.solomon.rabbitmq.annotation.MessageListener;
import com.steven.solomon.rabbitmq.entity.RabbitMqModel;
import com.steven.solomon.rabbitmq.utils.RabbitUtils;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

class AbstractConsumerIdempotencyTest {

  @Test
  void shouldAckDuplicateMessageWithoutCallingHandlerAgain() throws Exception {
    TestConsumer consumer = new TestConsumer(mock(RabbitUtils.class));
    Channel channel = mock(Channel.class);
    Message message = message("msg-1");

    consumer.onMessage(message, channel);
    consumer.onMessage(message, channel);

    assertThat(consumer.calls).hasValue(1);
    verify(channel, times(2)).basicAck(42L, false);
  }

  private Message message(String msgId) {
    RabbitMqModel<String> model = new RabbitMqModel<>("exchange-a", "route-a", "hello");
    model.setTenantCode("tenant-a");
    model.setMsgId(msgId);
    MessageProperties properties = new MessageProperties();
    properties.setDeliveryTag(42L);
    return new Message(JSONUtil.toJsonStr(model).getBytes(StandardCharsets.UTF_8), properties);
  }

  @MessageListener(queues = "queue-a")
  private static class TestConsumer extends AbstractConsumer<String, String> {

    private final AtomicInteger calls = new AtomicInteger();
    private final MessageIdempotencyExecutor executor =
        new MessageIdempotencyExecutor(new InMemoryMessageIdempotencyStore(),
            Duration.ofMinutes(1), Duration.ofMinutes(1));

    TestConsumer(RabbitUtils rabbitUtils) {
      super(rabbitUtils);
    }

    @Override
    public String handleMessage(String body) {
      calls.incrementAndGet();
      return body;
    }

    @Override
    public void saveLog(String result, Throwable throwable, RabbitMqModel<String> model) {
    }

    @Override
    protected MessageIdempotencyExecutor messageIdempotencyExecutor() {
      return executor;
    }
  }
}
