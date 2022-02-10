package io.fizz.common.domain.events;

import io.fizz.common.domain.*;

import java.util.Objects;

public class TextMessageSent extends AbstractDomainEvent {
    public static class Builder extends AbstractDomainEvent.Builder {
        private String content;
        private String channelId;
        private String nick;

        public TextMessageSent get() throws DomainErrorException {
            return new TextMessageSent(
                id, appId, countryCode, userId, version, sessionId,
                occurredOn, platform, build, custom01, custom02, custom03,
                content, channelId, nick
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

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder setNick(String nick) {
            this.nick = nick;
            return this;
        }
    }

    private static int MAX_NICK_LEN = 32;
    private static int MAX_MSG_CONTENT = 2048;
    private static final DomainErrorException ERROR_INVALID_CONTENT = new DomainErrorException(new DomainError("invalid_message_content"));
    private static final DomainErrorException ERROR_INVALID_CHANNEL_ID = new DomainErrorException(new DomainError("invalid_channel_id"));
    private static final DomainErrorException ERROR_INVALID_NICK = new DomainErrorException(new DomainError("invalid_nick"));

    private final String content;
    private final String channelId;
    private final String nick;

    private TextMessageSent(final String aId, final ApplicationId aAppId, CountryCode aCountryCode, final UserId aUserId,
                           int aVersion, final String aSessionId,
                           long aOccurredOn, final Platform aPlatform, final String aBuild,
                           final String aCustom01, final String aCustom02, final String aCustom03,
                           final String aContent, final String aChannelId, final String aNick) throws DomainErrorException {

        super(aId, aAppId, aCountryCode, aUserId, EventType.TEXT_MESSAGE_SENT, aVersion, aSessionId, aOccurredOn,
                aPlatform, aBuild, aCustom01, aCustom02, aCustom03);

        if (Objects.isNull(aContent) || aContent.length() > MAX_MSG_CONTENT) {
            throw ERROR_INVALID_CONTENT;
        }
        if (Objects.isNull(aChannelId) || aChannelId.length() <= 0 || aChannelId.length() > AbstractDomainEvent.MAX_ID_LEN ) {
            throw ERROR_INVALID_CHANNEL_ID;
        }
        if (!Objects.isNull(aNick) && aNick.length() > MAX_NICK_LEN) {
            throw ERROR_INVALID_NICK;
        }

        content = aContent;
        channelId = aChannelId;
        nick = aNick;
    }

    public String content() {
        return content;
    }

    public String channelId() {
        return channelId;
    }

    public String nick() {
        return nick;
    }
}
