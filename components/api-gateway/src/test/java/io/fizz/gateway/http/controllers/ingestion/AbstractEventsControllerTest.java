package io.fizz.gateway.http.controllers.ingestion;

import com.google.gson.Gson;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.controllers.TestUtils;
import io.fizz.gateway.http.models.PostEvent;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;

import java.util.Objects;

public abstract class AbstractEventsControllerTest {
    static int TEST_TIMEOUT = Constants.TEST_TIMEOUT;
    static Vertx vertx;
    static int port;
    static final String LARGE_ID_65 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-+1";
    static final String LARGE_ID_33 = "0123456789abcdefghijklmnopqrstuvw";

    void validateSingleInvalidEventData(final HttpClientResponse aResponse, final Buffer aBodyBuffer, int aHttpCode, final String aReason) {
        Assertions.assertEquals(aResponse.statusCode(), aHttpCode);

        final JsonObject body = new JsonObject(aBodyBuffer);
        Assertions.assertEquals(body.getString("reason"), "invalid_event_data");

        final JsonArray errors = body.getJsonArray("errors");
        Assertions.assertEquals(errors.size(), 1);

        final JsonObject error = errors.getJsonObject(0);
        Assertions.assertEquals(error.getString("reason"), aReason);
        Assertions.assertEquals((int)error.getInteger("id"), 0);
    }

    void postEventsRequest(final String aBody,
                           final Handler<HttpClientResponse> aResponseHandler,
                           final Handler<Throwable> aErrorHandler) {
        postEventsRequest(aBody, "/v1/apps/appA/events", aResponseHandler, aErrorHandler);
    }

    void postEventsRequest(final String aBody,
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

    String toJson(final PostEvent aEvent) {
        return "[" + new Gson().toJson(aEvent) + "]";
    }
}
