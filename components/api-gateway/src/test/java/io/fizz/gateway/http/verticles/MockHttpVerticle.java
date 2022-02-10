package io.fizz.gateway.http.verticles;

import io.fizz.chat.application.channel.AbstractTranslationClient;
import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.application.impl.HBaseApplicationRepository;
import io.fizz.chat.application.impl.MockApplicationService;
import io.fizz.chat.application.services.AbstractPushNotificationService;
import io.fizz.chat.group.infrastructure.bootstrap.ChatGroupComponent;
import io.fizz.chat.group.infrastructure.bootstrap.MockChatGroupComponent;
import io.fizz.chat.infrastructure.bootstrap.ChatComponent;
import io.fizz.chat.infrastructure.bootstrap.MockChatComponent;
import io.fizz.chat.infrastructure.services.MockPushNotificationService;
import io.fizz.chat.infrastructure.services.MockTranslationClient;
import io.fizz.chat.moderation.infrastructure.bootstrap.ChatModerationComponent;
import io.fizz.chat.moderation.infrastructure.bootstrap.MockChatModerationComponent;
import io.fizz.chat.presence.AbstractUserPresenceService;
import io.fizz.chat.presence.infrastructure.presence.MockUserPresenceService;
import io.fizz.chat.presence.persistence.AbstractPresenceRepository;
import io.fizz.chat.presence.persistence.InMemoryPresenceRepository;
import io.fizz.chat.presence.persistence.RedisPresenceRepository;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.infrastructure.messaging.MockTopicMessagePublisher;
import io.fizz.chatcommon.domain.RedisNamespace;
import io.fizz.chatcommon.infrastructure.VertxRedisClientProxy;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.fizz.command.bus.AbstractCommandBus;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.gateway.http.annotations.RLScope;
import io.fizz.gateway.http.auth.AuthenticatedUser;
import io.fizz.gateway.http.auth.SignatureAuthHandler;
import io.fizz.gateway.http.ratelimit.AbstractRateLimitRepository;
import io.fizz.gateway.http.ratelimit.MockRateLimitRepository;
import io.fizz.gateway.http.ratelimit.RateLimitConfig;
import io.fizz.gateway.http.services.handler.eventstream.AbstractEventStreamClientHandler;
import io.fizz.gateway.services.MockUSDConversionService;
import io.fizz.gateway.services.USDConversionService;
import io.fizz.chat.user.application.repository.AbstractUserRepository;
import io.fizz.chat.user.application.service.MockUserService;
import io.fizz.chat.user.application.service.UserService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.RedisOptions;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MockHttpVerticle extends HTTPVerticle {

    public static int CHANNEL_RATE_LIMIT_MAX_MOCK = 10;

    public MockHttpVerticle(int aPort,
                            int aPortInternal,
                            final AbstractEventStreamClientHandler aEventStreamHandler,
                            final String aRPCServiceHost,
                            final int aRPCServicePort) {
        super(aPort, aPortInternal, aEventStreamHandler, aRPCServiceHost, aRPCServicePort);
    }

    @Override
    protected ApplicationService buildApplicationService(final AbstractHBaseClient aClient) {
        return new MockApplicationService(new HBaseApplicationRepository(aClient));
    }

    @Override
    protected AbstractHBaseClient buildHBaseClient() {
        return new MockHBaseClient();
    }

    @Override
    protected Handler<RoutingContext> buildSignatureAuthHandler() {
        return new SignatureAuthHandler(getAuthProvider());
    }

    @Override
    protected AbstractTopicMessagePublisher buildTopicMessagePubSub() {
        return new MockTopicMessagePublisher();
    }

    @Override
    protected ChatComponent buildChatComponent() {
        return new MockChatComponent();
    }

    @Override
    protected ChatGroupComponent buildChatGroupComponent() {
        return new MockChatGroupComponent();
    }

    @Override
    protected AbstractUserPresenceService buildPresenceService() {
        return new MockUserPresenceService();
    }

    @Override
    protected AbstractPresenceRepository buildPresenceRepo(CompletableFuture<Void> buildRepo) {
        buildRepo.complete(null);
        return new InMemoryPresenceRepository();
    }

    @Override
    protected UserService buildUserService(final AbstractTopicMessagePublisher aTopicMessagePublisher,
                                           final AbstractUserRepository aUserRepository,
                                           final AbstractUserPresenceService aPresenceService) {
        return new MockUserService(aTopicMessagePublisher, aUserRepository, aPresenceService);
    }

    @Override
    protected AbstractTranslationClient createTranslationClient() {
        return new MockTranslationClient();
    }

    @Override
    protected ChatModerationComponent buildChatModerationComponent() {
        return new MockChatModerationComponent();
    }

    @Override
    protected AbstractPushNotificationService buildPushNotificationService()
    {
        return new MockPushNotificationService();
    }

    @Override
    protected USDConversionService currencyConversionService() {
        return new MockUSDConversionService();
    }

    @Override
    protected Future<Void> createCommandBus() {
        commandBus = AbstractCommandBus.create(vertx);

        return Future.succeededFuture();
    }

    protected AuthProvider getAuthProvider() {
        return new AuthProvider() {
            private boolean verify(String aPayload, String aSecret, String aSignature) {

                if (aSecret.isEmpty()) return false;
                if (aSignature.isEmpty()) return  false;

                try {
                    Mac hmac = Mac.getInstance("HmacSHA256");
                    SecretKeySpec secretKeySpec = new SecretKeySpec(
                            aSecret.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                    );
                    hmac.init(secretKeySpec);
                    byte[] mac = hmac.doFinal(aPayload.getBytes(StandardCharsets.UTF_8));
                    byte[] hash = Base64.getEncoder().encode(mac);
                    return new String(hash, StandardCharsets.UTF_8).equals(aSignature);
                }
                catch (NoSuchAlgorithmException | InvalidKeyException ex) {
                    return false;
                }
            }

            @Override
            public void authenticate(JsonObject jsonObject, Handler<AsyncResult<User>> handler) {
                String appId = jsonObject.getString("appId");
                String signature = jsonObject.getString("signature");
                String payload = jsonObject.getString("payload");

                final List<String> validApps = new ArrayList<String>() {{
                    add("appA");
                    add("appB");
                    add("appC");
                    add("appD");
                    add("b0128ae5-7e1b-427b-af93-f50cf773ac84d841c598-1fc7-44c4-984b-0c69d");
                }};

                if (!validApps.contains(appId) && !appId.endsWith("Test")) {
                    handler.handle(Future.failedFuture(new DomainErrorException("unknown_app")));
                }
                else
                if (!verify(payload, "secret", signature)) {
                    handler.handle(Future.failedFuture(new DomainErrorException("invalid_credentials")));
                }
                else {
                    handler.handle(Future.succeededFuture(new AuthenticatedUser(appId, "system")));
                }
            }
        };
    }

    @Override
    protected AbstractRateLimitRepository buildRateLimitRepository() {
        return new MockRateLimitRepository();
    }

    @Override
    protected Set<RateLimitConfig> buildRateLimitConfigs() {
        return new HashSet<RateLimitConfig>() {{ add(new RateLimitConfig(RLScope.CHANNEL, CHANNEL_RATE_LIMIT_MAX_MOCK)); }};
    }

    @Override
    protected Future<Void> createRedisClient() {
        return Future.succeededFuture();
    }
}
