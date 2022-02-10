package io.fizz.analytics.common;

import io.fizz.analytics.common.source.hive.*;
import io.fizz.analytics.domain.User;
import io.fizz.common.domain.DomainError;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.EventType;
import org.apache.spark.sql.Row;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class Profile {
    public static class Builder {
        private List<Row> events;
        private User user;

        public Builder setEvents(final List<Row> events) {
            this.events = events;
            return this;
        }

        public Builder setUser(final User user) {
            this.user = user;
            return this;
        }

        public Profile get() throws DomainErrorException {
            if (events.size() == 0) {
                throw new DomainErrorException(new DomainError("events list is empty"));
            }

            String userId = HiveRawEventTableSchema.userId(events.get(0));
            String appId = HiveRawEventTableSchema.appId(events.get(0));
            long amountSpentInCents = 0;
            double sentimentScore = 0.0;
            int messagesCount = 0;


            long processingTime = Utils.fromYYYYMMDD(HiveRawEventTableSchema.day(events.get(0))).getTime();
            long firstTimeActiveTS = Objects.isNull(user) ? processingTime : user.firstTimeActiveTS();
            long lastTimeActiveTS = Objects.isNull(user) ? processingTime : user.lastTimeActiveTS();

            String location = Objects.isNull(user) ? null : user.location();
            String build = Objects.isNull(user) ? null : user.build();
            String platform = Objects.isNull(user) ? null : user.platform();

            String custom01 = Objects.isNull(user) ? null : user.custom01();
            String custom02 = Objects.isNull(user) ? null : user.custom02();
            String custom03 = Objects.isNull(user) ? null : user.custom03();

            for (final Row event : events) {
                if (!Objects.equals(userId, HiveRawEventTableSchema.userId(event))) {
                    throw new DomainErrorException(new DomainError("invalid event list"));
                }

                if (!Objects.equals(appId, HiveRawEventTableSchema.appId(event))) {
                    throw new DomainErrorException(new DomainError("invalid event list"));
                }

                final long occurredOn = HiveRawEventTableSchema.occurredOn(event);
                firstTimeActiveTS = Long.min(firstTimeActiveTS, occurredOn);

                if (occurredOn > lastTimeActiveTS) {
                    lastTimeActiveTS = occurredOn;

                    location = Objects.isNull(HiveRawEventTableSchema.countryCode(event))
                            ? location : HiveRawEventTableSchema.countryCode(event);
                    build = Objects.isNull(HiveRawEventTableSchema.build(event))
                            ? build : HiveRawEventTableSchema.build(event);
                    platform = Objects.isNull(HiveRawEventTableSchema.platform(event))
                            ? platform : HiveRawEventTableSchema.platform(event);

                    custom01 = HiveRawEventTableSchema.custom01(event);
                    custom02 = HiveRawEventTableSchema.custom02(event);
                    custom03 = HiveRawEventTableSchema.custom03(event);
                }

                final JSONObject fields = new JSONObject(HiveRawEventTableSchema.fields(event));
                if (HiveRawEventTableSchema.eventType(event) == EventType.PRODUCT_PURCHASED.value()) {
                    amountSpentInCents += fields.getInt(HiveEventFields.PURCHASE_AMOUNT.value());
                }

                if (HiveRawEventTableSchema.eventType(event) == EventType.TEXT_MESSAGE_SENT.value()) {
                    sentimentScore += fields.has(HiveEventFields.SENTIMENT_SCORE.value()) ?
                            fields.getDouble(HiveEventFields.SENTIMENT_SCORE.value()) : 0.0;
                    messagesCount++;
                }
            }

            boolean firstSession = false;
            String lifecycleSegment = Segmentation.lifecycle.fetchSegment(durationInDays(lastTimeActiveTS, firstTimeActiveTS));
            long totalSpend = amountSpentInCents + (Objects.isNull(user) ? 0 : user.amountSpentInCents());
            String spenderSegment = Segmentation.spender.fetchSegment(totalSpend);

            User user = Objects.isNull(this.user) ? null : new User(this.user);
            if (Objects.nonNull(user)) {
                user
                .updateLastTimeActiveTS(lastTimeActiveTS)
                .addAmountSpentInCents(amountSpentInCents)
                .addSentimentSum(sentimentScore, messagesCount)
                .updateLocation(location)
                .updateBuild(build)
                .updatePlatform(platform)
                .setCustom01(custom01)
                .setCustom02(custom02)
                .setCustom03(custom03);
            }
            else {
                firstSession = true;
                user = new User.Builder()
                        .setId(userId)
                        .setAppId(appId)
                        .setFirstTimeActiveTS(firstTimeActiveTS)
                        .setLastTimeActiveTS(lastTimeActiveTS)
                        .setAmountSpentInCents(amountSpentInCents)
                        .setSentimentSum(sentimentScore, messagesCount)
                        .setLocation(location)
                        .setBuild(build)
                        .setPlatform(platform)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .get();
            }

            return new Profile(user, firstSession, lifecycleSegment, spenderSegment);
        }
    }

    private final User user;
    private final boolean firstSession;
    private final String spenderSegment;
    private final String lifecycleSegment;

    private Profile(final User aUser, final boolean aFirstSession,
                    final String aLifecycleSegment, final String aSpenderSegment) {
        user = aUser;
        firstSession = aFirstSession;
        spenderSegment = aSpenderSegment;
        lifecycleSegment = aLifecycleSegment;
    }

    public User user() {
        return user;
    }

    public boolean firstSession() {
        return firstSession;
    }

    public String spenderSegment() {
        return spenderSegment;
    }

    public String lifecycleSegment() {
        return lifecycleSegment;
    }

    public static int durationInDays(long latestActivityTS, long oldestActivityTS) {
        return (int)((latestActivityTS - oldestActivityTS)/(60*60*24*1000));
    }
}
