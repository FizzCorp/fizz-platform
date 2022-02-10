package io.fizz.analytics.jobs.hive2ES;

import io.fizz.analytics.AbstractSparkTest;
import io.fizz.common.infastructure.model.TextMessageES;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ScoredMessagesToMessagesESTest extends AbstractSparkTest {
    @Test
    @DisplayName("it should transform scored messages to MessageES objects")
    void transformScoredMessages() throws Exception {
        final MockScoredMessagesStore store = new MockScoredMessagesStore(spark);
        final Dataset<Row> scoredMsgsDS = store.scan();
        final ScoredMsgsToMessagesES transformer = new ScoredMsgsToMessagesES();
        final Dataset<TextMessageES> messagesDS = transformer.transform(scoredMsgsDS, null);
        final List<TextMessageES> messages = messagesDS.collectAsList();

        for (final TextMessageES message: messages) {
            switch (message.getId()) {
                case "1":
                    assert (message.getAppId().equals("appA"));
                    assert (message.getActorId().equals("userA"));
                    assert (message.getNick().equals("actorA"));
                    assert (message.getChannel().equals("channelA"));
                    assert (message.getContent().equals("message_1"));
                    assert (message.getPlatform().equals("ios"));
                    assert (message.getBuild().equals("build_1"));
                    assert (message.getCustom01().equals("A"));
                    assert (message.getCustom02().equals("B"));
                    assert (message.getCustom03().equals("C"));
                    assert (message.getCountryCode().equals("PK"));
                    assert (message.getAge().equals("days_8_14"));
                    assert (message.getSpender().equals("none"));
                    assert (message.getSentimentScore() == MockScoredMessagesStore.SENTIMENT_SCORE);
                    assert (message.getTimestamp() == MockScoredMessagesStore.MESSAGE_TS);
                    break;
                case "2":
                    assert (message.getAppId().equals("appA"));
                    assert (message.getActorId().equals("userB"));
                    assert (message.getNick().equals("actorB"));
                    assert (message.getChannel().equals("channelB"));
                    assert (message.getContent().equals("message_2"));
                    assert (message.getPlatform().equals("android"));
                    assert (message.getBuild().equals("build_2"));
                    assert (message.getCustom01().equals("D"));
                    assert (message.getCustom02().equals("E"));
                    assert (message.getCustom03().equals("F"));
                    assert (message.getCountryCode().equals("PK"));
                    assert (message.getAge().equals("days_8_14"));
                    assert (message.getSpender().equals("none"));
                    assert (message.getSentimentScore() == MockScoredMessagesStore.SENTIMENT_SCORE);
                    assert (message.getTimestamp() == MockScoredMessagesStore.MESSAGE_TS);
                    break;
                default:
                    throw new Exception("invalid message id");
            }
        }
    }
}
