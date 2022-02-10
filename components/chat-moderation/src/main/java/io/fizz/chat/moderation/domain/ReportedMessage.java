package io.fizz.chat.moderation.domain;

import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainError;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;

import java.util.Objects;

public class ReportedMessage {
    private final String id;
    private final ApplicationId appId;
    private final UserId reportedUserId;
    private final UserId reporterUserId;
    private final String message;
    private final ReportOffense offense;
    private final ChannelId channelId;
    private final String messageId;
    private final LanguageCode language;
    private final String description;
    private final Long time;

    public ReportedMessage(String id,
                           ApplicationId appId,
                           UserId reporterUserId,
                           UserId reportedUserId,
                           String message,
                           String messageId,
                           ChannelId channelId,
                           LanguageCode language,
                           ReportOffense offense,
                           String description,
                           Long time) throws DomainErrorException {
        Utils.assertRequiredArgument(id, "invalid_id");
        Utils.assertRequiredArgument(appId, "invalid_app_id");
        Utils.assertRequiredArgument(reporterUserId, "invalid_reporter_user_id");
        Utils.assertRequiredArgument(reportedUserId, "invalid_reported_user_id");
        Utils.assertRequiredArgument(message, "invalid_message");
        Utils.assertRequiredArgument(messageId, "invalid_message_id");
        Utils.assertRequiredArgument(channelId, "invalid_channel_id");
        Utils.assertRequiredArgument(language, "invalid_language");
        Utils.assertRequiredArgument(offense, "invalid_offense");
        Utils.assertRequiredArgument(time, "invalid_time");

        if (Objects.equals(reportedUserId.value(), reporterUserId.value())) {
            throw new DomainErrorException(new DomainError("invalid_reporter_user_id"));
        }

        if (time < 0) {
            throw new IllegalArgumentException("invalid_time");
        }

        this.id = id;
        this.appId = appId;
        this.reporterUserId = reporterUserId;
        this.reportedUserId = reportedUserId;
        this.message = message;
        this.messageId = messageId;
        this.channelId = channelId;
        this.offense = offense;
        this.language = language;
        this.description = description;
        this.time = time;
    }

    public String id() {
        return id;
    }

    public ApplicationId appId() {
        return appId;
    }

    public UserId reportedUserId() {
        return reportedUserId;
    }

    public UserId reporterUserId() {
        return reporterUserId;
    }

    public String message() {
        return message;
    }

    public ReportOffense offense() {
        return offense;
    }

    public ChannelId channelId() {
        return channelId;
    }

    public String messageId() {
        return messageId;
    }

    public LanguageCode language() {
        return language;
    }

    public String description() {
        return description;
    }

    public Long time() {
        return time;
    }
}
