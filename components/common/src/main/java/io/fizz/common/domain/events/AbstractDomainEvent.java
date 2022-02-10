package io.fizz.common.domain.events;

import io.fizz.common.Utils;
import io.fizz.common.domain.*;

import java.util.Objects;

public abstract class AbstractDomainEvent {
    public static final int VERSION = 1;

    public static abstract class Builder {
        protected String id;
        protected CountryCode countryCode;
        protected ApplicationId appId;
        protected UserId userId;
        protected int version = -1;
        protected String sessionId;
        protected long occurredOn = -1;
        protected Platform platform;
        protected String build;
        protected String custom01;
        protected String custom02;
        protected String custom03;

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

        public Builder setVersion(Integer version) {
            if (!Objects.isNull(version)) {
                this.version = version;
            }
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
    }

    static int MAX_ID_LEN = 64;
    private static int MAX_BUILD_LEN = 32;
    private static int MAX_CUSTOM_DIM_LEN = 64;
    private static final DomainErrorException ERROR_INVALID_ID = new DomainErrorException(new DomainError("invalid_event_id"));
    private static final DomainErrorException ERROR_INVALID_TYPE = new DomainErrorException(new DomainError("invalid_event_type"));
    private static final DomainErrorException ERROR_INVALID_VER = new DomainErrorException(new DomainError("invalid_event_ver"));
    private static final DomainErrorException ERROR_INVALID_SESSION_ID = new DomainErrorException(new DomainError("invalid_session_id"));
    private static final DomainErrorException ERROR_INVALID_TS = new DomainErrorException(new DomainError("invalid_timestamp"));
    private static final DomainErrorException ERROR_INVALID_BUILD = new DomainErrorException(new DomainError("invalid_build"));
    private static final DomainErrorException ERROR_INVALID_CUSTOM_DIM_01 = new DomainErrorException(new DomainError("invalid_custom_01"));
    private static final DomainErrorException ERROR_INVALID_CUSTOM_DIM_02 = new DomainErrorException(new DomainError("invalid_custom_02"));
    private static final DomainErrorException ERROR_INVALID_CUSTOM_DIM_03 = new DomainErrorException(new DomainError("invalid_custom_03"));

    private final String id;
    private final ApplicationId appId;
    private final CountryCode countryCode;
    private final UserId userId;
    private final EventType type;
    private final int version;
    private final String sessionId;
    private final long occurredOn;
    private final Platform platform;
    private final String build;
    private final String custom01;
    private final String custom02;
    private final String custom03;

    public AbstractDomainEvent(final String aId, final ApplicationId aAppId, CountryCode aCountryCode, final UserId aUserId,
                               final EventType aType, int aVersion, final String aSessionId,
                               long aOccurredOn, final Platform aPlatform, final String aBuild,
                               final String aCustom01, final String aCustom02, final String aCustom03) throws DomainErrorException {
        if (Objects.isNull(aId) || aId.length() <= 0 || aId.length() > MAX_ID_LEN) {
            throw ERROR_INVALID_ID;
        }
        if(Objects.isNull(aCountryCode)) {
            throw CountryCode.ERROR_INVALID_COUNTRY_CODE;
        }
        if (Objects.isNull(aAppId)) {
            throw ApplicationId.ERROR_INVALID_APP_ID;
        }
        if (Objects.isNull(aUserId)) {
            throw UserId.ERROR_INVALID_USER_ID;
        }
        if (Objects.isNull(aSessionId) || aSessionId.length() <= 0 || aSessionId.length() > MAX_ID_LEN) {
            throw ERROR_INVALID_SESSION_ID;
        }
        if (Objects.isNull(aType)) {
            throw ERROR_INVALID_TYPE;
        }
        if (aVersion <= 0 || aVersion > VERSION) {
            throw ERROR_INVALID_VER;
        }
        if (aOccurredOn < 0) {
            throw ERROR_INVALID_TS;
        }
        if (Objects.nonNull(aBuild) && aBuild.length() > MAX_BUILD_LEN) {
            throw ERROR_INVALID_BUILD;
        }
        if (Objects.nonNull(aCustom01) && (aCustom01.length() > MAX_CUSTOM_DIM_LEN
                                            || !Utils.isAlphaNumeric(aCustom01))) {
            throw ERROR_INVALID_CUSTOM_DIM_01;
        }
        if (Objects.nonNull(aCustom02) && (aCustom02.length() > MAX_CUSTOM_DIM_LEN
                                            || !Utils.isAlphaNumeric(aCustom02))) {
            throw ERROR_INVALID_CUSTOM_DIM_02;
        }
        if (Objects.nonNull(aCustom03) && (aCustom03.length() > MAX_CUSTOM_DIM_LEN
                                            || !Utils.isAlphaNumeric(aCustom03))) {
            throw ERROR_INVALID_CUSTOM_DIM_03;
        }

        id = aId;
        appId = aAppId;
        countryCode = aCountryCode;
        userId = aUserId;
        type = aType;
        version = aVersion;
        sessionId = aSessionId;
        occurredOn = aOccurredOn;
        platform = aPlatform;
        build = aBuild;
        custom01 = aCustom01;
        custom02 = aCustom02;
        custom03 = aCustom03;
    }

    public String id() { return id; }

    public ApplicationId appId() { return appId; }

    public CountryCode countryCode() {
        return countryCode;
    }

    public UserId userId() { return userId; }

    public EventType type() {
        return type;
    }

    public int version() {
        return version;
    }

    public String sessionId() {
        return sessionId;
    }

    public long occurredOn() {
        return occurredOn;
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
}
