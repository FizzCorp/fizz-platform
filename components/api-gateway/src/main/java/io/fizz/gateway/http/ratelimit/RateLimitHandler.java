package io.fizz.gateway.http.ratelimit;

import com.newrelic.api.agent.NewRelic;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.Utils;
import io.fizz.gateway.http.annotations.RLKeyType;
import io.fizz.gateway.http.annotations.RLScope;
import io.fizz.gateway.http.auth.AuthenticatedUser;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

public class RateLimitHandler implements Handler<RoutingContext> {

    private final RLScope scope;
    private final RLKeyType keyType;
    private final String keyName;
    private final RateLimitService service;

    public RateLimitHandler(final RLScope aScope,
                            final RLKeyType aType,
                            final String aKeyName,
                            final RateLimitService aRateLimitService) {
        Utils.assertRequiredArgument(aScope, "invalid rate limit scope specified");
        Utils.assertRequiredArgument(aType, "invalid rate limit key type specified");
        Utils.assertRequiredArgument(aKeyName, "invalid rate limit key name specified");
        Utils.assertRequiredArgument(aRateLimitService, "invalid rate limit service specified");

        scope = aScope;
        keyType = aType;
        keyName = aKeyName;
        service = aRateLimitService;
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final String appId = AuthenticatedUser.appId(aContext.user());
        final String userId = AuthenticatedUser.userId(aContext.user());
        String keyValue = parseContextValue(aContext);

        service.handle(appId, userId, scope, keyValue)
                .thenAccept((allowed) -> {
                    if (allowed) {
                        aContext.next();
                    } else {
                        NewRelic.noticeError("Too Many Requests", true);
                        WebUtils.doErrorWithReason(
                                aContext.response(),
                                WebUtils.STATUS_RATE_LIMIT,
                                "Too Many Requests",
                                null
                        );
                    }
                });
    }

    private String parseContextValue(final RoutingContext aContext) {
        if (Objects.isNull(keyName) || keyName.isEmpty()) {
            return "";
        }

        String keyValue = "";
        if (keyType == RLKeyType.PATH) {
            keyValue = aContext.request().getParam(keyName);
        }
        else if (keyType == RLKeyType.BODY) {
            final JsonObject body = aContext.getBodyAsJson();
            keyValue = body.getString(keyName);
        }
        return keyValue;
    }
}
