package com.steven.solomon.service;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.config.NoticeProperties;
import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.enums.NoticeChannelEnum;
import com.steven.solomon.enums.NoticeMsgTypeEnum;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 钉钉机器人通知服务。
 */
@Service
public class DingTalkServiceImpl extends AbstractRobotNoticeService {

    private static final String MARKDOWN = "markdown";

    public DingTalkServiceImpl(NoticeProperties properties) {
        super(properties);
    }

    @Override
    public NoticeChannelEnum getChannel() {
        return NoticeChannelEnum.DING_TALK;
    }

    @Override
    protected String webhookUrl(NoticeMessage message) throws Exception {
        NoticeProperties.DingTalk config = properties.getDingTalk();
        if (config == null || StrUtil.isBlank(config.getWebhookUrl())) {
            return null;
        }
        if (StrUtil.isBlank(config.getSecret())) {
            return config.getWebhookUrl();
        }

        long timestamp = System.currentTimeMillis();
        String content = timestamp + "\n" + config.getSecret();
        String sign = URLEncoder.encode(
                NoticePayloadUtils.hmacSha256Base64(content, config.getSecret()),
                StandardCharsets.UTF_8
        );
        return config.getWebhookUrl() + "&timestamp=" + timestamp + "&sign=" + sign;
    }

    @Override
    protected Map<String, Object> buildRequestBody(NoticeMessage message) {
        NoticeMsgTypeEnum msgType = message.getMsgType() == null ? NoticeMsgTypeEnum.MARKDOWN : message.getMsgType();
        Map<String, Object> body = NoticePayloadUtils.map()
                .put("msgtype", dingTalkMsgType(msgType))
                .build();

        switch (msgType) {
            case TEXT:
                body.put("text", textContent(message));
                break;
            case IMAGE:
            case FILE:
                body.put(msgType.getCode(), NoticePayloadUtils.mediaIdBody(message.getMediaId()));
                break;
            case VOICE:
                body.put("voice", voiceContent(message));
                break;
            case LINK:
                body.put("link", linkContent(message));
                break;
            case ACTION_CARD:
            case CARD:
                body.put("actionCard", actionCardContent(message));
                break;
            case FEED_CARD:
                body.put("feedCard", feedCardContent(message));
                break;
            case MARKDOWN:
                body.put(MARKDOWN, markdownContent(message));
                break;
            default:
                body.put("msgtype", MARKDOWN);
                body.put(MARKDOWN, markdownContent(message));
                break;
        }

        if (msgType == NoticeMsgTypeEnum.TEXT || msgType == NoticeMsgTypeEnum.MARKDOWN) {
            body.put("at", atContent(message));
        }
        return body;
    }

    private String dingTalkMsgType(NoticeMsgTypeEnum msgType) {
        return msgType == NoticeMsgTypeEnum.CARD ? NoticeMsgTypeEnum.ACTION_CARD.getCode() : msgType.getCode();
    }

    private Map<String, Object> textContent(NoticeMessage message) {
        return NoticePayloadUtils.map("content", signature(message.getContent(), "\n> ")).build();
    }

    private Map<String, Object> markdownContent(NoticeMessage message) {
        return NoticePayloadUtils.map()
                .put("title", message.getTitle())
                .put("text", buildMarkdown(message, "\n> "))
                .build();
    }

    private Map<String, Object> atContent(NoticeMessage message) {
        return NoticePayloadUtils.map()
                .putIf(Boolean.TRUE.equals(message.getAtAll()), "isAtAll", true)
                .putIf(NoticePayloadUtils.hasItems(message.getAtUsers()), "atMobiles", message.getAtUsers())
                .build();
    }

    private Map<String, Object> voiceContent(NoticeMessage message) {
        return NoticePayloadUtils.map()
                .put("media_id", message.getMediaId())
                .put("duration", 10)
                .build();
    }

    private Map<String, Object> linkContent(NoticeMessage message) {
        return NoticePayloadUtils.map()
                .put("title", message.getTitle())
                .put("text", message.getContent())
                .put("messageUrl", message.getLinkUrl())
                .put("picUrl", message.getLinkPicUrl())
                .build();
    }

    private Map<String, Object> actionCardContent(NoticeMessage message) {
        Map<String, Object> card = NoticePayloadUtils.map()
                .put("title", message.getTitle())
                .put("text", buildMarkdown(message, "\n> "))
                .put("btn_orientation", "0")
                .build();

        if (!NoticePayloadUtils.hasItems(message.getButtons())) {
            return card;
        }

        if (message.getButtons().size() == 1) {
            NoticeMessage.Button button = message.getButtons().get(0);
            card.put("singleTitle", button.getTitle());
            card.put("singleURL", button.getActionUrl());
            return card;
        }

        List<Map<String, Object>> buttons = new ArrayList<>();
        for (NoticeMessage.Button button : message.getButtons()) {
            buttons.add(NoticePayloadUtils.map()
                    .put("title", button.getTitle())
                    .put("actionURL", button.getActionUrl())
                    .build());
        }
        card.put("btns", buttons);
        return card;
    }

    private Map<String, Object> feedCardContent(NoticeMessage message) {
        List<Map<String, Object>> links = new ArrayList<>();
        if (NoticePayloadUtils.hasItems(message.getFeedItems())) {
            for (NoticeMessage.FeedItem item : message.getFeedItems()) {
                links.add(NoticePayloadUtils.map()
                        .put("title", item.getTitle())
                        .put("messageURL", item.getMessageUrl())
                        .put("picURL", item.getPicUrl())
                        .build());
            }
        }
        return NoticePayloadUtils.map("links", links).build();
    }

    private String buildMarkdown(NoticeMessage message, String signaturePrefix) {
        String content = message.getLevel().getEmoji() + " **" + message.getTitle() + "**\n\n"
                + message.getContent() + "\n\n";
        return signature(content, signaturePrefix);
    }

    private String signature(String content, String prefix) {
        return NoticePayloadUtils.appendSignature(
                content,
                properties.isGlobalSignature(),
                properties.getSignature(),
                prefix
        );
    }
}
