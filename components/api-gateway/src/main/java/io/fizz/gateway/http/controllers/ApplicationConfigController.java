package io.fizz.gateway.http.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fizz.chat.application.FCMConfiguration;
import io.fizz.chat.application.Preferences;
import io.fizz.chat.application.ProviderConfigSerde;
import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.chat.application.domain.AzureProviderConfig;
import io.fizz.chat.application.domain.CleanSpeakProviderConfig;
import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.moderation.application.service.ContentModerationService;
import io.fizz.chat.user.domain.PushPlatform;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.gateway.http.annotations.AsyncRestController;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.gateway.http.auth.AuthenticatedUser;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class ApplicationConfigController extends AbstractRestController  {
    private final ContentModerationService contentModService;
    private final ApplicationService appService;

    public ApplicationConfigController(final Vertx aVertx,
                                       final ContentModerationService aContentModService,
                                       final ApplicationService aAppService) {
        super(aVertx);

        Utils.assertRequiredArgument(aContentModService, "invalid_content_moderation_service");
        Utils.assertRequiredArgument(aAppService, "invalid_app_service");

        contentModService = aContentModService;
        appService = aAppService;
    }

    @AsyncRestController(path="/preferences", method= HttpMethod.POST, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> onSetPreferences(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final ApplicationId appId = appId(aContext);
            final JsonObject body = aContext.getBodyAsJson();

            Preferences prefs = new Preferences();

            if (body.containsKey("force_content_moderation")) {
                prefs.setForceContentModeration(body.getBoolean("force_content_moderation"));
            }

            return appService.updatePreferences(appId, prefs)
                    .thenApply(aVoid -> {
                        WebUtils.doOK(aResponse, new JsonObject());
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/preferences", method= HttpMethod.GET, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> onFetchPreferences(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final ApplicationId appId = appId(aContext);
            return preferences(appId)
                    .thenApply(aBody -> {
                        WebUtils.doOK(aResponse, aBody);
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/configs", method= HttpMethod.GET, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> onFetchApplicationConfig(final RoutingContext aContext,
                                                            final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final ApplicationId appId = appId(aContext);

            JsonObject resultJson = new JsonObject();
            return contentModerationConfig(appId)
                    .thenCompose(config -> addConfiguration(resultJson, "contentModerator", config))
                    .thenCompose(aVoid -> configFCM(appId))
                    .thenCompose(config -> addConfiguration(resultJson, "push", config))
                    .thenApply(result -> {
                        WebUtils.doOK(aResponse, resultJson);
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/configs/contentModerators", method= HttpMethod.POST, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> onSaveContentModeratorConfig(final RoutingContext aContext,
                                                                final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final ApplicationId appId = appId(aContext);
            final String responseBody = responseBody(aContext);
            final AbstractProviderConfig config = deserializeProviderConfig(responseBody);

            return appService.saveContentModerationConfig(appId, config)
                    .thenApply(aVoid -> {
                        WebUtils.doOK(aResponse, new JsonObject());
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/configs/contentModerators", method= HttpMethod.GET, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> onFetchContentModeratorConfig(final RoutingContext aContext,
                                                                 final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final ApplicationId appId = appId(aContext);

            return contentModerationConfig(appId)
                    .thenApply(result -> {
                        WebUtils.doOK(aResponse, result);
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/configs/contentModerators", method= HttpMethod.DELETE, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> onDeleteContentModeratorConfig(final RoutingContext aContext,
                                                                  final HttpServerResponse aResponse) {
        return Utils.async(() -> {

            final ApplicationId appId = appId(aContext);
            return appService.deleteContentModerationConfig(appId)
                    .thenApply(V -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/configs/push", method= HttpMethod.POST, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> onSetPushConfig(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final ApplicationId appId = appId(aContext);
            final JsonObject body = aContext.getBodyAsJson();
            final PushPlatform platform = PushPlatform.fromValue(body.getString("platform"));
            final String secret = body.getString("secret");

            CompletableFuture<Void> saved;
            if (platform == PushPlatform.FCM) {
                FCMConfiguration config = new FCMConfiguration("", secret);
                saved = appService.setPushConfig(appId, config);
            }
            else {
                throw new IllegalArgumentException("invalid_platform");
            }

            return saved.thenApply(aVoid -> {
                WebUtils.doOK(aResponse, new JsonObject());
                return null;
            });
        });
    }

    @AsyncRestController(path="/configs/push/:platform", method= HttpMethod.GET, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> onFetchPushConfig(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final ApplicationId appId = appId(aContext);
            final PushPlatform platform = PushPlatform.fromValue(aContext.request().getParam("platform"));

            if (platform == PushPlatform.FCM) {
                return configFCM(appId)
                        .thenApply(aBody -> {
                            WebUtils.doOK(aResponse, aBody);

                            return null;
                        });
            }
            else {
                throw new IllegalArgumentException("invalid_platform");
            }
        });
    }

    @AsyncRestController(path="/configs/push/:platform", method= HttpMethod.DELETE, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> onClearPushConfig(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final ApplicationId appId = appId(aContext);
            final PushPlatform platform = PushPlatform.fromValue(aContext.request().getParam("platform"));

            if (platform == PushPlatform.FCM) {
                return appService.clearFCMConfig(appId)
                        .thenApply(aVoid -> {
                            WebUtils.doOK(aResponse, new JsonObject());
                            return null;
                        });
            }
            else {
                throw new IllegalArgumentException("invalid_platform");
            }
        });
    }

    private ApplicationId appId(final RoutingContext aContext) {
        try {
            return new ApplicationId(AuthenticatedUser.appId(aContext.user()));
        }
        catch (DomainErrorException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private String responseBody(final RoutingContext aContext) {
        return aContext.getBodyAsString();
    }

    private CompletableFuture<JsonObject> configFCM(final ApplicationId aAppId) {
        return appService.getFCMConfig(aAppId)
                .thenApply(this::serializeFCMConfig);
    }

    private JsonObject serializeProviderConfig(final AbstractProviderConfig aConfig) {
        if (Objects.isNull(aConfig)) {
            return new JsonObject();
        }
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(AbstractProviderConfig.class, new ProviderConfigSerde())
                .registerTypeAdapter(AzureProviderConfig.class, new ProviderConfigSerde())
                .registerTypeAdapter(CleanSpeakProviderConfig.class, new ProviderConfigSerde())
                .create();
        return new JsonObject(gson.toJson(aConfig));
    }

    private AbstractProviderConfig deserializeProviderConfig(final String aConfig) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(AbstractProviderConfig.class, new ProviderConfigSerde())
                .registerTypeAdapter(AzureProviderConfig.class, new ProviderConfigSerde())
                .registerTypeAdapter(CleanSpeakProviderConfig.class, new ProviderConfigSerde())
                .create();
        return gson.fromJson(aConfig, AbstractProviderConfig.class);
    }

    private JsonObject serializeFCMConfig(final FCMConfiguration aConfig) {
        JsonObject json = new JsonObject();
        if (Objects.nonNull(aConfig)) {
            json.put("platform", PushPlatform.FCM.value());
            json.put("secret", aConfig.secret());
        }
        return json;
    }

    private CompletableFuture<JsonObject> preferences(final ApplicationId aAppId) {
        return appService.getPreferences(aAppId)
                .thenApply(aPrefs -> {
                    JsonObject json = new JsonObject();
                    if (Objects.nonNull(aPrefs)) {
                        json.put("force_content_moderation", aPrefs.isForceContentModeration());
                    }
                    return json;
                });
    }

    private CompletableFuture<JsonObject> contentModerationConfig(final ApplicationId aAppId) {
        return appService.fetchContentModerationConfig(aAppId)
                .thenApply(this::serializeProviderConfig);
    };

    private CompletableFuture<Void> addConfiguration(final JsonObject aResult,
                                                     final String aKey,
                                                     final JsonObject aConfig) {
        aResult.put(aKey, aConfig);
        return CompletableFuture.completedFuture(null);
    }
}

