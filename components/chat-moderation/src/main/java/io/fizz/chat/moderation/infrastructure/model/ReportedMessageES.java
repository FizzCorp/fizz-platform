package io.fizz.chat.moderation.infrastructure.model;

import io.fizz.common.Utils;

public class ReportedMessageES {
    private String id;
    private String appId;
    private String reporterUserId;
    private String reportedUserId;
    private String messageId;
    private String message;
    private String channelId;
    private String language;
    private String offense;
    private String description;
    private long timestamp;

    public ReportedMessageES(String id,
                             String appId,
                             String reporterUserId,
                             String reportedUserId,
                             String message,
                             String messageId,
                             String channelId,
                             String language,
                             String offense,
                             String description,
                             long timestamp) {
        Utils.assertRequiredArgument(id, "invalid_id");
        Utils.assertRequiredArgument(appId, "invalid_app_id");
        Utils.assertRequiredArgument(reporterUserId, "invalid_reporter_user_id");
        Utils.assertRequiredArgument(reportedUserId, "invalid_reported_user_id");
        Utils.assertRequiredArgument(message, "invalid_message");
        Utils.assertRequiredArgument(messageId, "invalid_message_id");
        Utils.assertRequiredArgument(channelId, "invalid_channel_id");
        Utils.assertRequiredArgument(offense, "invalid_offense");
        Utils.assertArgumentRange(timestamp, 0, Long.MAX_VALUE, "invalid_timestamp");

        this.id = id;
        this.appId = appId;
        this.reportedUserId = reportedUserId;
        this.reporterUserId = reporterUserId;
        this.message = message;
        this.messageId = messageId;
        this.channelId = channelId;
        this.offense = offense;
        this.language = language;
        this.description = description;
        this.timestamp = timestamp*1000;
    }

    public ReportedMessageES() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReporterUserId() {
        return reporterUserId;
    }

    public void setReporterUserId(String reporterUserId) {
        this.reporterUserId = reporterUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOffense() {
        return offense;
    }

    public void setOffense(String offense) {
        this.offense = offense;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
