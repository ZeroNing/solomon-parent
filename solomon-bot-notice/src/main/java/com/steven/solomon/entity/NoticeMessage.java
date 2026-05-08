package com.steven.solomon.entity;

import com.steven.solomon.enums.NoticeChannelEnum;
import com.steven.solomon.enums.NoticeLevelEnum;
import com.steven.solomon.enums.NoticeMsgTypeEnum;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 统一通知消息模型
 * 所有平台的消息都使用这个模型，内部自动适配不同平台的格式
 */
public class NoticeMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知渠道列表（必传）
     * 支持同时发送到多个渠道：List.of(WECHAT_WORK, DING_TALK, FEISHU)
     */
    private List<NoticeChannelEnum> channels;

    /**
     * 通知级别，默认：INFO
     * 可选值：INFO(普通)/WARN(警告)/ERROR(错误)/SUCCESS(成功)
     * 不同级别会显示不同的图标和颜色
     */
    private NoticeLevelEnum level = NoticeLevelEnum.INFO;

    /**
     * 消息类型，默认：MARKDOWN
     * 可选值：TEXT/MARKDOWN/LINK/IMAGE/FILE/ACTION_CARD/FEED_CARD
     */
    private NoticeMsgTypeEnum msgType = NoticeMsgTypeEnum.MARKDOWN;

    /**
     * 消息标题（必传）
     * 所有消息类型都需要标题
     */
    private String title;

    /**
     * 消息内容（必传）
     * 支持Markdown格式（MARKDOWN类型自动解析）
     */
    private String content;

    /**
     * 模板代码（可选，预留字段）
     */
    private String templateCode;

    /**
     * 模板参数（可选，预留字段）
     */
    private Map<String, Object> templateParams;

    /**
     * 接收人列表（可选，预留字段）
     */
    private List<String> receivers;

    /**
     * 是否异步发送，默认：true
     * 异步发送不会阻塞业务线程，发送结果会打印日志
     * 同步发送会等待发送完成后返回
     */
    private boolean async = true;

    /**
     * 失败重试次数，默认：3次
     * 发送失败时自动重试的次数
     */
    private int retryCount = 3;

    /**
     * 租户标识（可选，多租户场景使用）
     */
    private String tenantCode;

    /**
     * 是否@所有人（可选，优先级高于全局配置）
     * null：使用全局配置的atAll值
     * true：本次消息@所有人
     * false：本次消息不@所有人
     */
    private Boolean atAll;

    /**
     * @用户列表（可选，优先级高于全局配置）
     * 企业微信：填写用户userid
     * 钉钉：填写用户手机号或userid
     * 飞书：填写用户open_id（ou_xxxxxx）
     */
    private List<String> atUsers;

    // ==================== 各类型消息专属字段 ====================
    /**
     * 链接跳转URL（LINK类型必传）
     * 用户点击消息时跳转到的地址
     */
    private String linkUrl;

    /**
     * 链接封面图URL（LINK类型可选）
     * 链接消息显示的封面图片
     */
    private String linkPicUrl;

    /**
     * 媒体资源ID（IMAGE/FILE类型必传）
     * 企业微信/钉钉：上传文件后得到的media_id
     * 飞书：上传文件后得到的image_key/file_key
     */
    private String mediaId;

    /**
     * 卡片按钮列表（ACTION_CARD类型可选）
     * 交互卡片的按钮配置，支持多个按钮
     */
    private List<Button> buttons;

    /**
     * Feed流条目列表（FEED_CARD类型必传，钉钉专属）
     * 多图文消息的条目配置
     */
    private List<FeedItem> feedItems;

    /**
     * 扩展参数（可选）
     * 用于传递平台特殊参数
     */
    private Map<String, Object> extParams;

    // ==================== 构造方法 ====================

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

    // ==================== 内部类 ====================
    /**
     * 交互卡片按钮配置
     * 用于ACTION_CARD类型消息
     */
    public static class Button {
        /**
         * 按钮显示文字
         */
        private String title;
        /**
         * 按钮点击跳转URL
         */
        private String actionUrl;

        public Button() {}

        /**
         * 构造方法
         * @param title 按钮文字
         * @param actionUrl 跳转链接
         */
        public Button(String title, String actionUrl) {
            this.title = title;
            this.actionUrl = actionUrl;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    }

    /**
     * Feed流条目配置
     * 用于FEED_CARD类型消息（钉钉专属）
     */
    public static class FeedItem {
        /**
         * 条目标题
         */
        private String title;
        /**
         * 条目点击跳转URL
         */
        private String messageUrl;
        /**
         * 条目封面图片URL
         */
        private String picUrl;

        public FeedItem() {}

        /**
         * 构造方法
         * @param title 条目标题
         * @param messageUrl 跳转链接
         * @param picUrl 封面图片
         */
        public FeedItem(String title, String messageUrl, String picUrl) {
            this.title = title;
            this.messageUrl = messageUrl;
            this.picUrl = picUrl;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessageUrl() { return messageUrl; }
        public void setMessageUrl(String messageUrl) { this.messageUrl = messageUrl; }
        public String getPicUrl() { return picUrl; }
        public void setPicUrl(String picUrl) { this.picUrl = picUrl; }
    }

    // ==================== Getter & Setter ====================

    public List<NoticeChannelEnum> getChannels() {
        return channels;
    }

    public void setChannels(List<NoticeChannelEnum> channels) {
        this.channels = channels;
    }

    public NoticeLevelEnum getLevel() {
        return level;
    }

    public void setLevel(NoticeLevelEnum level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Map<String, Object> getTemplateParams() {
        return templateParams;
    }

    public void setTemplateParams(Map<String, Object> templateParams) {
        this.templateParams = templateParams;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public Map<String, Object> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, Object> extParams) {
        this.extParams = extParams;
    }

    public NoticeMsgTypeEnum getMsgType() {
        return msgType;
    }

    public void setMsgType(NoticeMsgTypeEnum msgType) {
        this.msgType = msgType;
    }

    public Boolean getAtAll() {
        return atAll;
    }

    public void setAtAll(Boolean atAll) {
        this.atAll = atAll;
    }

    public List<String> getAtUsers() {
        return atUsers;
    }

    public void setAtUsers(List<String> atUsers) {
        this.atUsers = atUsers;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getLinkPicUrl() {
        return linkPicUrl;
    }

    public void setLinkPicUrl(String linkPicUrl) {
        this.linkPicUrl = linkPicUrl;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public void setButtons(List<Button> buttons) {
        this.buttons = buttons;
    }

    public List<FeedItem> getFeedItems() {
        return feedItems;
    }

    public void setFeedItems(List<FeedItem> feedItems) {
        this.feedItems = feedItems;
    }
}
