package io.fizz.gateway.http.services.handler.eventstream;

import com.google.common.collect.Lists;
import io.fizz.common.domain.*;
import io.fizz.common.domain.events.*;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class MockEventStreamServiceClientHandlerTest {
    private static int TEST_TIMEOUT = Constants.TEST_TIMEOUT;

    private static final String VALUE_APP_ID = "appA";
    private static final String VALUE_PLATFORM = "ios";
    private static final String VALUE_BUILD = "build_1";
    private static final String VALUE_USER_1 = "user_1";
    private static final String VALUE_SESSION_1 = "session_1";
    private static final int VALUE_DURATION = 10;
    private static final String VALUE_TEXT_CONTENT = "text message";
    private static final String VALUE_CHANNEL_ID = "channel_1";
    private static final String VALUE_USER_NICK = "sender_nick";
    private static final String VALUE_COUNTRY_CODE = "PK";
    private static final int VALUE_AMOUNT = 100;
    private static final String VALUE_PRODUCT_ID = "com.test.inapp";
    private static final String VALUE_RECEIPT = "test.receipt";
    private static final String VALUE_FROM = "en";
    private static final String[] VALUE_TO = {"fr", "de"};
    private static final String VALUE_MESSAGE_ID = "test.message.id";
    private static final int VALUE_LENGTH = 40;

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
    void testEventStreamService(VertxTestContext aContext) throws DomainErrorException, InterruptedException {
        final List<AbstractDomainEvent> events = buildTestEvents();
        final MockEventStreamServiceClientHandler eventStreamHandler = new MockEventStreamServiceClientHandler(vertx);
        eventStreamHandler
                .put(events)
                .handle((aResult, aError) -> {
                    if (Objects.nonNull(aError)) {
                        aContext.failNow(aError);
                        return CompletableFuture.completedFuture(null);
                    }
                    Assertions.assertEquals(5, aResult.intValue());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(null);
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    void testEventStreamServiceEmptyList() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            try {
                final List<AbstractDomainEvent> events = Lists.newArrayList();
                final MockEventStreamServiceClientHandler eventStreamHandler = new MockEventStreamServiceClientHandler(vertx);
                eventStreamHandler.put(events);
            } catch (IllegalArgumentException ex) {
                Assertions.assertEquals(ex.getMessage(), "invalid_event_data");
                throw ex;
            }
        });
    }

    @Test
    void testEventStreamServiceNullList() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            try {
                final MockEventStreamServiceClientHandler eventStreamHandler = new MockEventStreamServiceClientHandler(vertx);
                eventStreamHandler.put(null);
            } catch (IllegalArgumentException ex) {
                Assertions.assertEquals(ex.getMessage(), "invalid_event_data");
                throw ex;
            }
        });
    }

    @Test
    void testEventStreamServiceLimitFailed(VertxTestContext aContext) throws DomainErrorException, InterruptedException {
        final List<AbstractDomainEvent> events = buildTestEvents();
        events.addAll(buildTestEvents());

        final MockEventStreamServiceClientHandler eventStreamHandler = new MockEventStreamServiceClientHandler(vertx);
        eventStreamHandler
                .put(events)
                .handle((aResult, aError) -> {
                    if (Objects.isNull(aError)) {
                        aContext.failNow(new Exception("Error should raised"));
                        return CompletableFuture.completedFuture(null);
                    }
                    Assertions.assertEquals(aError.getMessage(), "Invalid Records");
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(null);
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    private List<AbstractDomainEvent> buildTestEvents() throws DomainErrorException {
        final List<AbstractDomainEvent> events = new ArrayList<>();

        final SessionStarted sessionStarted = new SessionStarted.Builder()
                .setVersion(AbstractDomainEvent.VERSION)
                .setId(UUID.randomUUID().toString())
                .setCountryCode(new CountryCode(VALUE_COUNTRY_CODE))
                .setAppId(new ApplicationId(VALUE_APP_ID))
                .setUserId(new UserId(VALUE_USER_1))
                .setSessionId(VALUE_SESSION_1)
                .setOccurredOn(new Date().getTime())
                .setPlatform(new Platform(VALUE_PLATFORM))
                .setBuild(VALUE_BUILD)
                .get();
        events.add(sessionStarted);

        final SessionEnded sessionEnded = new SessionEnded.Builder()
                .setVersion(AbstractDomainEvent.VERSION)
                .setId(UUID.randomUUID().toString())
                .setCountryCode(new CountryCode(VALUE_COUNTRY_CODE))
                .setAppId(new ApplicationId(VALUE_APP_ID))
                .setUserId(new UserId(VALUE_USER_1))
                .setSessionId(VALUE_SESSION_1)
                .setOccurredOn(new Date().getTime())
                .setPlatform(new Platform(VALUE_PLATFORM))
                .setBuild(VALUE_BUILD)
                .setDuration(VALUE_DURATION)
                .get();
        events.add(sessionEnded);

        final TextMessageSent textMessageSent = new TextMessageSent.Builder()
                .setVersion(AbstractDomainEvent.VERSION)
                .setId(UUID.randomUUID().toString())
                .setCountryCode(new CountryCode(VALUE_COUNTRY_CODE))
                .setAppId(new ApplicationId(VALUE_APP_ID))
                .setUserId(new UserId(VALUE_USER_1))
                .setSessionId(VALUE_SESSION_1)
                .setOccurredOn(new Date().getTime())
                .setPlatform(new Platform(VALUE_PLATFORM))
                .setBuild(VALUE_BUILD)
                .setContent(VALUE_TEXT_CONTENT)
                .setChannelId(VALUE_CHANNEL_ID)
                .setNick(VALUE_USER_NICK)
                .get();
        events.add(textMessageSent);

        final ProductPurchased productPurchased = new ProductPurchased.Builder()
                .setVersion(AbstractDomainEvent.VERSION)
                .setId(UUID.randomUUID().toString())
                .setCountryCode(new CountryCode(VALUE_COUNTRY_CODE))
                .setAppId(new ApplicationId(VALUE_APP_ID))
                .setUserId(new UserId(VALUE_USER_1))
                .setSessionId(VALUE_SESSION_1)
                .setOccurredOn(new Date().getTime())
                .setPlatform(new Platform(VALUE_PLATFORM))
                .setBuild(VALUE_BUILD)
                .setAmountInCents(VALUE_AMOUNT)
                .setProductId(VALUE_PRODUCT_ID)
                .setReceipt(VALUE_RECEIPT)
                .get();
        events.add(productPurchased);


        final TextMessageTranslated textMessageTranslated = new TextMessageTranslated.Builder()
                .setVersion(AbstractDomainEvent.VERSION)
                .setId(UUID.randomUUID().toString())
                .setCountryCode(new CountryCode(VALUE_COUNTRY_CODE))
                .setAppId(new ApplicationId(VALUE_APP_ID))
                .setUserId(new UserId(VALUE_USER_1))
                .setSessionId(VALUE_SESSION_1)
                .setOccurredOn(new Date().getTime())
                .setPlatform(new Platform(VALUE_PLATFORM))
                .setBuild(VALUE_BUILD)
                .setFrom(VALUE_FROM)
                .setTo(VALUE_TO)
                .setMessageId(VALUE_MESSAGE_ID)
                .setLength(VALUE_LENGTH)
                .get();
        events.add(textMessageTranslated);

        return events;
    }
}
