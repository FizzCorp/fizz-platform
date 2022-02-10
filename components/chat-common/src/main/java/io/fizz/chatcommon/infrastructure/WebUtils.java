package io.fizz.chatcommon.infrastructure;

import com.google.gson.Gson;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.DomainErrorException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Objects;

public final class WebUtils {
    private static final LoggingService.Log logger = LoggingService.getLogger(WebUtils.class);

    public static class Error {
        final String reason;
        final List<NotificationError> errors;

        public Error(final String aReason, final List<NotificationError> aErrors) {
            if (Objects.isNull(aReason)) {
                throw new IllegalArgumentException("invalid reason specified.");
            }

            reason = aReason;
            if (Objects.isNull(aErrors) || aErrors.size() <= 0) {
                errors = null;
            }
            else {
                errors = aErrors;
            }
        }
    }

    public static class NotificationError {
        final int id;
        final String reason;

        public NotificationError(final int aId, final String aReason) {
            id = aId;
            reason = aReason;
        }
    }

    public static final String KEY_CONTENT_TYPE = "content-type";
    public static final String CONTENT_JSON = "application/json";
    public static final String CONTENT_TEXT = "text";
    public static final int STATUS_OK = 200;
    public static final String KEY_ERROR_STATUS = "status";
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;
    public static final int STATUS_CONFLICT = 409;
    public static final int STATUS_RATE_LIMIT = 429;

    public static void doOK(final HttpServerResponse aResponse, final JsonObject aBody) {
        doOK(aResponse, CONTENT_JSON, aBody.toString());
    }

    public static void doOK(final HttpServerResponse aResponse, final JsonArray aBody) {
        doOK(aResponse, CONTENT_JSON, aBody.toString());
    }

    public static void doOK(final HttpServerResponse aResponse, final String aBody) {
        doOK(aResponse, CONTENT_TEXT, aBody);
    }

    public static void doOK(final HttpServerResponse aResponse, final String header, final String aBody) {
        if (aResponse.closed()) {
            return;
        }

        aResponse.putHeader(KEY_CONTENT_TYPE, header);
        aResponse.setStatusCode(STATUS_OK);

        if (aBody != null) {
            aResponse.end(aBody);
        }
        else {
            aResponse.end();
        }
    }

    public static void doError(final HttpServerResponse aResponse, int aCode, final JsonObject aBody) {
        doError(aResponse, aCode, CONTENT_JSON, Objects.isNull(aBody) ? null : aBody.toString());
    }

    public static void doError(final HttpServerResponse aResponse, int aCode, final String header, final String aBody) {
        if (aResponse.closed()) {
            return;
        }

        aResponse.putHeader(KEY_CONTENT_TYPE, header);
        aResponse.setStatusCode(aCode);
        if (aBody != null) {
            aResponse.end(aBody);
        }
        else {
            aResponse.end();
        }
    }

    public static void doErrorWithReason(final HttpServerResponse aResponse,
                                         final int aCode,
                                         final String aReason,
                                         final List<NotificationError> aNotification) {
        doError(aResponse, aCode, CONTENT_JSON, new Gson().toJson(new Error(aReason, aNotification)));
    }

    public static void doDomainError(final HttpServerResponse aResponse,
                                     final Throwable aError) {
        doErrorWithReason(
            aResponse,
            STATUS_BAD_REQUEST,
            ((DomainErrorException)aError).error().reason(),
            null
        );
    }

    public static void doErrorWithReason(final HttpServerResponse aResponse,
                                         final int aCode,
                                         final String aReason) {
        doErrorWithReason(
            aResponse,
            aCode,
            aReason,
            null
        );
    }

    public static void logError(String message) {
        logger.error(message);
    }
}
