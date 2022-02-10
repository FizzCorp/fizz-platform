package io.fizz.gateway.http;

import io.fizz.gateway.http.controllers.ingestion.AbstractEventsControllerTest;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * This class is used for running manual e2e test and thus relies on external services.
 * Therefore it is disabled by default.
 * */
@ExtendWith(VertxExtension.class)
class EventsSubmissionE2ETest extends AbstractEventsControllerTest {
    /*
    @Test
    @DisplayName("it should submit session_started event with success")
    void sessionStartedEventTest(VertxTestContext aContext) throws InterruptedException {
        postEventsRequest(
                toJson(
                        new PostEvent()
                                .setType("session_started")
                                .setVer(AbstractDomainEvent.VERSION)
                                .setUser_id("userA")
                                .setSession_id("session_1")
                                .setTime(new Date().getTime())
                ),
                resp -> {
                    Assertions.assertEquals(resp.statusCode(), HttpStatus.SC_OK);
                    aContext.completeNow();
                },
                aContext::failNow
        );
        Assertions.assertTrue(aContext.awaitCompletion(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should submit session_ended event with success")
    void sessionEndedEventTest(VertxTestContext aContext) throws InterruptedException {
        postEventsRequest(
                toJson(
                        new PostEvent()
                                .setType("session_ended")
                                .setVer(AbstractDomainEvent.VERSION)
                                .setUser_id("userA")
                                .setSession_id("session_1")
                                .setTime(new Date().getTime())
                                .setDuration(100)
                ),
                resp -> {
                    Assertions.assertEquals(resp.statusCode(), HttpStatus.SC_OK);
                    aContext.completeNow();
                },
                aContext::failNow
        );
        Assertions.assertTrue(aContext.awaitCompletion(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should submit text_msg_sent event with success")
    void textMessageSentEventTest(VertxTestContext aContext) throws InterruptedException {
        postEventsRequest(
                toJson(
                        new PostEvent()
                                .setType("text_msg_sent")
                                .setVer(AbstractDomainEvent.VERSION)
                                .setUser_id("userA")
                                .setSession_id("session_1")
                                .setTime(new Date().getTime())
                                .setContent("test message")
                                .setChannel_id("channel_1")
                                .setNick("userA_nick")
                ),
                resp -> {
                    Assertions.assertEquals(resp.statusCode(), HttpStatus.SC_OK);
                    aContext.completeNow();
                },
                aContext::failNow
        );
        Assertions.assertTrue(aContext.awaitCompletion(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should submit product_purchased event with success")
    void productPurchasedEventTest(VertxTestContext aContext) throws InterruptedException {
        postEventsRequest(
                toJson(
                        new PostEvent()
                                .setType("product_purchased")
                                .setVer(AbstractDomainEvent.VERSION)
                                .setUser_id("userA")
                                .setSession_id("session_1")
                                .setTime(new Date().getTime())
                                .setProduct_id("product_a")
                                .setCurrency("PKR")
                                .setAmount(100)
                ),
                resp -> {
                    System.out.println(resp.toString());
                    System.out.println(resp.statusCode());
                    Assertions.assertEquals(resp.statusCode(), HttpStatus.SC_OK);
                    aContext.completeNow();
                },
                aContext::failNow
        );
        Assertions.assertTrue(aContext.awaitCompletion(5, TimeUnit.SECONDS));
    }
    */
}
