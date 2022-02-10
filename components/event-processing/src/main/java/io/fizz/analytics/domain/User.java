package io.fizz.analytics.domain;

import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainError;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;

import java.util.Objects;

public class User {
    public static class Builder {
        private static final String USER_FIELDS_DEFAULT_VALUE = "unknown";
        private UserId id;
        private ApplicationId appId;
        private long firstTimeActiveTS;
        private long lastTimeActiveTS;
        private long amountSpentInCents;
        private String location;
        private String build;
        private String platform;
        private String custom01;
        private String custom02;
        private String custom03;
        private double sentimentSum;
        private long messagesCounts;

        public User get() throws DomainErrorException {
            if (Objects.isNull(location)) {
                location = USER_FIELDS_DEFAULT_VALUE;
            }
            if (Objects.isNull(build)) {
                build = USER_FIELDS_DEFAULT_VALUE;
            }
            if (Objects.isNull(platform)) {
                platform = USER_FIELDS_DEFAULT_VALUE;
            }
            return new User(id, appId, firstTimeActiveTS, lastTimeActiveTS, amountSpentInCents,
                    location, build, platform, custom01, custom02, custom03,
                    sentimentSum, messagesCounts);
        }

        public Builder setId(final UserId id) {
            this.id = id;
            return this;
        }

        public Builder setId(final String id) {
            try {
                this.id = new UserId(id);
            } catch (IllegalArgumentException ignored) { }
            return this;
        }

        public Builder setAppId(final ApplicationId appId) {
            this.appId = appId;
            return this;
        }

        public Builder setAppId(final String appId) {
            try {
                this.appId = new ApplicationId(appId);
            } catch (DomainErrorException ignored) { }
            return this;
        }

        public Builder setFirstTimeActiveTS(final long firstTimeActiveTS) {
            this.firstTimeActiveTS = firstTimeActiveTS;
            return this;
        }

        public Builder setLastTimeActiveTS(final long lastTimeActiveTS) {
            this.lastTimeActiveTS = lastTimeActiveTS;
            return this;
        }

        public Builder setAmountSpentInCents(final long amountSpentInCents) {
            this.amountSpentInCents = amountSpentInCents;
            return this;
        }

        public Builder setLocation(final String location) {
            this.location = location;
            return this;
        }

        public Builder setBuild(final String build) {
            this.build = build;
            return this;
        }

        public Builder setPlatform(final String platform) {
            this.platform = platform;
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

        public Builder setSentimentSum(final double sentimentSum, final long messagesCounts) {
            this.sentimentSum = sentimentSum;
            this.messagesCounts = messagesCounts;
            return this;
        }
    }

    private static final DomainErrorException ERROR_INVALID_TS = new DomainErrorException(new DomainError("invalid_ts"));
    private static final DomainErrorException ERROR_INVALID_AMOUNT = new DomainErrorException(new DomainError("invalid_amount"));
    private static final DomainErrorException ERROR_INVALID_LOCATION = new DomainErrorException(new DomainError("invalid_location"));
    private static final DomainErrorException ERROR_INVALID_BUILD = new DomainErrorException(new DomainError("invalid_build"));
    private static final DomainErrorException ERROR_INVALID_PLATFORM = new DomainErrorException(new DomainError("invalid_platform"));
    private static final DomainErrorException ERROR_INVALID_SENTIMENT = new DomainErrorException(new DomainError("invalid_sentiment"));
    private static final DomainErrorException ERROR_INVALID_MESSAGE_COUNT = new DomainErrorException(new DomainError("invalid_message_count"));

    private final UserId id;
    private final ApplicationId appId;
    private final long firstTimeActiveTS;
    private long lastTimeActiveTS;
    private long amountSpentInCents;
    private String location;
    private String build;
    private String platform;
    private String custom01;
    private String custom02;
    private String custom03;
    private double sentimentSum;
    private long messagesCounts;

