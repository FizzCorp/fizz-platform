package io.fizz.gateway.http.ratelimit;

import io.fizz.chatcommon.domain.RedisNamespace;
import io.fizz.common.Utils;
import io.fizz.gateway.http.annotations.RLScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RateLimitServiceTest {

    @Test
    @DisplayName("it should not handle calls over rate limit")
    void rateLimitTest() throws Exception {

        final int MAX_LIMIT = 5;
        final String APP_ID = "appA";
        final String USER_ID = "User1";
        final String CHANNEL_ID = "channelA";
        Set<RateLimitConfig> configList = new HashSet<RateLimitConfig>() {{
            add(new RateLimitConfig(RLScope.CHANNEL, MAX_LIMIT));
        }};
        RateLimitService service = new RateLimitService(configList, new MockRateLimitRepository(), RedisNamespace.RATE_LIMIT);

        // sleep till next second starts
        long remainingMillis = Utils.now()%1000;
        Thread.sleep(1000+remainingMillis);

        // calls within rate limit should be handled successfully
        for (int ii=0; ii<MAX_LIMIT; ii++) {
            Assertions.assertTrue(service.handle(APP_ID, USER_ID, RLScope.CHANNEL, CHANNEL_ID).get());
        }

        // Call over rate limit should be failed
        Assertions.assertFalse(service.handle(APP_ID, USER_ID, RLScope.CHANNEL, CHANNEL_ID).get());

        // wait a second for key to be updated [second interval]
        Thread.sleep(1000);

        // After second interval new calls should be allowed again
        Assertions.assertTrue(service.handle(APP_ID, USER_ID, RLScope.CHANNEL, CHANNEL_ID).get());
    }

}
