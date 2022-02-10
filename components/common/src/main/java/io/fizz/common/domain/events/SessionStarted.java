package io.fizz.common.domain.events;

import io.fizz.common.domain.*;

public class SessionStarted extends AbstractDomainEvent {
    public static class Builder extends AbstractDomainEvent.Builder {
        public SessionStarted get() throws DomainErrorException {
            return new SessionStarted(
                    id, appId, countryCode, userId, version, sessionId,
                    occurredOn, platform, build, custom01, custom02, custom03
            );
        }

        @Override
        public Builder setId(String id) {
            super.setId(id);
            return this;
        }

        @Override
        public Builder setAppId(ApplicationId appId) {
            super.setAppId(appId);
            return this;
        }

        @Override
        public Builder setCountryCode(CountryCode countryCode) {
            super.setCountryCode(countryCode);
            return this;
        }

        @Override
        public Builder setUserId(UserId userId) {
            super.setUserId(userId);
            return this;
        }

        @Override
        public Builder setVersion(Integer version) {
            super.setVersion(version);
            return this;
        }

        @Override
        public Builder setSessionId(String sessionId) {
            super.setSessionId(sessionId);
            return this;
        }

        @Override
        public Builder setOccurredOn(Long occurredOn) {
            super.setOccurredOn(occurredOn);
            return this;
        }

        @Override
        public Builder setPlatform(Platform platform) {
            super.setPlatform(platform);
            return this;
        }

        @Override
        public Builder setBuild(String build) {
            super.setBuild(build);
            return this;
        }

        @Override
        public Builder setCustom01(String custom01) {
            super.setCustom01(custom01);
            return this;
        }

        @Override
        public Builder setCustom02(String custom02) {
            super.setCustom02(custom02);
            return this;
        }

        @Override
        public Builder setCustom03(String custom03) {
            super.setCustom03(custom03);
            return this;
        }
    }

    private SessionStarted(final String aId, final ApplicationId aAppId, CountryCode aCountryCode, final UserId aUserId,
                           int aVersion, final String aSessionId, long aOccurredOn, final Platform aPlatform,
                           final String aBuild, final String aCustom01, final String aCustom02, final String aCustom03) throws DomainErrorException {
        super(aId, aAppId, aCountryCode, aUserId, EventType.SESSION_STARTED,
            aVersion, aSessionId, aOccurredOn, aPlatform,
            aBuild, aCustom01, aCustom02, aCustom03);
    }
}
