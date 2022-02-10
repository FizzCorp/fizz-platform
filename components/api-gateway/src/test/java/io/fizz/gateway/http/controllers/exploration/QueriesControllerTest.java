package io.fizz.gateway.http.controllers.exploration;

import com.google.gson.Gson;
import io.fizz.common.ConfigService;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.fizz.gateway.http.controllers.TestUtils;
import io.fizz.gateway.http.models.MetricsQueryRequest;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AuthApi;
import io.swagger.client.model.SessionAuthRequest;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class QueriesControllerTest {
    private static int TEST_TIMEOUT = Constants.TEST_TIMEOUT;
    private static Vertx vertx;
    private static int port;
    private static String sessionToken;
    private static final String APP_ID = "appA";
    private static final String LARGE_ID_65 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-+1";

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

        request.setUserId("userA");
        request.setAppId(APP_ID);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );
        sessionToken = api.createSession(request).getToken();
    }

    @Nested
    @DisplayName("when running metrics query")
    class MetricsQueryTestSuite {
        @Test
        @DisplayName("it should send an error for invalid app id")
        void invalidRequestBodyTest(VertxTestContext aContext) throws InterruptedException {
            postRequest(
                "invalid_body",
                "/v1/apps/appA/queries/metrics",
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateErrorResponse(buffer, resp.statusCode(), "invalid_request_body");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid app id")
        void invalidAppIdTest(VertxTestContext aContext) throws InterruptedException {
            final String LARGE_APP_ID = "b0128ae5-7e1b-427b-af93-f50cf773ac84d841c598-1fc7-44c4-984b-0c69d";

            postRequest(
                "{}",
                "/v1/apps/" + LARGE_APP_ID + "/queries/metrics",
                resp -> {
                    resp.bodyHandler(buffer -> {
                       validateErrorResponse(buffer, resp.statusCode(), "invalid_app_id");
                       aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for missing query start")
        void missingQueryStartTest(VertxTestContext aContext) throws InterruptedException {
            postMetricsQueryRequest(
                new Gson().toJson(
                    new MetricsQueryRequest(
                        null,
                        new Date().getTime(),
                        new ArrayList<MetricsQueryRequest.Metric>() {
                            {
                                add(new MetricsQueryRequest.Metric("new_users_count_daily"));
                            }
                        }
                    )
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateErrorResponse(buffer, resp.statusCode(), "invalid_query_start");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @DisplayName("it should send an error for invalid query start")
        void invalidQueryStartTest(VertxTestContext aContext) throws InterruptedException {
            postMetricsQueryRequest(
                new Gson().toJson(
                    new MetricsQueryRequest(
                        new Date().getTime(),
                        new Date().getTime(),
                        new ArrayList<MetricsQueryRequest.Metric>() {
                            {
                                add(new MetricsQueryRequest.Metric("new_users_count_daily"));
                            }
                        }
                    )
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateErrorResponse(buffer, resp.statusCode(), "invalid_query_start");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for missing query end")
        void missingQueryEndTest(VertxTestContext aContext) throws InterruptedException {
            postMetricsQueryRequest(
                new Gson().toJson(
                    new MetricsQueryRequest(
                        new Date().getTime()/1000,
                        null,
                        new ArrayList<MetricsQueryRequest.Metric>() {
                            {
                                add(new MetricsQueryRequest.Metric("new_users_count_daily"));
                            }
                        }
                    )
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateErrorResponse(buffer, resp.statusCode(), "invalid_query_end");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid query end")
        void invalidQueryEndTest(VertxTestContext aContext) throws InterruptedException {
            postMetricsQueryRequest(
                new Gson().toJson(
                    new MetricsQueryRequest(
                        new Date().getTime()/1000,
                        new Date().getTime(),
                        new ArrayList<MetricsQueryRequest.Metric>() {
                            {
                                add(new MetricsQueryRequest.Metric("new_users_count_daily"));
                            }
                        }
                    )
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateErrorResponse(buffer, resp.statusCode(), "invalid_query_end");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for missing metrics")
        void missingMetricsTest(VertxTestContext aContext) throws InterruptedException {
            postMetricsQueryRequest(
                new Gson().toJson(
                    new MetricsQueryRequest(
                        new Date().getTime()/1000,
                        new Date().getTime()/1000,
                        null
                    )
                ),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateErrorResponse(buffer, resp.statusCode(), "invalid_metrics");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send success for empty metrics")
        void emptyMetricsTest(VertxTestContext aContext) throws InterruptedException {
            postMetricsQueryRequest(
                new Gson().toJson(
                    new MetricsQueryRequest(
                        new Date().getTime()/1000,
                        new Date().getTime()/1000,
                        new ArrayList<>()
                    )
                ),
                resp -> {
                    Assertions.assertEquals(resp.statusCode(), HttpStatus.SC_OK);
                    aContext.completeNow();
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send error for invalid metric id")
        void invalidMetricIdTest(VertxTestContext aContext) throws InterruptedException {
            postRequest(
                new Gson().toJson(
                    new MetricsQueryRequest(
                        1520640000L,
                        1520899200L,
                        new ArrayList<MetricsQueryRequest.Metric>(){
                            {
                                add(new MetricsQueryRequest.Metric("active_users_count_sliding_monthly"));
                                add(new MetricsQueryRequest.Metric("active_users_count_daily_"));
                            }
                        }
                    )
                ),
                    "/v1/apps/appA/queries/metrics",
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateErrorResponse(buffer, resp.statusCode(), "invalid_metric_id");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send error for empty segment")
        void segmentMetricsTest(VertxTestContext aContext) throws InterruptedException {
            postMetricsQueryRequest(
                    new Gson().toJson(
                            new MetricsQueryRequest(
                                    new Date().getTime()/1000,
                                    new Date().getTime()/1000,
                                    new ArrayList<MetricsQueryRequest.Metric>() {
                                        {
                                            add(new MetricsQueryRequest.Metric("new_users_count_daily"));
                                        }
                                    },
                                    new HashMap<>()
                            )
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_segment");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send error for invalid segment key")
        void segmentMetricsMissingValueTest(VertxTestContext aContext) throws InterruptedException {
            postMetricsQueryRequest(
                    new Gson().toJson(
                            new MetricsQueryRequest(
                                    new Date().getTime()/1000,
                                    new Date().getTime()/1000,
                                    new ArrayList<MetricsQueryRequest.Metric>() {
                                        {
                                            add(new MetricsQueryRequest.Metric("new_users_count_daily"));
                                        }
                                    },
                                    new HashMap<String, String>() {
                                        {
                                            put("cc", "pk");
                                        }
                                    }
                            )
                    ),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_segment");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        private void postMetricsQueryRequest(final String aBody,
                                       final Handler<HttpClientResponse> aResponseHandler,
                                       final Handler<Throwable> aErrorHandler) {
            postRequest(aBody, "/v1/apps/appA/queries/metrics", aResponseHandler, aErrorHandler);
        }
    }

    @Nested
    @DisplayName("when running messages query")
    class MessagesQueryTestSuite {
        @Test
        @DisplayName("it should send an error for invalid app id")
        void invalidRequestBodyTest(VertxTestContext aContext) throws InterruptedException {
            postRequest(
                    "invalid_body",
                    "/v1/apps/appA/queries/metrics",
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_request_body");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid app id")
        void invalidAppIdTest(VertxTestContext aContext) throws InterruptedException {
            final String LARGE_APP_ID = "b0128ae5-7e1b-427b-af93-f50cf773ac84d841c598-1fc7-44c4-984b-0c69d";

            postRequest(
                    "{}",
                    "/v1/apps/" + LARGE_APP_ID + "/queries/metrics",
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_app_id");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for missing query start")
        void missingQueryStartTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                new JsonObject().put("end", new Date().getTime()/100).toString(),
                resp -> {
                    resp.bodyHandler(buffer -> {
                        validateErrorResponse(buffer, resp.statusCode(), "invalid_query_start");
                        aContext.completeNow();
                    });
                },
                aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for when sending query start in MS")
        void millisQueryStartTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                        .put("start", new Date().getTime())
                        .put("end", new Date().getTime()/1000)
                        .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_start");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for when sending query start that is negative")
        void invalidQueryStartTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", -1L)
                            .put("end", new Date().getTime()/1000)
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_start");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for missing query end")
        void missingQueryEndTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject().put("start", new Date().getTime()/1000).toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_end");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for when sending query start in MS")
        void millisQueryEndTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                        .put("start", new Date().getTime()/1000)
                        .put("end", new Date().getTime())
                        .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_end");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for when sending query start that is negative")
        void invalidQueryEndTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                        .put("start", new Date().getTime()/1000)
                        .put("end", -1L)
                        .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_end");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for an invalid query Range")
        void invalidQueryRangeTest(VertxTestContext aContext) throws InterruptedException {
            long time = new Date().getTime()/1000;
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", time)
                            .put("end", time - 10)
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_range");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for when sending an invalid cursor")
        void invalidCursorTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("cursor", "invalid_base64_cursor")
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_request_body");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid search text")
        void invalidSearchTextTest(VertxTestContext aContext) throws InterruptedException {
            StringBuilder largeText = new StringBuilder(LARGE_ID_65);
            for ( int i = 2048 / LARGE_ID_65.length(); i >= 0; i--) {
                largeText.append(LARGE_ID_65);
            }
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("text",largeText.toString())
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_search_text");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid search phrase")
        void invalidSearchPhraseTest(VertxTestContext aContext) throws InterruptedException {
            StringBuilder largeText = new StringBuilder(LARGE_ID_65);
            for ( int i = 2048 / LARGE_ID_65.length(); i >= 0; i--) {
                largeText.append(LARGE_ID_65);
            }
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("phrase", largeText.toString())
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_search_phrase");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid platform")
        void invalidPlatformTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("platform", "unsupported")
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "unsupported_platform");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid platform")
        void invalidAgeSegmentTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("platform", "unsupported")
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "unsupported_platform");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid sentiment score")
        void invalidSentimentScoreTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("sentiment_score", "unsupported")
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_sentiment_score");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for sending a sentiment without score")
        void missingScoreInSentimentTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("sentiment_score", new JsonObject().put("op", "lt"))
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_sentiment_score");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for sending a sentiment without comparison operation")
        void missingOpInSentimentTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("sentiment_score", new JsonObject().put("score", 0.5))
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_sentiment_score");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for sending a sentiment without comparison operation")
        void invalidOpInSentimentTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("sentiment_score", new JsonObject().put("score", 0.5).put("op", "invalid_op"))
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_sentiment_score");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for sending an invalid sort order")
        void invalidSortOrderTest(VertxTestContext aContext) throws InterruptedException {
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("sort", "invalid_sort_order")
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_sort_order");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid page size")
        void invalidPageSizeTest1(VertxTestContext aContext) throws InterruptedException {
            final int minPageSize = ConfigService.instance().getNumber("es.page.size.min").intValue();
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("text", LARGE_ID_65)
                            .put("page_size", minPageSize-1)
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_page_size");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid page size")
        void invalidPageSizeTest2(VertxTestContext aContext) throws InterruptedException {
            final int maxPageSize = ConfigService.instance().getNumber("es.page.size.max").intValue();
            postMessagesQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("text", LARGE_ID_65)
                            .put("page_size", maxPageSize + 1)
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_page_size");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        /*
        @Test
        void test(VertxTestContext aContext) throws InterruptedException {
            final HttpClient client = vertx.createHttpClient();
            final HttpClientRequest req = client.post(
                port,
                "localhost",
                "/v1/apps/04edde8e9900002d/queries/messages",
                resp -> {
                    resp.bodyHandler(buffer -> {
                        System.out.println(buffer);
                        JsonObject json = new JsonObject(buffer);
                        JsonArray items = json.getJsonArray("items");
                        System.out.println(items.size());
                        aContext.completeNow();
                    });
                });
            final JsonObject json = new JsonObject();
            json.put("start", 1526353076L);
            json.put("end", 1526380173L);
            //json.put("cursor", "Dg==");
            final String body = json.toString();
            req.putHeader("content-length", Integer.toString(body.length()));
            req.putHeader("content-type", "application/json");
            req.write(body);
            req.end();

            Assertions.assertTrue(aContext.awaitCompletion(60, TimeUnit.SECONDS));
        }
        */

        private void postMessagesQueryRequest(final String aBody,
                                             final Handler<HttpClientResponse> aResponseHandler,
                                             final Handler<Throwable> aErrorHandler) {
            postRequest(aBody, "/v1/apps/appA/queries/messages", aResponseHandler, aErrorHandler);
        }
    }

    @Nested
    @DisplayName("when running keywords query")
    class KeywordsQueryTestSuite {
        @Test
        @DisplayName("it should send an error for invalid app id")
        void invalidRequestBodyTest(VertxTestContext aContext) throws InterruptedException {
            postRequest(
                    "invalid_body",
                    "/v1/apps/appA/queries/keywords",
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_request_body");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid app id")
        void invalidAppIdTest(VertxTestContext aContext) throws InterruptedException {
            final String LARGE_APP_ID = "b0128ae5-7e1b-427b-af93-f50cf773ac84d841c598-1fc7-44c4-984b-0c69d";

            postRequest(
                    "{}",
                    "/v1/apps/" + LARGE_APP_ID + "/queries/keywords",
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_app_id");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for missing query start")
        void missingQueryStartTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject().put("end", new Date().getTime()/100).toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_start");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for when sending query start in MS")
        void millisQueryStartTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime())
                            .put("end", new Date().getTime()/1000)
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_start");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for when sending query start that is negative")
        void invalidQueryStartTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", -1L)
                            .put("end", new Date().getTime()/1000)
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_start");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for missing query end")
        void missingQueryEndTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject().put("start", new Date().getTime()/1000).toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_end");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for when sending query start in MS")
        void millisQueryEndTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime())
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_end");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for when sending query start that is negative")
        void invalidQueryEndTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", -1L)
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_end");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for an invalid query Range")
        void invalidQueryRangeTest(VertxTestContext aContext) throws InterruptedException {
            long time = new Date().getTime()/1000;
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", time)
                            .put("end", time - 10)
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_query_range");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid search text")
        void invalidSearchTextTest(VertxTestContext aContext) throws InterruptedException {
            StringBuilder largeText = new StringBuilder(LARGE_ID_65);
            for ( int i = 2048 / LARGE_ID_65.length(); i >= 0; i--) {
                largeText.append(LARGE_ID_65);
            }
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("text",largeText.toString())
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_search_text");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid search phrase")
        void invalidSearchPhraseTest(VertxTestContext aContext) throws InterruptedException {
            StringBuilder largeText = new StringBuilder(LARGE_ID_65);
            for ( int i = 2048 / LARGE_ID_65.length(); i >= 0; i--) {
                largeText.append(LARGE_ID_65);
            }
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("phrase", largeText.toString())
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_search_phrase");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid platform")
        void invalidPlatformTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("platform", "unsupported")
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "unsupported_platform");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid platform")
        void invalidAgeSegmentTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("platform", "unsupported")
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "unsupported_platform");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for invalid sentiment score")
        void invalidSentimentScoreTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("sentiment_score", "unsupported")
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_sentiment_score");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for sending a sentiment without score")
        void missingScoreInSentimentTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("sentiment_score", new JsonObject().put("op", "lt"))
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_sentiment_score");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for sending a sentiment without comparison operation")
        void missingOpInSentimentTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("sentiment_score", new JsonObject().put("score", 0.5))
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_sentiment_score");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should send an error for sending a sentiment without comparison operation")
        void invalidOpInSentimentTest(VertxTestContext aContext) throws InterruptedException {
            postKeywordsQueryRequest(
                    new JsonObject()
                            .put("start", new Date().getTime()/1000)
                            .put("end", new Date().getTime()/1000)
                            .put("sentiment_score", new JsonObject().put("score", 0.5).put("op", "invalid_op"))
                            .toString(),
                    resp -> {
                        resp.bodyHandler(buffer -> {
                            validateErrorResponse(buffer, resp.statusCode(), "invalid_sentiment_score");
                            aContext.completeNow();
                        });
                    },
                    aContext::failNow
            );
            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        private void postKeywordsQueryRequest(final String aBody,
                                              final Handler<HttpClientResponse> aResponseHandler,
                                              final Handler<Throwable> aErrorHandler) {
            postRequest(aBody, "/v1/apps/appA/queries/keywords", aResponseHandler, aErrorHandler);
        }
    }

    private void validateErrorResponse(Buffer aBuffer, int aStatusCode, final String aReason) {
        final JsonObject body = new JsonObject(aBuffer);

        Assertions.assertEquals(aStatusCode, HttpStatus.SC_BAD_REQUEST);
        Assertions.assertEquals(body.getString("reason"), aReason);
    }

    private void postRequest(final String aBody,
                             final String aURL,
                             final Handler<HttpClientResponse> aResponseHandler,
                             final Handler<Throwable> aErrorHandler) {
        final HttpClient client = vertx.createHttpClient();

        final HttpClientRequest req = client.post(port, "localhost", aURL, aResponseHandler);
        req.putHeader("content-length", Integer.toString(aBody.length()));
        req.putHeader("content-type", "application/json");
        req.putHeader("Authorization", "HMAC-SHA256 " + TestUtils.createSignature(aBody, "secret"));
        req.write(aBody);
        if (!Objects.isNull(aErrorHandler)) {
            req.exceptionHandler(aErrorHandler);
        }
        req.end();
    }

    private void postRequest(final String aBody,
                             final String aURL,
                             final String aSessionToken,
                             final Handler<HttpClientResponse> aResponseHandler,
                             final Handler<Throwable> aErrorHandler) {
        final HttpClient client = vertx.createHttpClient();

        final HttpClientRequest req = client.post(port, "localhost", aURL, aResponseHandler);
        req.putHeader("content-length", Integer.toString(aBody.length()));
        req.putHeader("content-type", "application/json");
        req.putHeader("Session-Token", aSessionToken);
        req.write(aBody);
        if (!Objects.isNull(aErrorHandler)) {
            req.exceptionHandler(aErrorHandler);
        }
        req.end();
    }
}
