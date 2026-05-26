package com.steven.solomon.entity;

import com.steven.solomon.enums.NoticeChannelEnum;
import com.steven.solomon.enums.NoticeLevelEnum;
import com.steven.solomon.enums.NoticeMsgTypeEnum;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 统一通知消息模型。
 *
 * <p>业务侧只需要描述“发给谁、发什么、用什么类型发”，具体平台报文由各机器人服务适配。</p>
 */
public class NoticeMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通知渠道，支持一次投递到多个机器人。 */
    private List<NoticeChannelEnum> channels;

    /** 通知级别，默认普通消息。 */
    private NoticeLevelEnum level = NoticeLevelEnum.INFO;

    /** 消息类型，默认 Markdown。 */
    private NoticeMsgTypeEnum msgType = NoticeMsgTypeEnum.MARKDOWN;

    /** 消息标题。 */
    private String title;

    /** 消息正文。 */
    private String content;

    /** 模板编码，预留给业务侧模板引擎扩展。 */
    private String templateCode;

    /** 模板参数，预留给业务侧模板引擎扩展。 */
    private Map<String, Object> templateParams;

    /** 接收人列表，预留给业务侧精准投递扩展。 */
    private List<String> receivers;

    /** 是否异步发送。 */
    private boolean async = true;

    /** 异步发送失败重试次数。 */
    private int retryCount = 3;

    /** 租户编码，预留给多租户场景。 */
    private String tenantCode;

    /** 是否提醒所有人，优先级高于全局配置。 */
    private Boolean atAll;

    /** 被提醒用户列表，各平台按自己的用户标识解释。 */
    private List<String> atUsers;

    /** 链接消息跳转地址。 */
    private String linkUrl;

    /** 链接消息封面图地址或平台图片标识。 */
    private String linkPicUrl;

    /** 图片、文件、语音等媒体消息的平台资源标识。 */
    private String mediaId;

    /** 图片 Base64 内容，企业微信群机器人图片消息使用。 */
    private String imageBase64;

    /** 图片 MD5，企业微信群机器人图片消息使用。 */
    private String imageMd5;

    /** 文件名，文件类消息或降级展示时使用。 */
    private String fileName;

    /** 语音时长，单位秒。 */
    private Integer duration;

    /** 群名片 ID，飞书 share_chat 消息使用。 */
    private String shareChatId;

    /** 交互卡片按钮。 */
    private List<Button> buttons;

    /** Feed 流卡片条目。 */
    private List<FeedItem> feedItems;

    /** 平台扩展参数。 */
    private Map<String, Object> extParams;

    public NoticeMessage() {
    }

    public NoticeMessage(NoticeChannelEnum channel, String title, String content, List<String> receivers) {
        this.channels = List.of(channel);
        this.title = title;
        this.content = content;
        this.receivers = receivers;
    }

    public NoticeMessage(NoticeChannelEnum channel, String title, String content, Boolean atAll, List<String> atUsers) {
        this.channels = List.of(channel);
        this.title = title;
        this.content = content;
        this.atAll = atAll;
        this.atUsers = atUsers;
    }

    public static NoticeMessage create() {
        return new NoticeMessage();
    }

    public static NoticeMessage of(NoticeChannelEnum channel, String title, String content) {
        return create().channel(channel).title(title).content(content);
    }

    public NoticeMessage channel(NoticeChannelEnum channel) {
        this.channels = List.of(channel);
        return this;
    }

    public NoticeMessage channels(List<NoticeChannelEnum> channels) {
        this.channels = channels;
        return this;
    }

    public NoticeMessage level(NoticeLevelEnum level) {
        if (level != null) {
            this.level = level;
        }
        return this;
    }

    public NoticeMessage msgType(NoticeMsgTypeEnum msgType) {
        if (msgType != null) {
            this.msgType = msgType;
        }
        return this;
    }

    public NoticeMessage title(String title) {
        this.title = title;
        return this;
    }

    public NoticeMessage content(String content) {
        this.content = content;
        return this;
    }

    public NoticeMessage template(String templateCode, Map<String, Object> templateParams) {
        this.templateCode = templateCode;
        this.templateParams = templateParams;
        return this;
    }

    public NoticeMessage receivers(List<String> receivers) {
        this.receivers = receivers;
        return this;
    }

    public NoticeMessage async(boolean async) {
        this.async = async;
        return this;
    }

    public NoticeMessage retryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public NoticeMessage tenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
        return this;
    }

    public NoticeMessage at(Boolean atAll, List<String> atUsers) {
        this.atAll = atAll;
        this.atUsers = atUsers;
        return this;
    }

    public NoticeMessage link(String linkUrl, String linkPicUrl) {
        this.linkUrl = linkUrl;
        this.linkPicUrl = linkPicUrl;
        return this;
    }

    public NoticeMessage mediaId(String mediaId) {
        this.mediaId = mediaId;
        return this;
    }

    public NoticeMessage image(String mediaId) {
        return msgType(NoticeMsgTypeEnum.IMAGE).mediaId(mediaId);
    }

    public NoticeMessage image(String imageBase64, String imageMd5) {
        this.msgType = NoticeMsgTypeEnum.IMAGE;
        this.imageBase64 = imageBase64;
        this.imageMd5 = imageMd5;
        return this;
    }

    public NoticeMessage file(String mediaId, String fileName) {
        this.msgType = NoticeMsgTypeEnum.FILE;
        this.mediaId = mediaId;
        this.fileName = fileName;
        return this;
    }

    public NoticeMessage voice(String mediaId, Integer duration) {
        this.msgType = NoticeMsgTypeEnum.VOICE;
        this.mediaId = mediaId;
        this.duration = duration;
        return this;
    }

    public NoticeMessage shareChat(String shareChatId) {
        this.msgType = NoticeMsgTypeEnum.SHARE_CHAT;
        this.shareChatId = shareChatId;
        return this;
    }

    public NoticeMessage buttons(List<Button> buttons) {
        this.buttons = buttons;
        return this;
    }

    public NoticeMessage feedItems(List<FeedItem> feedItems) {
        this.feedItems = feedItems;
        return this;
    }

    public NoticeMessage extParams(Map<String, Object> extParams) {
        this.extParams = extParams;
        return this;
    }

    /**
     * 交互卡片按钮配置。
     */
    public static class Button implements Serializable {

        private static final long serialVersionUID = 1L;

        private String title;

        private String actionUrl;

        public Button() {
        }

        public Button(String title, String actionUrl) {
            this.title = title;
            this.actionUrl = actionUrl;
        }

        public static Button of(String title, String actionUrl) {
            return new Button().title(title).actionUrl(actionUrl);
        }

        public Button title(String title) {
            this.title = title;
            return this;
        }

        public Button actionUrl(String actionUrl) {
            this.actionUrl = actionUrl;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Button setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getActionUrl() {
            return actionUrl;
        }

        public Button setActionUrl(String actionUrl) {
            this.actionUrl = actionUrl;
            return this;
        }
    }

    /**
     * Feed 流卡片条目配置。
     */
    public static class FeedItem implements Serializable {

        private static final long serialVersionUID = 1L;

        private String title;

        private String messageUrl;

        private String picUrl;

        public FeedItem() {
        }

        public FeedItem(String title, String messageUrl, String picUrl) {
            this.title = title;
            this.messageUrl = messageUrl;
            this.picUrl = picUrl;
        }

        public static FeedItem of(String title, String messageUrl, String picUrl) {
            return new FeedItem().title(title).messageUrl(messageUrl).picUrl(picUrl);
        }

        public FeedItem title(String title) {
            this.title = title;
            return this;
        }

        public FeedItem messageUrl(String messageUrl) {
            this.messageUrl = messageUrl;
            return this;
        }

        public FeedItem picUrl(String picUrl) {
            this.picUrl = picUrl;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public FeedItem setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getMessageUrl() {
            return messageUrl;
        }

        public FeedItem setMessageUrl(String messageUrl) {
            this.messageUrl = messageUrl;
            return this;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public FeedItem setPicUrl(String picUrl) {
            this.picUrl = picUrl;
            return this;
        }
    }

    public List<NoticeChannelEnum> getChannels() {
        return channels;
    }

    public NoticeMessage setChannels(List<NoticeChannelEnum> channels) {
        return channels(channels);
    }

    public NoticeLevelEnum getLevel() {
        return level;
    }

    public NoticeMessage setLevel(NoticeLevelEnum level) {
        return level(level);
    }

    public NoticeMsgTypeEnum getMsgType() {
        return msgType;
    }

    public NoticeMessage setMsgType(NoticeMsgTypeEnum msgType) {
        return msgType(msgType);
    }

    public String getTitle() {
        return title;
    }

    public NoticeMessage setTitle(String title) {
        return title(title);
    }

    public String getContent() {
        return content;
    }

    public NoticeMessage setContent(String content) {
        return content(content);
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public NoticeMessage setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
        return this;
    }

    public Map<String, Object> getTemplateParams() {
        return templateParams;
    }

    public NoticeMessage setTemplateParams(Map<String, Object> templateParams) {
        this.templateParams = templateParams;
        return this;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public NoticeMessage setReceivers(List<String> receivers) {
        return receivers(receivers);
    }

    public boolean isAsync() {
        return async;
    }

    public NoticeMessage setAsync(boolean async) {
        return async(async);
    }

    public int getRetryCount() {
        return retryCount;
    }

    public NoticeMessage setRetryCount(int retryCount) {
        return retryCount(retryCount);
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public NoticeMessage setTenantCode(String tenantCode) {
        return tenantCode(tenantCode);
    }

    public Boolean getAtAll() {
        return atAll;
    }

    public NoticeMessage setAtAll(Boolean atAll) {
        this.atAll = atAll;
        return this;
    }

    public List<String> getAtUsers() {
        return atUsers;
    }

    public NoticeMessage setAtUsers(List<String> atUsers) {
        this.atUsers = atUsers;
        return this;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public NoticeMessage setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
        return this;
    }

    public String getLinkPicUrl() {
        return linkPicUrl;
    }

    public NoticeMessage setLinkPicUrl(String linkPicUrl) {
        this.linkPicUrl = linkPicUrl;
        return this;
    }

    public String getMediaId() {
        return mediaId;
    }

    public NoticeMessage setMediaId(String mediaId) {
        return mediaId(mediaId);
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public NoticeMessage setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
        return this;
    }

    public String getImageMd5() {
        return imageMd5;
    }

    public NoticeMessage setImageMd5(String imageMd5) {
        this.imageMd5 = imageMd5;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public NoticeMessage setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public Integer getDuration() {
        return duration;
    }

    public NoticeMessage setDuration(Integer duration) {
        this.duration = duration;
        return this;
    }

    public String getShareChatId() {
        return shareChatId;
    }

    public NoticeMessage setShareChatId(String shareChatId) {
        this.shareChatId = shareChatId;
        return this;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public NoticeMessage setButtons(List<Button> buttons) {
        return buttons(buttons);
    }

    public List<FeedItem> getFeedItems() {
        return feedItems;
    }

    public NoticeMessage setFeedItems(List<FeedItem> feedItems) {
        return feedItems(feedItems);
    }

    public Map<String, Object> getExtParams() {
        return extParams;
    }

    public NoticeMessage setExtParams(Map<String, Object> extParams) {
        return extParams(extParams);
    }
}
