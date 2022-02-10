package io.fizz.gateway.http.auth;

import com.newrelic.api.agent.NewRelic;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.session.SessionUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.Objects;

public class SessionAuthHandler implements Handler<RoutingContext> {
    private final SessionStore store;

    public SessionAuthHandler(final SessionStore aStore) {
        if (Objects.isNull(aStore)) {
            throw new IllegalArgumentException("invalid session store specified.");
        }

        store = aStore;
    }

    @Override
    public void handle(RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final String token = aContext.request().headers().get("Session-Token");

        if (Objects.isNull(token)) {
            NewRelic.noticeError("missing_session_token", true);
            WebUtils.doErrorWithReason(
                response,
                WebUtils.STATUS_UNAUTHORIZED,
                "missing_session_token",
                null
            );
            return;
        }

        store.get(token, aResult -> {
            if (aResult.succeeded()) {
                final Session session = aResult.result();
                if (Objects.isNull(session)) {
                    NewRelic.noticeError("invalid_session", true);
                    WebUtils.doErrorWithReason(
                        response,
                        WebUtils.STATUS_UNAUTHORIZED,
                        "invalid_session",
                        null
                    );
                }
                else {
                    aContext.setUser(new AuthenticatedUser(SessionUtils.getAppId(session), SessionUtils.getUserId(session)));
                    aContext.setSession(session);
                    aContext.next();
                }
            }
            else {
                NewRelic.noticeError(aResult.cause());
                WebUtils.doErrorWithReason(
                    response,
                    WebUtils.STATUS_INTERNAL_SERVER_ERROR,
                    "session_fetch_failed",
                    null
                );
            }
        });
    }
}
