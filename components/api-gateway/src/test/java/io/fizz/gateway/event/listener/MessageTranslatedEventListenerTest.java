package io.fizz.gateway.event.listener;

import io.fizz.chat.domain.channel.ChannelMessageTranslated;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;
import io.fizz.common.domain.events.AbstractDomainEvent;
import io.fizz.common.domain.events.TextMessageTranslated;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.fizz.gateway.http.services.handler.eventstream.AbstractEventStreamClientHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class MessageTranslatedEventListenerTest {
    private static int TEST_TIMEOUT = Constants.TEST_TIMEOUT;

    private static final String APP_ID = "AppA";
    private static final String USER_ID = "UserA";
    private static final int LENGTH = 9;
    private static final String MESSAGE_ID = "AppAChannelA1";
    private static final LanguageCode[] languageCodes = new LanguageCode[]{LanguageCode.FRENCH};
    private static final String[] languageCodesStr = new String[]{LanguageCode.FRENCH.value()};

    private static Vertx vertx;

    static private Future<String> deployVertex() {
        Future<String> future = Future.future();
        vertx.deployVerticle(MockApplication.class.getName(), future);
        return future;
    }

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws InterruptedException {
        vertx = Vertx.vertx();

        deployVertex()
                .setHandler(ar -> {
                    if (ar.succeeded()) {
                        aContext.completeNow();
                    } else {
                        aContext.failNow(ar.cause());
                    }
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> aContext.completeNow());
    }

    @Test
    @DisplayName("it should put translation event into stream")
    void testTranslationEvent(VertxTestContext aContext) throws DomainErrorException, InterruptedException {
        AbstractEventStreamClientHandler eventStream = aEvents -> {
            Assertions.assertEquals(1, aEvents.size());
            AbstractDomainEvent event = aEvents.get(0);
            Assertions.assertEquals(TextMessageTranslated.class, event.getClass());

            TextMessageTranslated translationEvent = (TextMessageTranslated) event;
            Assertions.assertEquals(LENGTH,  translationEvent.length());
            Assertions.assertEquals(String.valueOf(MESSAGE_ID),  translationEvent.messageId());
            Assertions.assertEquals(APP_ID,  translationEvent.appId().value());
            Assertions.assertEquals(USER_ID,  translationEvent.userId().value());
            Assertions.assertEquals(Arrays.toString(languageCodesStr),  translationEvent.to());

            aContext.completeNow();

            return CompletableFuture.completedFuture(1);
        };

        MessageTranslatedEventListener eventListener = new MessageTranslatedEventListener(eventStream);
        ChannelMessageTranslated event = buildChannelMessageTranslated();
        eventListener.handleEvent(event);

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    private ChannelMessageTranslated buildChannelMessageTranslated() throws DomainErrorException {
        return new ChannelMessageTranslated.Builder()
                .setId(UUID.randomUUID().toString())
                .setAppId(new ApplicationId(APP_ID))
                .setLength(LENGTH)
                .setOccurredOn(new Date().getTime())
                .setMessageId(MESSAGE_ID)
                .setUserId(new UserId(USER_ID))
                .setTo(languageCodes)
                .get();
    }
}
