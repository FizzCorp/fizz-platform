package io.fizz.common.domain.events;

import io.fizz.common.domain.*;

import java.util.Arrays;
import java.util.Objects;

public class TextMessageTranslated extends AbstractDomainEvent {
    public static class Builder extends AbstractDomainEvent.Builder {
        private String channelId;
        private String messageId;
        private String from;
        private String[] to;
        private int length;

        public TextMessageTranslated get() throws DomainErrorException {
            return new TextMessageTranslated(
                    id, appId, countryCode, userId, version, sessionId,
                    occurredOn, platform, build, custom01, custom02, custom03,
                    channelId, messageId, from, Arrays.toString(to), length
            );
        }

        public Builder setId(String id) {
            super.setId(id);
            return this;
        }

        public Builder setAppId(ApplicationId appId) {
            super.setAppId(appId);
            return this;
        }

        @Override
        public Builder setCountryCode(CountryCode countryCode) {
            super.setCountryCode(countryCode);
            return this;
        }

        public Builder setUserId(UserId userId) {
            super.setUserId(userId);
            return this;
        }

        public Builder setVersion(Integer version) {
            super.setVersion(version);
            return this;
        }

        public Builder setSessionId(String sessionId) {
            super.setSessionId(sessionId);
            return this;
        }

        public Builder setOccurredOn(Long occurredOn) {
            super.setOccurredOn(occurredOn);
            return this;
        }

        public Builder setPlatform(Platform platform) {
            super.setPlatform(platform);
            return this;
        }

        public Builder setBuild(String build) {
            super.setBuild(build);
            return this;
        }

        public Builder setCustom01(String custom01) {
            super.setCustom01(custom01);
            return this;
        }

        public Builder setCustom02(String custom02) {
            super.setCustom02(custom02);
            return this;
        }

        public Builder setCustom03(String custom03) {
            super.setCustom03(custom03);
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

        public Builder setFrom(String from) {
            this.from = from;
            return this;
        }

        public Builder setTo(String[] to) {
            this.to = to;
            return this;
        }

        public Builder setLength(int length) {
            this.length = length;
            return this;
        }
    }

    private static final DomainErrorException ERROR_INVALID_MESSAGE_ID = new DomainErrorException(new DomainError("invalid_message_message_id"));
    private static final DomainErrorException ERROR_INVALID_FROM = new DomainErrorException(new DomainError("invalid_from"));
    private static final DomainErrorException ERROR_INVALID_TO = new DomainErrorException(new DomainError("invalid_to"));
    private static final DomainErrorException ERROR_INVALID_LENGTH = new DomainErrorException(new DomainError("invalid_length"));

    private final String channelId;
    private final String messageId;
    private final String from;
    private final String to;
    private final int length;

    private TextMessageTranslated(final String aId, final ApplicationId aAppId, CountryCode aCountryCode, final UserId aUserId,
                                  int aVersion, final String aSessionId,
                                  long aOccurredOn, final Platform aPlatform, final String aBuild,
                                  final String aCustom01, final String aCustom02, final String aCustom03,
                                  final String aChannelId,
                                  final String aMessageId, final String aFrom, final String aTo, final int aLength) throws DomainErrorException {

        super(aId, aAppId, aCountryCode, aUserId, EventType.TEXT_TRANSLATED, aVersion, aSessionId, aOccurredOn,
                aPlatform, aBuild, aCustom01, aCustom02, aCustom03);

        if (Objects.isNull(aMessageId) || aMessageId.length() <= 0) {
            throw ERROR_INVALID_MESSAGE_ID;
        }
        if (!Objects.isNull(aTo) && aTo.length() <= 0) {
            throw ERROR_INVALID_TO;
        }
        if(aLength <= 0) {
            throw  ERROR_INVALID_LENGTH;
        }
        channelId = aChannelId;
        messageId = aMessageId;
        from = aFrom;
        to = aTo;
        length = aLength;
    }

    public String channelId() {
        return channelId;
    }

    public String messageId() {
        return messageId;
    }

    public String from() {
        return from;
    }

    public String to() {
        return to;
    }

    public int length() {
        return length;
    }
}
