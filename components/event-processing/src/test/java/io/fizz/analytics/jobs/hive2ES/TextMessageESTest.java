package io.fizz.analytics.jobs.hive2ES;

import io.fizz.common.infastructure.model.TextMessageES;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TextMessageESTest {
    @Test
    @DisplayName("it should create valid text message")
    void basicValidityTest() {
        final String id = "test_id";
        final String appId = "test_app_id";
        final String countryCode = "pk";
        final String actorId = "test_actor";
        final String nick = "test_nick";
        final String content = "test message";
        final String channel = "test_channel";
        final String platform = "test_platform";
        final String build = "build01";
        final String custom01 = "customDim1";
        final String custom02 = "customDim2";
        final String custom03 = "customDim3";
        final long timestamp = 12345678L;
        final Double sentimentScore = 0.5;

        final TextMessageES message = new TextMessageES(
                id, appId, countryCode, actorId, nick, content, channel, platform, build,
                custom01, custom02, custom03, timestamp, sentimentScore, null, null, null
        );

        assert (message.getId().equals(id));
        assert (message.getAppId().equals(appId));
        assert (message.getCountryCode().equals(countryCode));
        assert (message.getActorId().equals(actorId));
        assert (message.getNick().equals(nick));
        assert (message.getContent().equals(content));
        assert (message.getChannel().equals(channel));
        assert (message.getPlatform().equals(platform));
        assert (message.getBuild().equals(build));
        assert (message.getCustom01().equals(custom01));
        assert (message.getCustom02().equals(custom02));
        assert (message.getCustom03().equals(custom03));
        assert (message.getTimestamp() == timestamp);
        assert (message.getSentimentScore() == sentimentScore);
    }
}
