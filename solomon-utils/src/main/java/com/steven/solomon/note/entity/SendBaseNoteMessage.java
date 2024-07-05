package com.steven.solomon.note.entity;

import java.io.Serializable;
import java.util.List;

public class SendBaseNoteMessage implements Serializable {

    /**
     * 需要@的人
     */
    private SendAtMessage at;

    /**
     * 发送链接
     */
    private SendLinkMessage link;

    /**
     * 发送普通文本
     */
    private SendTextMessage text;

    /**
     * 发送markdown消息
     */
    private SendMarkdownMessage markdown;

    public SendBaseNoteMessage() {
        super();
    }

    public SendBaseNoteMessage(SendAtMessage at) {
        super();
        this.at = at;
    }

    public SendBaseNoteMessage(SendLinkMessage link) {
        super();
        this.link = link;
    }

    public SendBaseNoteMessage(SendTextMessage text) {
        super();
        this.text = text;
    }

    public SendBaseNoteMessage(SendTextMessage text,SendAtMessage at) {
        super();
        this.text = text;
        this.at = at;
    }

    public SendBaseNoteMessage(SendMarkdownMessage markdown) {
        super();
        this.markdown = markdown;
    }

    public static class SendAtMessage implements Serializable {
        /**
         * 被@人的手机号
         */
        private String at;

        private List<String> atMobiles;
        /**
         * 被@人的工号
         */
        private List<String> atUserIds;
        /**
         * @所有人时:true,否则为:false
         */
        private boolean isAtAll = false;

        public SendAtMessage(){
            super();
        }

        public SendAtMessage(String at,List<String> atMobiles,List<String> atUserIds,boolean isAtAll) {
            this.at = at;
            this.atMobiles = atMobiles;
            this.atUserIds = atUserIds;
            this.isAtAll = isAtAll;
        }

        public String getAt() {
            return at;
        }

        public void setAt(String at) {
            this.at = at;
        }

        public List<String> getAtMobiles() {
            return atMobiles;
        }

        public void setAtMobiles(List<String> atMobiles) {
            this.atMobiles = atMobiles;
        }

        public List<String> getAtUserIds() {
            return atUserIds;
        }

        public void setAtUserIds(List<String> atUserIds) {
            this.atUserIds = atUserIds;
        }

        public boolean getAtAll() {
            return isAtAll;
        }

        public void setAtAll(boolean atAll) {
            isAtAll = atAll;
        }
    }

    public static class SendLinkMessage implements Serializable{
        /**
         * 点击消息跳转的URL
         */
        private String messageUrl;
        /**
         * 图片URL
         */
        private String picUrl;
        /**
         * 消息内容。如果太长只会部分展示
         */
        private String text;
        /**
         * 消息标题
         */
        private String title;

        public SendLinkMessage(){
            super();
        }

        public SendLinkMessage(String messageUrl, String picUrl, String text, String title) {
            this.messageUrl = messageUrl;
            this.picUrl = picUrl;
            this.text = text;
            this.title = title;
        }

        public String getMessageUrl() {
            return messageUrl;
        }

        public void setMessageUrl(String messageUrl) {
            this.messageUrl = messageUrl;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static class SendTextMessage implements Serializable{

        /**
         * 消息
         */
        private String message;

        public SendTextMessage(){
            super();
        }

        public SendTextMessage(String message){
            super();
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class SendMarkdownMessage implements Serializable {
        /**
         * markdown格式的消息
         */
        private String text;
        /**
         * 首屏会话透出的展示内容
         */
        private String title;

        public SendMarkdownMessage(){
            super();
        }

        public SendMarkdownMessage(String text, String title){
            this.text = text;
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public SendAtMessage getAt() {
        return at;
    }

    public void setAt(SendAtMessage at) {
        this.at = at;
    }

    public SendLinkMessage getLink() {
        return link;
    }

    public void setLink(SendLinkMessage link) {
        this.link = link;
    }

    public SendTextMessage getText() {
        return text;
    }

    public void setText(SendTextMessage text) {
        this.text = text;
    }

    public SendMarkdownMessage getMarkdown() {
        return markdown;
    }

    public void setMarkdown(SendMarkdownMessage markdown) {
        this.markdown = markdown;
    }
}
