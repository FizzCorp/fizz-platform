package io.fizz.chat.infrastructure.services;

import io.fizz.chat.application.AbstractApplicationService;
import io.fizz.chat.application.FCMConfiguration;
import io.fizz.chat.application.services.AbstractPushNotificationService;
import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.chat.user.application.UserDTO;
import io.fizz.chat.user.application.service.AbstractUserApplicationService;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FCMPushNotificationService implements AbstractPushNotificationService
{
    private static final LoggingService.Log log = LoggingService.getLogger(FCMPushNotificationService.class);

    private final WebClient client;
    private final AbstractApplicationService appService;
    private final AbstractUserApplicationService userService;

    public FCMPushNotificationService(final Vertx aVertx,
                                      final AbstractApplicationService aAppService,
                                      final AbstractUserApplicationService aUserService) {
        Utils.assertRequiredArgument(aVertx, "invalid_vertx_instance");
        Utils.assertRequiredArgument(aAppService, "invalid_app_service");
        Utils.assertRequiredArgument(aUserService, "invalid_user_repo");

        this.client = WebClient.create(aVertx);
        this.appService = aAppService;
        this.userService = aUserService;
    }

    @Override
    public CompletableFuture<Void> send(final ApplicationId aAppId,
                                        final ChannelMessage aMessage,
                                        final Set<UserId> aNotifyList) {
        Utils.assertRequiredArgument(aAppId, "invalid_app_id");
        Utils.assertRequiredArgument(aMessage, "invalid_message");
        Utils.assertRequiredArgument(aNotifyList, "invalid_notify_list");

        if (aNotifyList.size() <= 0) {
            return CompletableFuture.completedFuture(null);
        }

        final CompletableFuture<FCMConfiguration> credentialsFetched = fetchCredentials(aAppId);

        return credentialsFetched.thenCompose(aConfiguration -> {
            if (Objects.isNull(aConfiguration)) {
                log.warn("missing push notification configuration for app: " + aAppId.value());
                return CompletableFuture.completedFuture(null);
            }

            final CompletableFuture<Set<String>> tokensFetched = fetchTokens(aAppId, aNotifyList);

            return tokensFetched.thenCompose(aTokens -> notify(aConfiguration, aTokens, aMessage));
        });
    }

    private CompletableFuture<FCMConfiguration> fetchCredentials(final ApplicationId aAppId) {
        return appService.getFCMConfig(aAppId);
    }

    private CompletableFuture<Set<String>> fetchTokens(final ApplicationId aAppId, final Set<UserId> aNotifyList) {
        final List<CompletableFuture<UserDTO>> usersFetched = new ArrayList<>(aNotifyList.size());

        for (UserId id: aNotifyList) {
            usersFetched.add(userService.fetchUser(aAppId, id));
        }

        return CompletableFuture.allOf(usersFetched.toArray(new CompletableFuture[0]))
                .thenApply(aVoid -> {
                    final Set<String> tokens = new HashSet<>();
                    for (CompletableFuture<UserDTO> userFetched: usersFetched) {
                        tokens.add(userFetched.join().token());
                    }

                    return tokens;
                });
    }

    private CompletableFuture<Void> notify(final FCMConfiguration aConfiguration,
                                           final Set<String> aTokens,
                                           final ChannelMessage aMessage) {
        if (aTokens.size() <= 0) {
            return CompletableFuture.completedFuture(null);
        }

        final CompletableFuture<Void> sent = new CompletableFuture<>();
        final JsonObject payload = buildPayload(aTokens, aMessage);

        client.post(443,"fcm.googleapis.com", "/fcm/send")
                .ssl(true)
                .putHeader("Authorization", "key=" + aConfiguration.secret())
                .sendJson(payload, aResult-> {
                    if (aResult.succeeded()) {
                        sent.complete(null);
                    }
                    else {
                        sent.completeExceptionally(aResult.cause());
                    }
                });

        return sent;
    }

    private JsonObject buildPayload(final Set<String> aTokens, final ChannelMessage aMessage) {
        final JsonArray registrationIds = new JsonArray();
        for (String token: aTokens) {
            registrationIds.add(token);
        }

        final JsonObject notification = new JsonObject();
        notification.put("title", aMessage.nick());
        notification.put("body",  aMessage.body());

        final JsonObject payload = new JsonObject();
        payload.put("registration_ids", registrationIds);
        payload.put("notification", notification);

        return payload;
    }
}
