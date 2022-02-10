package io.fizz.chat.domain.channel;

import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.CountryCode;
import io.fizz.common.domain.Platform;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.Objects;

public class ChannelMessageTranslated implements AbstractDomainEvent {
    public static class Builder {
        private String id;
        private CountryCode countryCode;
        private ApplicationId appId;
        private UserId userId;
        private String sessionId;
        private long occurredOn = -1;
        private Platform platform;
        private String build;
        private String custom01;
        private String custom02;
        private String custom03;
        private String messageId;
        private String channelId;
        private LanguageCode from;
        private LanguageCode[] to;
        private int length;

        public ChannelMessageTranslated get() throws IllegalArgumentException {
            return new ChannelMessageTranslated(
                    id, appId, countryCode, userId, sessionId,
                    occurredOn, platform, build, custom01, custom02, custom03,
                    channelId, messageId, from, to, length
            );
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setAppId(ApplicationId appId) {
            this.appId = appId;
            return this;
        }

        public Builder setCountryCode(CountryCode countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder setUserId(UserId userId) {
            this.userId = userId;
            return this;
        }

        public Builder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder setOccurredOn(Long occurredOn) {
            if (!Objects.isNull(occurredOn)) {
                this.occurredOn = occurredOn;
            }
            return this;
        }

        public Builder setPlatform(Platform platform) {
            this.platform = platform;
            return this;
        }

        public Builder setBuild(String build) {
            this.build = build;
            return this;
        }

        public Builder setCustom01(String custom01) {
            this.custom01 = custom01;
            return this;
        }

        public Builder setCustom02(String custom02) {
            this.custom02 = custom02;
            return this;
        }

        public Builder setCustom03(String custom03) {
            this.custom03 = custom03;
            return this;
        }

        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder setMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder setFrom(LanguageCode from) {
            this.from = from;
            return this;
        }

        public Builder setTo(LanguageCode[] to) {
            this.to = to;
            return this;
        }

        public Builder setLength(int length) {
            this.length = length;
            return this;
        }
    }

    public static final DomainEventType TYPE = new DomainEventType("chMsgTrans", ChannelMessageTranslated.class);

    public static final IllegalArgumentException ERROR_INVALID_APP_ID = new IllegalArgumentException("invalid_app_id");
    public static final IllegalArgumentException ERROR_INVALID_COUNTRY_CODE = new IllegalArgumentException("invalid_country_code");
    private static final IllegalArgumentException ERROR_INVALID_MESSAGE_ID = new IllegalArgumentException("invalid_message_message_id");
    private static final IllegalArgumentException ERROR_INVALID_FROM = new IllegalArgumentException("invalid_from");
    private static final IllegalArgumentException ERROR_INVALID_TO = new IllegalArgumentException("invalid_to");
    private static final IllegalArgumentException ERROR_INVALID_LENGTH = new IllegalArgumentException("invalid_length");
    private static final IllegalArgumentException ERROR_INVALID_ID = new IllegalArgumentException("invalid_event_id");
    private static final IllegalArgumentException ERROR_INVALID_SESSION_ID = new IllegalArgumentException("invalid_session_id");
    private static final IllegalArgumentException ERROR_INVALID_TS = new IllegalArgumentException("invalid_timestamp");
    private static final IllegalArgumentException ERROR_INVALID_BUILD = new IllegalArgumentException("invalid_build");
    private static final IllegalArgumentException ERROR_INVALID_CUSTOM_DIM_01 = new IllegalArgumentException("invalid_custom_01");
    private static final IllegalArgumentException ERROR_INVALID_CUSTOM_DIM_02 = new IllegalArgumentException("invalid_custom_02");
    private static final IllegalArgumentException ERROR_INVALID_CUSTOM_DIM_03 = new IllegalArgumentException("invalid_custom_03");

    private final String id;
    private final ApplicationId appId;
    private final CountryCode countryCode;
    private final UserId userId;
    private final String sessionId;
    private final long occurredOn;
    private final Platform platform;
    private final String build;
    private final String custom01;
    private final String custom02;
    private final String custom03;
    private final String channelId;
    private final String messageId;
    private final LanguageCode from;
    private final LanguageCode[] to;
    private final int length;

    private ChannelMessageTranslated(final String aId, final ApplicationId aAppId, CountryCode aCountryCode, final UserId aUserId,
                                  final String aSessionId, long aOccurredOn, final Platform aPlatform, final String aBuild,
                                  final String aCustom01, final String aCustom02, final String aCustom03,
                                     final String aChannelId, final String aMessageId,
                                     final LanguageCode aFrom, final LanguageCode[] aTo, final int aLength) throws IllegalArgumentException {

        Utils.assertRequiredArgument(aId, ERROR_INVALID_ID);
        Utils.assertRequiredArgument(aAppId, ERROR_INVALID_APP_ID);
        Utils.assertRequiredArgument(aUserId, UserId.ERROR_INVALID_USER_ID);
        Utils.assertRequiredArgument(aOccurredOn, ERROR_INVALID_TS);
        Utils.assertRequiredArgument(aMessageId, ERROR_INVALID_MESSAGE_ID);
        Utils.assertRequiredArgument(aTo, ERROR_INVALID_TO);

        if(aLength <= 0) {
            throw ERROR_INVALID_LENGTH;
        }

        id = aId;
        appId = aAppId;
        countryCode = aCountryCode;
        userId = aUserId;
        sessionId = aSessionId;
        occurredOn = aOccurredOn;
        platform = aPlatform;
        build = aBuild;
        custom01 = aCustom01;
        custom02 = aCustom02;
        custom03 = aCustom03;

        channelId = aChannelId;
        messageId = aMessageId;
        from = aFrom;
        to = aTo;
        length = aLength;
    }

    public String id() { return id; }

    public ApplicationId appId() { return appId; }

    public CountryCode countryCode() {
        return countryCode;
    }

    public UserId userId() { return userId; }

    public String sessionId() {
        return sessionId;
    }

    public Platform platform() {
        return platform;
    }

    public String build() {
        return build;
    }

    public String custom01() {
        return custom01;
    }

    public String custom02() {
        return custom02;
    }

    public String custom03() {
        return custom03;
    }

    public String channelId() {
        return channelId;
    }

    public String messageId() {
        return messageId;
    }

    public LanguageCode from() {
        return from;
    }

    public LanguageCode[] to() {
        return to;
    }

    public int length() {
        return length;
    }

    @Override
    public Date occurredOn() {
        return new Date(occurredOn);
    }

    @Override
    public DomainEventType type() {
        return TYPE;
    }

}
