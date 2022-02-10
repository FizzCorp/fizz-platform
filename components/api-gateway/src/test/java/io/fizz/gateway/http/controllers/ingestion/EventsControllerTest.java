package io.fizz.gateway.http.controllers.ingestion;

import com.google.gson.Gson;
import io.fizz.common.ConfigService;
import io.fizz.common.domain.events.AbstractDomainEvent;
import io.fizz.common.domain.events.SessionEnded;
import io.fizz.gateway.http.MockApplication;
import io.fizz.gateway.http.controllers.TestUtils;
import io.fizz.gateway.http.models.PostEvent;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AuthApi;
import io.swagger.client.api.IngestionApi;
import io.swagger.client.model.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class EventsControllerTest extends AbstractEventsControllerTest {

    private static String sessionToken;
    private static final String APP_ID = "appA";
    private static final String USER_ID = "userA";

    static private Future<String> deployVertex() {
        Future<String> future = Future.future();
        vertx.deployVerticle(MockApplication.class.getName(), future);
        return future;
    }

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws InterruptedException {
        vertx = Vertx.vertx();
        port = ConfigService.instance().getNumber("http.port").intValue();

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

    @BeforeEach
    void init() throws ApiException {
        if (Objects.nonNull(sessionToken)) {
            return;
        }
        final AuthApi api = new AuthApi(new ApiClient());
        final SessionAuthRequest request = new SessionAuthRequest();

        request.setUserId(USER_ID);
        request.setAppId(APP_ID);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );

        sessionToken = api.createSession(request).getToken();
    }

    @Test
    @DisplayName("it should publish events to ingestion service")
    void publishEventsTest() throws ApiException {
        final IngestionApi client = new IngestionApi(new ApiClient());
        final List<AbstractEvent> events = new ArrayList<>();

        final SessionStarted sessionStarted = new SessionStarted();
        sessionStarted.setUserId(USER_ID);
        sessionStarted.setType(EventType.SESSION_STARTED);
        sessionStarted.setVer(1);
        sessionStarted.setSessionId("session_a");
        sessionStarted.setTime(BigDecimal.valueOf(new Date().getTime()));
        events.add(sessionStarted);

        final TextMsgSent messageSent = new TextMsgSent();
        messageSent.setUserId(USER_ID);
        messageSent.setType(EventType.TEXT_MSG_SENT);
        messageSent.setVer(1);
        messageSent.setSessionId("session_a");
        messageSent.setTime(BigDecimal.valueOf(new Date().getTime()));
        messageSent.setContent("this is a test message");
        messageSent.setChannelId("channelA");
        events.add(messageSent);

        final ProductPurchased productPurchased = new ProductPurchased();
        productPurchased.setUserId(USER_ID);
        productPurchased.setType(EventType.PRODUCT_PURCHASED);
        productPurchased.setVer(1);
        productPurchased.setSessionId("session_a");
        productPurchased.setTime(BigDecimal.valueOf(new Date().getTime()));
        productPurchased.setProductId("product_a");
        productPurchased.amount(10.0);
        productPurchased.receipt("receipt_a");
        productPurchased.setCurrency("usd");
        events.add(productPurchased);

        final io.swagger.client.model.SessionEnded sessionEnded = new io.swagger.client.model.SessionEnded();
        sessionEnded.setUserId(USER_ID);
        sessionEnded.setType(EventType.SESSION_ENDED);
        sessionEnded.setVer(1);
        sessionEnded.setSessionId("session_a");
        sessionEnded.setTime(BigDecimal.valueOf(new Date().getTime()));
        sessionEnded.setDuration(100);
        events.add(sessionEnded);

        String payload = new Gson().toJson(events);
        String signature = TestUtils.createSignature(payload, "secret");

        client.getApiClient().addDefaultHeader("Authorization", "HMAC-SHA256 " + signature);
        client.submitEventsWithDigest(APP_ID, events);
    }

    @Test
    @DisplayName("it should send an error if an array of json objects is not sent")
    void invalidEventsArrayTest(VertxTestContext aContext) throws InterruptedException {
        postEventsRequest(
                "{}",
                res -> {
                    res.bodyHandler(body -> {
                        final JsonObject reason = new JsonObject(body.toString());
                        Assertions.assertEquals(reason.getString("reason"), "invalid_request_body");
                        Assertions.assertEquals(res.statusCode(), HttpStatus.SC_BAD_REQUEST);
                        aContext.completeNow();
                    });
                },
                aContext::failNow
        );
        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should send a success code if an empty list is sent")
    void emptyEventsArrayTest(VertxTestContext aContext) throws InterruptedException {
        postEventsRequest(
            "[]",
            res -> {
                Assertions.assertEquals(HttpStatus.SC_OK, res.statusCode());
                aContext.completeNow();
            },
            aContext::failNow
        );

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should return error for invalid app id")
    void invalidAppIdTest(VertxTestContext aContext) throws InterruptedException {
        postEventsRequest(
            "{}",
            "/v1/apps/" + LARGE_ID_65 + "/events",
            resp -> {
                resp.bodyHandler(buffer -> {
                    Assertions.assertEquals(resp.statusCode(), HttpStatus.SC_UNAUTHORIZED);
                    final JsonObject body = new JsonObject(buffer);
                    Assertions.assertEquals(body.getString("reason"), "unknown_app");
                    aContext.completeNow();
                });
            },
            aContext::failNow
        );

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Nested
    @DisplayName("when specifying invalid event type")
    class InvalidEventTypeTestSuite {
        @Test
        @DisplayName("it should return an error when sending missing event type")
        void missingEventTypeTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(new PostEvent().setUser_id("userA")),
                    res -> {
                        res.bodyHandler(body -> {
                            validateSingleInvalidEventData(res, body, HttpStatus.SC_BAD_REQUEST, "invalid_event_type");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return error for invalid event type")
        void invalidEventTypeTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(new PostEvent().setType("invalid_event_type")),
                    response -> {
                        response.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(response, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_event_type");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("when specifying invalid user id")
    class InvalidUserIdTestSuite {
        @Test
        @DisplayName("it should return an error for missing user id")
        void missingUserIdTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(new PostEvent().setType("session_started")),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_user_id");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );

            Assertions.assertTrue(aContext.awaitCompletion(5, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return an error for empty user id")
        void emptyUserIdTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(new PostEvent().setType("session_started").setUser_id("")),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_user_id");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );

            Assertions.assertTrue(aContext.awaitCompletion(5, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return an error for empty user id greate than 64 characters")
        void userIdLargerThanMaxBoundTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(new PostEvent().setType("session_started").setUser_id(LARGE_ID_65)),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_user_id");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );

            Assertions.assertTrue(aContext.awaitCompletion(5, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("when specifying invalid event version")
    class InvalidEventVersionTestSuite {
        @Test
        @DisplayName("it should return an error for missing event version")
        void missingEventVersionTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("session_started")
                    .setUser_id("userA")
                    .setSession_id("session_1")
                    .setTime(new Date().getTime())
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_event_ver");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return an error for negative event version")
        void negativeEventVersionTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("session_started")
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setVer(-1)
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_event_ver");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return an error for zero event version value")
        void zeroEventVersionTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("session_started")
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setVer(0)
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_event_ver");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return an error for unsupported event version")
        void unsupportedEventVersionTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("session_started")
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setVer(AbstractDomainEvent.VERSION + 1)
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_event_ver");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("when specifying invalid session id")
    class InvalidSessionIdTestSuite {
        @Test
        @DisplayName("it should send an error for missing session id")
        void missingSessionId(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("session_started")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setTime(new Date().getTime())
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_session_id");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for empty session id")
        void emptySessionId(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("session_started")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setTime(new Date().getTime())
                    .setSession_id("")
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_session_id");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid session id")
        void invalidSessionId(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("session_started")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setTime(new Date().getTime())
                    .setSession_id(LARGE_ID_65)
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_session_id");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }
    }


    @Nested
    @DisplayName("when specifying invalid event time")
    class InvalidEventTimeTestSuite {
        @Test
        @DisplayName("it should return an error for missing event time")
        void missingEventTime(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("session_started")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setSession_id("session_1")
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_timestamp");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("when specifying invalid platform")
    class InvalidPlatformTestSuite {
        @Test
        @DisplayName("it should return an error for unsupported platform")
        void unsupportedPlatform(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("session_started")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setSession_id("session_1")
                    .setTime(new Date().getTime())
                    .setPlatform("unsupported_platform")
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "unsupported_platform");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("when specifying an invalid build")
    class InvalidBuildTestSuite {
        @Test
        @DisplayName("it should return an error for invalid build")
        void unsupportedPlatform(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("session_started")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setSession_id("session_1")
                    .setTime(new Date().getTime())
                    .setPlatform("ios")
                    .setBuild(LARGE_ID_33)
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_build");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("when specifying an invalid custom dimension")
    class CustomDimensionTestSuite {
        @Test
        @DisplayName("it should return an error for invalid custom dimension 01")
        void validCharsetCustomDim01(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                            new PostEvent()
                                    .setType("session_started")
                                    .setVer(AbstractDomainEvent.VERSION)
                                    .setUser_id("userA")
                                    .setSession_id("session_1")
                                    .setTime(new Date().getTime())
                                    .setPlatform("ios")
                                    .setBuild("build_1_0_11")
                                    .setCustom_01("Test/._-01")
                    ),
                    res -> {
                        Assertions.assertEquals(HttpStatus.SC_OK, res.statusCode());
                        aContext.completeNow();
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return an error for invalid custom dimension 01")
        void invalidLengthCustomDim01(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("session_started")
                        .setVer(AbstractDomainEvent.VERSION)
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setPlatform("ios")
                        .setBuild("build_1_0_11")
                        .setCustom_01(LARGE_ID_65)
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_custom_01");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return an error for invalid custom dimension 02")
        void invalidLengthCustomDim02(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("session_started")
                        .setVer(AbstractDomainEvent.VERSION)
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setPlatform("ios")
                        .setBuild("build_1_0_11")
                        .setCustom_02(LARGE_ID_65)
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_custom_02");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return an error for invalid custom dimension 03")
        void invalidLengthCustomDim03(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("session_started")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setSession_id("session_1")
                    .setTime(new Date().getTime())
                    .setPlatform("ios")
                    .setBuild("build_1_0_11")
                    .setCustom_03(LARGE_ID_65)
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_custom_03");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return an error for invalid custom dimension 01")
        void invalidCharsetCustomDim01(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                            new PostEvent()
                                    .setType("session_started")
                                    .setVer(AbstractDomainEvent.VERSION)
                                    .setUser_id("userA")
                                    .setSession_id("session_1")
                                    .setTime(new Date().getTime())
                                    .setPlatform("ios")
                                    .setBuild("build_1_0_11")
                                    .setCustom_01("Test?")
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_custom_01");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return an error for invalid custom dimension 02")
        void invalidCharsetCustomDim02(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                            new PostEvent()
                                    .setType("session_started")
                                    .setVer(AbstractDomainEvent.VERSION)
                                    .setUser_id("userA")
                                    .setSession_id("session_1")
                                    .setTime(new Date().getTime())
                                    .setPlatform("ios")
                                    .setBuild("build_1_0_11")
                                    .setCustom_02("test,")
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_custom_02");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should return an error for invalid custom dimension 03")
        void invalidCharsetCustomDim03(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                            new PostEvent()
                                    .setType("session_started")
                                    .setVer(AbstractDomainEvent.VERSION)
                                    .setUser_id("userA")
                                    .setSession_id("session_1")
                                    .setTime(new Date().getTime())
                                    .setPlatform("ios")
                                    .setBuild("build_1_0_11")
                                    .setCustom_03("test!")
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_custom_03");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("when specifying an invalid session duration")
    class InvalidSessionDurationTestSuite {
        @Test
        @DisplayName("it should not return an error for invalid min bound duration")
        void invalidMinBoundDuration(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("session_ended")
                        .setVer(AbstractDomainEvent.VERSION)
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setDuration(SessionEnded.MIN_DURATION - 1)
                    ),
                    res -> {
                        Assertions.assertEquals(HttpStatus.SC_OK, res.statusCode());
                        aContext.completeNow();
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should not return an error for invalid max bound duration")
        void invalidMaxBoundDuration(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("session_ended")
                        .setVer(AbstractDomainEvent.VERSION)
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setDuration(SessionEnded.MAX_DURATION + 1)
                    ),
                    res -> {
                        Assertions.assertEquals(HttpStatus.SC_OK, res.statusCode());
                        aContext.completeNow();
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("when specifying invalid data for a TextMessagesSent event")
    class InvalidTextMessageSentEvenTestSuite {
        @Test
        @DisplayName("it should send an error when text message content is not specified")
        void missingTextMessageContent(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("text_msg_sent")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setSession_id("session_1")
                    .setTime(new Date().getTime())
                    .setChannel_id("channel_1")
                    .setNick("userA")
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_message_content");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error when channel id is not speicifed")
        void missingChannelIdTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                        new PostEvent()
                        .setType("text_msg_sent")
                        .setVer(AbstractDomainEvent.VERSION)
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setContent("test message")
                        .setNick("userA")
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_channel_id");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error when empty channel id is specified")
        void emptyChannelIdTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("text_msg_sent")
                        .setVer(AbstractDomainEvent.VERSION)
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setContent("test message")
                        .setNick("userA")
                        .setChannel_id("")
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_channel_id");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error when invalid channel id is specified")
        void invalidChannelIdTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("text_msg_sent")
                        .setVer(AbstractDomainEvent.VERSION)
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setContent("test message")
                        .setNick("userA")
                        .setChannel_id(LARGE_ID_65)
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_channel_id");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error when sending invalid nick")
        void invalidNickTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("text_msg_sent")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setSession_id("session_1")
                    .setTime(new Date().getTime())
                    .setContent("test message")
                    .setNick(LARGE_ID_33)
                    .setChannel_id("channel_a")
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_nick");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("when specifying invalid data for a ProductPurchased event")
    class InvalidProductPurchasedEventTestSuite {
        @Test
        @DisplayName("it should send an error when product amount is missing")
        void missingPurchaseAmountTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("product_purchased")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setSession_id("session_1")
                    .setTime(new Date().getTime())
                    .setProduct_id("product_1")
                    .setReceipt("receipt")
                    .setCurrency("USD")
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_purchase_amount");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error when product amount is invalid")
        void invalidPurchaseAmountTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("product_purchased")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setSession_id("session_1")
                    .setTime(new Date().getTime())
                    .setProduct_id("product_1")
                    .setReceipt("receipt")
                    .setCurrency("USD")
                    .setAmount(-100)
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_purchase_amount");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error when the product id is missing")
        void missingProductIdTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("product_purchased")
                        .setVer(AbstractDomainEvent.VERSION)
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setCurrency("USD")
                        .setAmount(100)
                        .setReceipt("receipt")
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_product_id");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error when the product id is invalid")
        void invalidProductIdTest(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                toJson(
                    new PostEvent()
                    .setType("product_purchased")
                    .setVer(AbstractDomainEvent.VERSION)
                    .setUser_id("userA")
                    .setSession_id("session_1")
                    .setTime(new Date().getTime())
                    .setCurrency("USD")
                    .setAmount(100)
                    .setProduct_id(LARGE_ID_65)
                    .setReceipt("receipt")
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_product_id");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error when the currency code is missing")
        void missingCurrencyCode(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("product_purchased")
                        .setVer(AbstractDomainEvent.VERSION)
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setAmount(100)
                        .setProduct_id("product_1")
                        .setReceipt("receipt")
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_currency_code");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error when the currency code is empty")
        void emptyCurrencyCode(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                        new PostEvent()
                        .setType("product_purchased")
                        .setVer(AbstractDomainEvent.VERSION)
                        .setUser_id("userA")
                        .setSession_id("session_1")
                        .setTime(new Date().getTime())
                        .setAmount(100)
                        .setProduct_id("product_1")
                        .setReceipt("receipt")
                        .setCurrency("")
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_currency_code");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error when the currency code is invalid")
        void invalidCurrencyCode(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                            new PostEvent()
                                    .setType("product_purchased")
                                    .setVer(AbstractDomainEvent.VERSION)
                                    .setUser_id("userA")
                                    .setSession_id("session_1")
                                    .setTime(new Date().getTime())
                                    .setAmount(100)
                                    .setProduct_id("product_1")
                                    .setReceipt("receipt")
                                    .setCurrency("ABCD")
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "invalid_currency_code");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error when the currency code is unsupported")
        void unsupportedCurrencyCode(VertxTestContext aContext) throws InterruptedException {
            postEventsRequest(
                    toJson(
                            new PostEvent()
                                    .setType("product_purchased")
                                    .setVer(AbstractDomainEvent.VERSION)
                                    .setUser_id("userA")
                                    .setSession_id("session_1")
                                    .setTime(new Date().getTime())
                                    .setAmount(100)
                                    .setProduct_id("product_1")
                                    .setReceipt("receipt")
                                    .setCurrency("XYZ")
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateSingleInvalidEventData(resp, buffer, HttpStatus.SC_BAD_REQUEST, "unsupported_currency_code");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }
    }
}
