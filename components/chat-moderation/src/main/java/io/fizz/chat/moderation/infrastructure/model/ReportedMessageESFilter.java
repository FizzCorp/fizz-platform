package io.fizz.chat.moderation.infrastructure.model;

import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.QueryRange;
import io.fizz.common.domain.UserId;

import java.util.Objects;

public class ReportedMessageESFilter {
    private boolean filterPassed = true;
    private final ReportedMessageES messageES;

    public ReportedMessageESFilter(ReportedMessageES messageES) {
        this.messageES = messageES;
    }

    public ReportedMessageESFilter filter(final ApplicationId aAppId) {
        filterPassed = filterPassed && (Objects.isNull(aAppId) || aAppId.value().equals(messageES.getAppId()));
        return this;
    }

    public ReportedMessageESFilter filter(final UserId aUserId) {
        filterPassed = filterPassed && (Objects.isNull(aUserId) || aUserId.value().equals(messageES.getReportedUserId()));
        return this;
    }

    public ReportedMessageESFilter filter(final ChannelId aChannelId) {
        filterPassed = filterPassed && (Objects.isNull(aChannelId) || aChannelId.value().equals(messageES.getChannelId()));
        return this;
    }

    public ReportedMessageESFilter filter(final LanguageCode aLang) {
        filterPassed = filterPassed && (Objects.isNull(aLang) || aLang.value().equals(messageES.getLanguage()));
        return this;
    }

    public ReportedMessageESFilter filter(final QueryRange aRange) {
        filterPassed = filterPassed && (aRange.getStart() <= messageES.getTimestamp() / 1000 && aRange.getEnd() >= messageES.getTimestamp() / 1000);
        return this;
    }

    public ReportedMessageES get() {
        return filterPassed ? messageES : null;
    }
}
