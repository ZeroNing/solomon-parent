package com.steven.solomon.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.steven.solomon.config.NoticeProperties;
import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.enums.NoticeChannelEnum;
import com.steven.solomon.service.NoticeService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

class NoticeUtilsMetricsTest {

    @Test
    void shouldRecordSendMetrics() throws Exception {
        NoticeProperties properties = new NoticeProperties();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        NoticeUtils noticeUtils = new NoticeUtils(
                List.of(new StubNoticeService(true)),
                properties,
                meterRegistry);

        noticeUtils.send(NoticeMessage.of(NoticeChannelEnum.DING_TALK, "title", "content").async(false));

        assertThat(meterRegistry.counter(
                "solomon.notice.send.total",
                "channel", "DING_TALK",
                "async", "false",
                "outcome", "success").count()).isEqualTo(1.0);
        assertThat(meterRegistry.timer(
                "solomon.notice.send.duration",
                "channel", "DING_TALK",
                "async", "false",
                "outcome", "success").count()).isEqualTo(1);
    }

    private static class StubNoticeService implements NoticeService {

        private final boolean success;

        private StubNoticeService(boolean success) {
            this.success = success;
        }

        @Override
        public NoticeChannelEnum getChannel() {
            return NoticeChannelEnum.DING_TALK;
        }

        @Override
        public boolean send(NoticeMessage message) {
            return success;
        }
    }
}
