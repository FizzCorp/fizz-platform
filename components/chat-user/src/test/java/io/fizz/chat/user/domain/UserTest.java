package io.fizz.chat.user.domain;

import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.Url;
import io.fizz.common.domain.UserId;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class UserTest {
    private static ApplicationId appA;
    private static UserId userA;
    private static Nick nick;
    private static StatusMessage statusMessage;
    private static Url profileUrl;

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws Exception {
        appA = new ApplicationId("appA");
        userA = new UserId("userA");
        nick = new Nick("nick");
        statusMessage = new StatusMessage("Status A");
        profileUrl = new Url("http://www.google.com");;

        aContext.completeNow();
    }

    @Test
    @DisplayName("it should allow to create user with proper values")
    void validUserTest() {
        new User(appA, userA, nick, statusMessage, profileUrl);
    }

    @Test
    @DisplayName("it should not create profile with invalid values")
    void invalidUserTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new User(null, userA, nick, statusMessage, profileUrl));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new User(appA, null, nick, statusMessage, profileUrl));
    }

    @Test
    @DisplayName("it should not set an invalid token")
    void invalidTokenTest() {
        final User user = new User.Builder().setUserId(new UserId("userA")).setAppId(appA).build();

        user.setToken("cJKxaEvuQaiC8-7uUlXl90:APA91bFC27dJWL476wWWRen0bcQPTRoGfCSSY7xQ8yi2LCj66r295c4YmNkxP4bo4mVNvAcO8Z42SvIFGBniYR2dQq-StwtfYx82qIDHfm3pcnZJ6GVZYtL4-PoyRL-EhI_lXgTKhEQy");
        Assertions.assertThrows(IllegalArgumentException.class, () -> user.setToken(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> user.setToken(null));
    }
}