    private User(final UserId aId,
                final ApplicationId aAppId,
                final long aFirstTimeActiveTS,
                final long aLastTimeActiveTS,
                final long aAmountSpentInCents,
                final String aLocation,
                final String aBuild,
                final String aPlatform,
                final String aCustom01,
                final String aCustom02,
                final String aCustom03,
                final double aSentimentSum,
                final long aMessagesCounts) throws DomainErrorException {

        if (Objects.isNull(aId)) {
            throw UserId.ERROR_INVALID_USER_ID;
        }
        if (Objects.isNull(aAppId)) {
            throw ApplicationId.ERROR_INVALID_APP_ID;
        }
        if (aFirstTimeActiveTS <=0 || aLastTimeActiveTS <= 0 || aFirstTimeActiveTS > aLastTimeActiveTS) {
            throw ERROR_INVALID_TS;
        }
        if (aAmountSpentInCents < 0) {
            throw ERROR_INVALID_AMOUNT;
        }
        if (Objects.isNull(aLocation) || Utils.isEmpty(aLocation)) {
            throw ERROR_INVALID_LOCATION;
        }
        if (Objects.isNull(aBuild) || Utils.isEmpty(aBuild)) {
            throw ERROR_INVALID_BUILD;
        }
        if (Objects.isNull(aPlatform) || Utils.isEmpty(aPlatform)) {
            throw ERROR_INVALID_PLATFORM;
        }
        if (aMessagesCounts < 0) {
            throw ERROR_INVALID_MESSAGE_COUNT;
        }
        if (aSentimentSum != 0 && aMessagesCounts == 0) {
            throw ERROR_INVALID_MESSAGE_COUNT;
        }
        double avgSentiment = aSentimentSum / aMessagesCounts;
        if (avgSentiment < -1 || avgSentiment > 1) {
            throw ERROR_INVALID_SENTIMENT;
        }

        id = aId;
        appId = aAppId;
        firstTimeActiveTS = aFirstTimeActiveTS;
        lastTimeActiveTS = aLastTimeActiveTS;
        amountSpentInCents = aAmountSpentInCents;
        location = aLocation;
        build = aBuild;
        platform = aPlatform;
        custom01 = aCustom01;
        custom02 = aCustom02;
        custom03 = aCustom03;
        sentimentSum = aSentimentSum;
        messagesCounts = aMessagesCounts;

    }

    public User(User other) throws DomainErrorException {
        this(other.id, other.appId, other.firstTimeActiveTS, other.lastTimeActiveTS, other.amountSpentInCents,
                other.location, other.build, other.platform, other.custom01, other.custom02, other.custom03,
                other.sentimentSum, other.messagesCounts);
    }

    public UserId id() {
        return id;
    }

    public ApplicationId appId() {
        return appId;
    }

    public long firstTimeActiveTS() {
        return firstTimeActiveTS;
    }

    public long lastTimeActiveTS() {
        return lastTimeActiveTS;
    }

    public long amountSpentInCents() {
        return amountSpentInCents;
    }

    public String location() {
        return location;
    }

    public String build() {
        return build;
    }

    public String platform() {
        return platform;
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

    public double sentimentSum() {
        return sentimentSum;
    }

    public long messagesCounts() {
        return messagesCounts;
    }

    public User updateLastTimeActiveTS(long aLastTimeActiveTS) throws DomainErrorException {
        if (aLastTimeActiveTS < lastTimeActiveTS) {
            throw ERROR_INVALID_TS;
        }
        this.lastTimeActiveTS = aLastTimeActiveTS;
        return this;
    }

    public User updateLocation(String aLocation) throws DomainErrorException {
        if (Objects.isNull(aLocation) || Utils.isEmpty(aLocation)) {
            throw ERROR_INVALID_LOCATION;
        }
        location = aLocation;
        return this;
    }

    public User updateBuild(String aBuild) throws DomainErrorException {
        if (Objects.isNull(aBuild) || Utils.isEmpty(aBuild)) {
            throw ERROR_INVALID_BUILD;
        }
        build = aBuild;
        return this;
    }

    public User updatePlatform(String aPlatform) throws DomainErrorException {
        if (Objects.isNull(aPlatform) || Utils.isEmpty(aPlatform)) {
            throw ERROR_INVALID_PLATFORM;
        }
        platform = aPlatform;
        return this;
    }

    public User setCustom01(String custom01) {
        this.custom01 = custom01;
        return this;
    }

    public User setCustom02(String custom02) {
        this.custom02 = custom02;
        return this;
    }

    public User setCustom03(String custom03) {
        this.custom03 = custom03;
        return this;
    }

    public User addAmountSpentInCents(long aAmountSpentInCents) throws DomainErrorException {
        if (aAmountSpentInCents < 0) {
            throw ERROR_INVALID_AMOUNT;
        }
        this.amountSpentInCents += aAmountSpentInCents;
        return this;
    }

    public User addSentimentSum(double aSentimentSum, int aMessagesCounts) throws DomainErrorException {
        if (aMessagesCounts < 0) {
            throw ERROR_INVALID_MESSAGE_COUNT;
        }
        if (aSentimentSum != 0 && aMessagesCounts == 0) {
            throw ERROR_INVALID_MESSAGE_COUNT;
        }
        double avgSentiment = aSentimentSum / aMessagesCounts;
        if (avgSentiment < -1 || avgSentiment > 1) {
            throw ERROR_INVALID_SENTIMENT;
        }
        this.sentimentSum += aSentimentSum;
        this.messagesCounts += aMessagesCounts;
        return this;
    }
}
