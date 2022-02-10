package io.fizz.analytics.domain;

import io.fizz.common.domain.DomainErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserTest {
    private static final String USER_ID = "userA";
    private static final String APP_ID = "appA";
    private static final String BUILD = "buildA";
    private static final String PLATFORM = "ios";
    private static final String LOCATION = "PK";
    private static final String CUSTOM01 = "TestA";
    private static final String CUSTOM02 = "TestB";
    private static final String CUSTOM03 = "TestC";
    private static final String DEFAULT_VALUE = "unknown";
    private static final double SENTIMENT_SUM = 15;
    private static final int MESSAGES_COUNT = 20;
    private static final int AMOUNT_SPENT = 100;
    private static final long LAST_TIME_ACTIVITY_TS = 1516779734;
    private static final long FIRST_TIME_ACTIVITY_TS = 1516779734;

    @Test
    public void itShouldBuildUserWithValidValue() throws Exception {
        User user = getDefaultUser();
        Assertions.assertNotNull(user);
        Assertions.assertEquals(user.id().value(), USER_ID);
        Assertions.assertEquals(user.appId().value(), APP_ID);
        Assertions.assertEquals(user.firstTimeActiveTS(), FIRST_TIME_ACTIVITY_TS);
        Assertions.assertEquals(user.lastTimeActiveTS(), LAST_TIME_ACTIVITY_TS);
        Assertions.assertEquals(user.amountSpentInCents(), AMOUNT_SPENT);
        Assertions.assertEquals(user.sentimentSum(), SENTIMENT_SUM);
        Assertions.assertEquals(user.messagesCounts(), MESSAGES_COUNT);
        Assertions.assertEquals(user.build(), BUILD);
        Assertions.assertEquals(user.platform(), PLATFORM);
        Assertions.assertEquals(user.location(), LOCATION);
        Assertions.assertEquals(user.custom01(), CUSTOM01);
        Assertions.assertEquals(user.custom02(), CUSTOM02);
        Assertions.assertEquals(user.custom03(), CUSTOM03);
    }

    @Test
    public void itShouldBuildUserWithNullValue() throws Exception {
        User user = new User.Builder()
                .setId(USER_ID)
                .setAppId(APP_ID)
                .setFirstTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                .setLastTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                .setAmountSpentInCents(AMOUNT_SPENT)
                .setSentimentSum(SENTIMENT_SUM, MESSAGES_COUNT)
                .setBuild(null)
                .setPlatform(null)
                .setLocation(null)
                .setCustom01(null)
                .setCustom02(null)
                .setCustom03(null)
                .get();
        Assertions.assertNotNull(user);
        Assertions.assertEquals(user.id().value(), USER_ID);
        Assertions.assertEquals(user.appId().value(), APP_ID);
        Assertions.assertEquals(user.firstTimeActiveTS(), FIRST_TIME_ACTIVITY_TS);
        Assertions.assertEquals(user.lastTimeActiveTS(), LAST_TIME_ACTIVITY_TS);
        Assertions.assertEquals(user.amountSpentInCents(), AMOUNT_SPENT);
        Assertions.assertEquals(user.sentimentSum(), SENTIMENT_SUM);
        Assertions.assertEquals(user.messagesCounts(), MESSAGES_COUNT);
        Assertions.assertEquals(user.build(), DEFAULT_VALUE);
        Assertions.assertEquals(user.platform(), DEFAULT_VALUE);
        Assertions.assertEquals(user.location(), DEFAULT_VALUE);
        Assertions.assertNull(user.custom01());
        Assertions.assertNull(user.custom02());
        Assertions.assertNull(user.custom03());
    }

    @Test
    public void itShouldNotBuildUserWithInvalidUserId() throws DomainErrorException {
        try {
            new User.Builder()
                    .setAppId(APP_ID)
                    .setFirstTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                    .setLastTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                    .setAmountSpentInCents(AMOUNT_SPENT)
                    .setSentimentSum(SENTIMENT_SUM, MESSAGES_COUNT)
                    .setBuild(null)
                    .setPlatform(null)
                    .setLocation(null)
                    .setCustom01(null)
                    .setCustom02(null)
                    .setCustom03(null)
                    .get();
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals(e.getMessage(), "invalid_user_id");
        }
    }

    @Test
    public void itShouldNotBuildUserWithInvalidAppId() {
        try {
            new User.Builder()
                    .setId(USER_ID)
                    .setFirstTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                    .setLastTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                    .setAmountSpentInCents(AMOUNT_SPENT)
                    .setSentimentSum(SENTIMENT_SUM, MESSAGES_COUNT)
                    .setBuild(null)
                    .setPlatform(null)
                    .setLocation(null)
                    .setCustom01(null)
                    .setCustom02(null)
                    .setCustom03(null)
                    .get();
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_app_id");
        }
    }

    @Test
    public void itShouldNotBuildUserWithInvalidFirstTS() {
        try {
            new User.Builder()
                    .setId(USER_ID)
                    .setAppId(APP_ID)
                    .setFirstTimeActiveTS(0)
                    .setLastTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                    .setAmountSpentInCents(AMOUNT_SPENT)
                    .setSentimentSum(SENTIMENT_SUM, MESSAGES_COUNT)
                    .setBuild(null)
                    .setPlatform(null)
                    .setLocation(null)
                    .setCustom01(null)
                    .setCustom02(null)
                    .setCustom03(null)
                    .get();
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_ts");
        }
    }

    @Test
    public void itShouldNotBuildUserWithInvalidLastTS() {
        try {
            new User.Builder()
                    .setId(USER_ID)
                    .setAppId(APP_ID)
                    .setFirstTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                    .setLastTimeActiveTS(0)
                    .setAmountSpentInCents(AMOUNT_SPENT)
                    .setSentimentSum(SENTIMENT_SUM, MESSAGES_COUNT)
                    .setBuild(null)
                    .setPlatform(null)
                    .setLocation(null)
                    .setCustom01(null)
                    .setCustom02(null)
                    .setCustom03(null)
                    .get();
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_ts");
        }
    }

    @Test
    public void itShouldNotBuildUserWithInvalidFirstAndLastTS() {
        try {
            new User.Builder()
                    .setId(USER_ID)
                    .setAppId(APP_ID)
                    .setFirstTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                    .setLastTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                    .setAmountSpentInCents(AMOUNT_SPENT)
                    .setSentimentSum(SENTIMENT_SUM, MESSAGES_COUNT)
                    .setBuild(null)
                    .setPlatform(null)
                    .setLocation(null)
                    .setCustom01(null)
                    .setCustom02(null)
                    .setCustom03(null)
                    .get();
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_ts");
        }
    }

    @Test
    public void itShouldNotBuildUserWithInvalidAmount() {
        try {
            new User.Builder()
                    .setId(USER_ID)
                    .setAppId(APP_ID)
                    .setFirstTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                    .setLastTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                    .setAmountSpentInCents(-1)
                    .setSentimentSum(SENTIMENT_SUM, MESSAGES_COUNT)
                    .setBuild(null)
                    .setPlatform(null)
                    .setLocation(null)
                    .setCustom01(null)
                    .setCustom02(null)
                    .setCustom03(null)
                    .get();
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_amount");
        }
    }

    @Test
    public void itShouldBuildUserWithZeroAmount() {
        try {
            new User.Builder()
                    .setId(USER_ID)
                    .setAppId(APP_ID)
                    .setFirstTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                    .setLastTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                    .setAmountSpentInCents(0)
                    .setSentimentSum(SENTIMENT_SUM, MESSAGES_COUNT)
                    .setBuild(null)
                    .setPlatform(null)
                    .setLocation(null)
                    .setCustom01(null)
                    .setCustom02(null)
                    .setCustom03(null)
                    .get();
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_sentiment");
        }
    }

    @Test
    public void itShouldNotBuildUserWithInvalidSentimentSumPositive() {
        try {
            new User.Builder()
                    .setId(USER_ID)
                    .setAppId(APP_ID)
                    .setFirstTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                    .setLastTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                    .setAmountSpentInCents(0)
                    .setSentimentSum(20, 10)
                    .setBuild(null)
                    .setPlatform(null)
                    .setLocation(null)
                    .setCustom01(null)
                    .setCustom02(null)
                    .setCustom03(null)
                    .get();
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_sentiment");
        }
    }

    @Test
    void itShouldNotBuildUserWithInvalidSentimentSumNegative() {
        try {
            new User.Builder()
                    .setId(USER_ID)
                    .setAppId(APP_ID)
                    .setFirstTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                    .setLastTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                    .setAmountSpentInCents(0)
                    .setSentimentSum(-20, 10)
                    .setBuild(null)
                    .setPlatform(null)
                    .setLocation(null)
                    .setCustom01(null)
                    .setCustom02(null)
                    .setCustom03(null)
                    .get();
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_sentiment");
        }
    }

    @Test
    public void itShouldNotBuildUserWithNegativeMessageCount() {
        try {
            new User.Builder()
                    .setId(USER_ID)
                    .setAppId(APP_ID)
                    .setFirstTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                    .setLastTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                    .setAmountSpentInCents(0)
                    .setSentimentSum(-20, -10)
                    .setBuild(null)
                    .setPlatform(null)
                    .setLocation(null)
                    .setCustom01(null)
                    .setCustom02(null)
                    .setCustom03(null)
                    .get();
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_message_count");
        }
    }

    @Test
    public void itShouldNotBuildUserWithZeroMessageCount() {
        try {
            new User.Builder()
                    .setId(USER_ID)
                    .setAppId(APP_ID)
                    .setFirstTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                    .setLastTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                    .setAmountSpentInCents(0)
                    .setSentimentSum(-20, 0)
                    .setBuild(null)
                    .setPlatform(null)
                    .setLocation(null)
                    .setCustom01(null)
                    .setCustom02(null)
                    .setCustom03(null)
                    .get();
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_message_count");
        }
    }

    @Test
    public void itShouldNotUpdateInvalidTS() {
        try {
            User user = getDefaultUser();
            user.updateLastTimeActiveTS(LAST_TIME_ACTIVITY_TS - 1);
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_ts");
        }
    }

    @Test
    public void itShouldNotUpdateNullPlatform() {
        try {
            User user = getDefaultUser();
            user.updatePlatform(null);
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_platform");
        }
    }

    @Test
    public void itShouldNotUpdateNullBuild() {
        try {
            User user = getDefaultUser();
            user.updateBuild(null);
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_build");
        }
    }

    @Test
    public void itShouldNotUpdateNullLocation() {
        try {
            User user = getDefaultUser();
            user.updateLocation(null);
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_location");
        }
    }

    @Test
    public void itShouldNotAddNegativeAmount() {
        try {
            User user = getDefaultUser();
            user.addAmountSpentInCents(-1);
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_amount");
        }
    }

    @Test
    public void itShouldNotAddInvalidSentimentSum() {
        try {
            User user = getDefaultUser();
            user.addSentimentSum(-20, 5);
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_sentiment");
        }
    }

    @Test
    public void itShouldNotAddZeroMessageCount() {
        try {
            User user = getDefaultUser();
            user.addSentimentSum(-20, 0);
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_message_count");
        }
    }

    @Test
    public void itShouldNotAddNegativeMessageCount() {
        try {
            User user = getDefaultUser();
            user.addSentimentSum(-20, -1);
        } catch (DomainErrorException e) {
            Assertions.assertEquals(e.error().reason(), "invalid_message_count");
        }
    }

    @Test
    public void itShouldAbleToUpdateCustomParams() throws DomainErrorException {
        User user = getDefaultUser();
        user.setCustom01("TestD");
        user.setCustom02("TestE");
        user.setCustom03("TestF");

        Assertions.assertEquals(user.custom01(), "TestD");
        Assertions.assertEquals(user.custom02(), "TestE");
        Assertions.assertEquals(user.custom03(), "TestF");
    }

    @Test
    public void itShouldAbleToUpdateNullCustomParams() throws DomainErrorException {
        User user = getDefaultUser();
        user.setCustom01(null);
        user.setCustom02(null);
        user.setCustom03(null);

        Assertions.assertNull(user.custom01());
        Assertions.assertNull(user.custom02());
        Assertions.assertNull(user.custom03());
    }

    private User getDefaultUser() throws DomainErrorException {
        return new User.Builder()
                .setId(USER_ID)
                .setAppId(APP_ID)
                .setFirstTimeActiveTS(FIRST_TIME_ACTIVITY_TS)
                .setLastTimeActiveTS(LAST_TIME_ACTIVITY_TS)
                .setAmountSpentInCents(AMOUNT_SPENT)
                .setSentimentSum(SENTIMENT_SUM, MESSAGES_COUNT)
                .setBuild(BUILD)
                .setPlatform(PLATFORM)
                .setLocation(LOCATION)
                .setCustom01(CUSTOM01)
                .setCustom02(CUSTOM02)
                .setCustom03(CUSTOM03)
                .get();
    }
}
