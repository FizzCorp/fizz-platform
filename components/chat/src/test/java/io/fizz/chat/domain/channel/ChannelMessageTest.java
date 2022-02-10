package io.fizz.chat.domain.channel;

import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

class ChannelMessageTest {
    @Test
    void invalidInputTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new ChannelMessage(
                    -1,
                    new UserId("from"),
                    "nick",
                    new ChannelId(new ApplicationId("appA"), "channel"),
                    new TopicId("topic"),
                    "body",
                    "data",
                    null,
                    new Date(),
                    null
            )
        );

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new ChannelMessage(
                    1,
                    new UserId("from"),
                    "nick",
                    new ChannelId(new ApplicationId("appA"), "channel"),
                    new TopicId("topic"),
                    "body",
                    "data",
                    null,
                    null,
                    null
            )
        );

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new ChannelMessage(
                1,
                null,
                "nick",
                new ChannelId(new ApplicationId("appA"), "channel"),
                new TopicId("topic"),
                "body",
                "data",
                null,
                new Date(),
                null
            )
        );

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new ChannelMessage(
                1,
                new UserId("user"),
                "nick",
                null,
                new TopicId("topic"),
                "body",
                "data",
                null,
                new Date(),
                null
            )
        );

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new ChannelMessage(
                    1,
                    new UserId("user"),
                    "nick",
                    new ChannelId(new ApplicationId("appA"), "channel"),
                    null,
                    "body",
                    "data",
                    null,
                    new Date(),
                    null
                )
        );



        final ChannelMessage message = createMessage();

        Assertions.assertThrows(IllegalArgumentException.class, () -> message.updateData(gen(ChannelMessage.MAX_DATA_LEN*2)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> message.updateBody(gen(ChannelMessage.MAX_BODY_LEN*2), null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> message.updateNick(gen(ChannelMessage.MAX_NICK_LEN*2)));
    }

    @Test
    void updateNickTest() throws InterruptedException {
        final ChannelMessage message = createMessage();

        TimeUnit.SECONDS.sleep(1);
        message.updateNick("nick2");
        Assertions.assertEquals(message.nick(), "nick2");
        Assertions.assertTrue(message.updated() > message.created());
    }

    @Test
    void updateBodyTest() throws InterruptedException {
        final ChannelMessage message = createMessage();

        TimeUnit.SECONDS.sleep(1);
        message.updateBody("body2", LanguageCode.ENGLISH);
        Assertions.assertEquals(message.body(), "body2");
        Assertions.assertEquals(message.locale(), LanguageCode.ENGLISH);
        Assertions.assertTrue(message.updated() > message.created());
    }

    @Test
    void updateDataTest() throws InterruptedException {
        final ChannelMessage message = createMessage();

        TimeUnit.SECONDS.sleep(1);
        message.updateData("data2");
        Assertions.assertEquals(message.data(), "data2");
        Assertions.assertTrue(message.updated() > message.created());
    }

    @Test
    void messageDeletedTest() throws InterruptedException {
        final ChannelMessage message = createMessage();

        TimeUnit.SECONDS.sleep(1);
        message.markAsDeleted();
        Assertions.assertTrue(message.deleted());
    }

    private ChannelMessage createMessage() {
        try {
            return new ChannelMessage(
                    1,
                    new UserId("from"),
                    "nick",
                    new ChannelId(new ApplicationId("appA"), "channel"),
                    new TopicId("topic"),
                    "body",
                    "data",
                    null,
                    new Date(),
                    null
            );
        }
        catch (DomainErrorException ex) {
            return null;
        }
    }

    private String gen(int aSize) {
        byte[] array = new byte[aSize];
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }
}
