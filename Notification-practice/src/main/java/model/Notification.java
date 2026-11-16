package model;

import enums.ChannelType;

public class Notification {
    String id;
    ChannelType channelType;
    String subject;
    String body;
    String recipientId;

    public Notification(String id, ChannelType channelType, String subject, String body, String recipientId) {
        this.id = id;
        this.channelType = channelType;
        this.subject = subject;
        this.body = body;
        this.recipientId = recipientId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }
}
