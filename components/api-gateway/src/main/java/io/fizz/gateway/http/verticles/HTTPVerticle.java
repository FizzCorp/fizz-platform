package io.fizz.gateway.http.verticles;

import io.fizz.chat.application.channel.AbstractTranslationClient;
import io.fizz.chat.application.channel.ChannelApplicationService;
import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.application.impl.HBaseApplicationRepository;
import io.fizz.chat.application.services.AbstractPushNotificationService;
import io.fizz.chat.domain.Permissions;
import io.fizz.chat.group.domain.group.GroupPermissions;
import io.fizz.chat.group.infrastructure.bootstrap.ChatGroupComponent;
import io.fizz.chat.infrastructure.bootstrap.ChatComponent;
import io.fizz.chat.infrastructure.services.FCMPushNotificationService;
import io.fizz.chat.infrastructure.translation.BingTranslationClient;
import io.fizz.chat.moderation.infrastructure.bootstrap.ChatModerationComponent;
import io.fizz.chat.presence.AbstractUserPresenceService;
import io.fizz.chat.presence.infrastructure.presence.UserPresenceService;
import io.fizz.chat.presence.persistence.AbstractPresenceRepository;
import io.fizz.chat.presence.persistence.RedisPresenceRepository;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.AbstractSubscriberRepository;
import io.fizz.chat.pubsub.infrastructure.messaging.EMQXTopicMessagePublisher;
import io.fizz.chat.pubsub.infrastructure.serde.JsonTopicMessageSerde;
import io.fizz.chat.user.application.repository.AbstractUserRepository;
import io.fizz.chat.user.application.service.UserService;
import io.fizz.chat.user.infrastructure.persistence.HBaseUserRepository;
import io.fizz.chataccess.application.AuthorizationService;
import io.fizz.chataccess.domain.AbstractAuthorizationService;
import io.fizz.chataccess.domain.role.Role;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chataccess.infrastructure.bootstrap.ChatAccessComponent;
import io.fizz.chataccess.infrastructure.persistence.local.InMemoryRoleRepository;
import io.fizz.chatcommon.domain.RedisNamespace;
import io.fizz.chatcommon.infrastructure.VertxRedisClientProxy;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.HBaseClientProxy;
import io.fizz.command.bus.AbstractCommandBus;
import io.fizz.command.bus.cluster.AbstractCluster;
import io.fizz.command.bus.cluster.ClusteredGrpcChannelFactory;
import io.fizz.command.bus.impl.cluster.kafka.KafkaCluster;
import io.fizz.common.ConfigService;
import io.fizz.common.LoggingService;
import io.fizz.gateway.event.listener.MessageTranslatedEventListener;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.gateway.http.annotations.RLScope;
import io.fizz.gateway.http.auth.SessionAuthHandler;
import io.fizz.gateway.http.auth.SignatureAuthHandler;
import io.fizz.gateway.http.auth.SignatureAuthProvider;
import io.fizz.gateway.http.controllers.*;
import io.fizz.gateway.http.controllers.chat.ChatController;
import io.fizz.gateway.http.controllers.exploration.QueriesController;
import io.fizz.gateway.http.controllers.gdpr.GDPRController;
import io.fizz.gateway.http.controllers.group.GroupController;
import io.fizz.gateway.http.controllers.group.UserGroupController;
import io.fizz.gateway.http.controllers.group.UserGroupControllerInternal;
import io.fizz.gateway.http.controllers.ingestion.DigestEventsController;
import io.fizz.gateway.http.controllers.ingestion.SessionEventsController;
import io.fizz.gateway.http.controllers.user.UserController;
import io.fizz.gateway.http.ratelimit.AbstractRateLimitRepository;
import io.fizz.gateway.http.ratelimit.RateLimitConfig;
import io.fizz.gateway.http.ratelimit.RateLimitService;
import io.fizz.gateway.http.ratelimit.RedisRateLimitRepository;
import io.fizz.gateway.http.services.handler.eventstream.AbstractEventStreamClientHandler;
import io.fizz.gateway.services.CurrencyLayerUSDConversionService;
import io.fizz.gateway.services.USDConversionService;
import io.fizz.gateway.services.keycloak.KCService;
import io.fizz.gdpr.infrastructure.bootstrap.GDPRComponent;
import io.fizz.logger.application.service.LoggerService;
import io.fizz.session.HBaseSessionStore;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.RedisOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class HTTPVerticle extends AbstractVerticle {
    private static final LoggingService.Log logger = LoggingService.getLogger(HTTPVerticle.class);

    private static final RoleName ROLE_NAME_ADMIN = new RoleName("admin");
    private static final RoleName ROLE_NAME_BANNED = new RoleName("banned");
    private static final RoleName ROLE_NAME_MUTED = new RoleName("muted");
    private static final RoleName ROLE_NAME_USER = new RoleName("user");

    private static final String API_VER = ConfigService.instance().getString("gateway.api.ver");
    private static final String KAFKA_SERVERS = ConfigService.instance().getString("gateway.kafka.servers");
    private static final int SESSION_TIMEOUT_SEC = ConfigService.instance().getNumber("chat.hbase.sessions.ttl").intValue();
    private static final long SESSION_TIMEOUT_MS = SESSION_TIMEOUT_SEC*1000L;
    private static final String ALLOWED_CORS_URL = ConfigService.instance().getString("http.allowed.cors.url");
    private static final String KAFKA_CLUSTER_TOPIC = ConfigService.instance().getString("gateway.kafka.cluster.topic");
    private static final String KAFKA_CLUSTER_GROUP = ConfigService.instance().getString("gateway.kafka.cluster.group");
    private static final String REDIS_HOST = ConfigService.instance().getString("redis.host");
    private static final int REDIS_PORT = ConfigService.instance().getNumber("redis.port").intValue();

    protected AbstractCommandBus commandBus;

    private final int port;
    private final int portInternal;
    private final String rpcServiceHost;
    private final int rpcServicePort;
    private final AbstractEventStreamClientHandler eventStreamHandler;
    private VertxRedisClientProxy redisClient;
    private ChatComponent chatComponent;
    private UserService userService;
    private ApplicationService appService;
    private LoggerService loggerService;
    private AbstractTopicMessagePublisher messagePublisher;
    private AbstractPushNotificationService pushNotificationService;
    private ChatAccessComponent chatAccessComponent;
    private ChatModerationComponent chatModComponent;
    private ChatGroupComponent chatGroupComponent;
    private GDPRComponent gdprComponent;
    private AbstractUserRepository userRepository;
    private AbstractUserPresenceService presenceService;
    private AbstractHBaseClient hbaseClient;
    private AbstractPresenceRepository presenceRepo;

    public HTTPVerticle(int aPort,
                        int aPortInternal,
                        final AbstractEventStreamClientHandler aEventStreamHandler,
                        final String aRPCServiceHost,
                        final int aRPCServicePort) {
        if (Objects.isNull(aEventStreamHandler)) {
            throw new IllegalArgumentException("invalid event stream handler specified.");
        }

        port = aPort;
        portInternal = aPortInternal;
        rpcServiceHost = aRPCServiceHost;
        rpcServicePort = aRPCServicePort;
        eventStreamHandler = aEventStreamHandler;
    }

    @Override
    public void start(Future<Void> startFuture) {
        buildPipeline()
                .compose(v -> mountRoutes())
                .compose(this::startServer)
        .compose(v -> mountRoutesInternal())
        .compose(this::startServerInternal)
                .setHandler(startFuture);
    }

    private Future<Void> startServer(Router aRouter) {
        final Future<Void> future = Future.future();
        final HttpServer server = vertx.createHttpServer();
        server.requestHandler(aRouter).listen(port, res -> {
            logger.info("Started HTTP server on port: " + port);
            if (res.succeeded()) {
                future.complete();
            }
            else {
                future.fail(res.cause());
            }
        });

        return future;
    }

    private Future<Void> startServerInternal(Router aRouter) {
        final Future<Void> future = Future.future();
        final HttpServer server = vertx.createHttpServer();
        server.requestHandler(aRouter).listen(portInternal, res -> {
            logger.info("Started HTTP server on port: " + portInternal);
            if (res.succeeded()) {
                future.complete();
            }
            else {
                future.fail(res.cause());
            }
        });

        return future;
    }



    protected Future<Void> buildPipeline() {
        return createHBaseClient()
                .compose(v -> createCommandBus())
                .compose(v -> createRedisClient())
                .compose(v -> createAuthorizationService())
                .compose(v -> createApplicationService())
                .compose(v -> createChatModerationService())
                .compose(v -> createGDPRService())
                .compose(v -> createUserRepository())
                .compose(v -> createPresenceRepo())
                .compose(v -> createPresenceService())
                .compose(v -> createTopicMessagePubSub())
                .compose(v -> createUserComponent())
                .compose(v -> createPushNotificationService())
                .compose(v -> createChatComponent())
                .compose(v -> createChatService())
                .compose(v -> createChatGroupService())
                .compose(v -> createLoggerService())
                .compose(v -> registerChatEvent());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private Future<Void> createHBaseClient() {
        logger.info("creating hbase client...");

        hbaseClient = buildHBaseClient();
        return Future.succeededFuture();
    }

    protected AbstractHBaseClient buildHBaseClient() {
        return new HBaseClientProxy(vertx);
    }

    private Future<Void> createTopicMessagePubSub() {
        logger.info("creating chat publishers...");
        messagePublisher = buildTopicMessagePubSub();
        return Future.succeededFuture();
    }

    protected AbstractTopicMessagePublisher buildTopicMessagePubSub() {
        return new EMQXTopicMessagePublisher(vertx, new JsonTopicMessageSerde());
    }

    private Future<Void> createChatComponent() {
        chatComponent = buildChatComponent();
        return Future.succeededFuture();
    }

    protected ChatComponent buildChatComponent() {
        return new ChatComponent();
    }

    private Future<Void> createPushNotificationService() {
        pushNotificationService = buildPushNotificationService();
        return Future.succeededFuture();
    }

    protected AbstractPushNotificationService buildPushNotificationService() {
        return new FCMPushNotificationService(vertx, appService, userService);
    }

    private Future<Void> createUserComponent() {
        final Future<Void> created = Future.future();
        userService = buildUserService(messagePublisher, userRepository, presenceService);
        userService.open().handle((v, aError) -> {
            if (Objects.isNull(aError)) {
                created.complete();
            } else {
                created.fail(aError.getCause());
            }
            return CompletableFuture.completedFuture(null);
        });
        return created;
    }

    private Future<Void> createApplicationService() {
        appService = buildApplicationService(hbaseClient);

        return Future.succeededFuture();
    }

    protected ApplicationService buildApplicationService(final AbstractHBaseClient aClient) {
        return new ApplicationService(new HBaseApplicationRepository(aClient));
    }


    protected UserService buildUserService(final AbstractTopicMessagePublisher aTopicMessagePublisher,
                                           final AbstractUserRepository aUserRepository,
                                           final AbstractUserPresenceService aPresenceService) {
        return new UserService(aTopicMessagePublisher, aUserRepository, aPresenceService);
    }

    protected ChatAccessComponent buildChatAccessComponent() {
        return new ChatAccessComponent();
    }

    protected ChatModerationComponent buildChatModerationComponent() {
        return new ChatModerationComponent();
    }

    protected GDPRComponent buildGDPRComponent() {
        return new GDPRComponent();
    }

    protected ChatGroupComponent buildChatGroupComponent() {
        return new ChatGroupComponent();
    }

    private Future<Void> createAuthorizationService() {
        logger.info("creating authorization service...");

        final String[] permissionsAll = new String[] {
                Permissions.READ_MESSAGES.value(),
                Permissions.READ_CHANNEL_TOPICS.value(),
                Permissions.PUBLISH_MESSAGES.value(),
                Permissions.EDIT_OWN_MESSAGE.value(),
                Permissions.DELETE_OWN_MESSAGE.value(),
                Permissions.DELETE_ANY_MESSAGE.value(),
                Permissions.MANAGE_MUTES.value(),
                Permissions.MANAGE_BANS.value(),
                GroupPermissions.CREATE.value(),
                GroupPermissions.EDIT_GROUP_PROFILE.value(),
                GroupPermissions.EDIT_MEMBER_ROLES.value(),
                GroupPermissions.INVITE_MEMBERS.value(),
                GroupPermissions.ADD_MEMBERS.value(),
                GroupPermissions.REMOVE_ANY_MEMBER.value(),
                GroupPermissions.REMOVE_SELF.value(),
                GroupPermissions.ACCEPT_MEMBERSHIP.value()
        };
        final String[] permissionsUsers = new String[] {
                Permissions.READ_MESSAGES.value(),
                Permissions.PUBLISH_MESSAGES.value(),
                Permissions.EDIT_OWN_MESSAGE.value(),
                Permissions.DELETE_OWN_MESSAGE.value(),
                GroupPermissions.ACCEPT_MEMBERSHIP.value()
        };
        final String[] permissionsMuted = new String[] {
                Permissions.READ_MESSAGES.value(),
                GroupPermissions.ACCEPT_MEMBERSHIP.value(),
                GroupPermissions.REMOVE_SELF.value()
        };

        final Role admin = new Role(ROLE_NAME_ADMIN, 1000, permissionsAll);
        final Role banned = new Role(ROLE_NAME_BANNED, 4000);
        final Role muted = new Role(ROLE_NAME_MUTED, 5000, permissionsMuted);
        final Role user = new Role(ROLE_NAME_USER, 7000, permissionsUsers);

        final Future<Void> created = Future.future();
        final ChatAccessComponent chatAccess = buildChatAccessComponent();

        chatAccess.open(vertx, hbaseClient, user, user)
                .whenComplete((v, aError) -> {
                    if (Objects.isNull(aError)) {
                        final InMemoryRoleRepository roleRepo = chatAccess.roleRepo();

                        roleRepo.save(admin);
                        roleRepo.save(banned);
                        roleRepo.save(muted);
                        roleRepo.save(user);

                        chatAccessComponent = chatAccess;

                        created.complete();
                    }
                    else {
                        created.fail(aError);
                    }
                });

        return created;
    }

    private Future<Void> createChatService() {
        final AbstractAuthorizationService authService = chatAccessComponent.authorizationService();
        final Promise<Void> created = Promise.promise();

        final AbstractTranslationClient translationClient = createTranslationClient();
        final CompletableFuture<Void> opened =
                                        chatComponent.open(
                                            vertx,
                                            redisClient,
                                            RedisNamespace.CHAT,
                                            hbaseClient,
                                            messagePublisher,
                                            appService,
                                            chatModComponent,
                                            translationClient,
                                            authService,
                                            presenceService,
                                            pushNotificationService,
                                            ROLE_NAME_MUTED,
                                            ROLE_NAME_BANNED,
                                            SESSION_TIMEOUT_SEC,
                                            KAFKA_SERVERS
                                        );

        opened.handle((v, aError) -> {
            if (Objects.isNull(aError)) {
                created.complete();
            } else {
                created.fail(aError.getCause());
            }
            return CompletableFuture.completedFuture(null);
        });

        return created.future();
    }

    private Future<Void> createChatGroupService() {
        final Promise<Void> created = Promise.promise();

        chatGroupComponent = buildChatGroupComponent();

        final CompletableFuture<Void> opened =
                chatGroupComponent.open(
                        vertx,
                        chatAccessComponent.authorizationService(),
                        chatComponent.channelService(),
                        chatComponent.userNotificationService(),
                        ROLE_NAME_MUTED,
                        hbaseClient,
                        KAFKA_SERVERS
                );

        opened.whenComplete((aVoid, aError) -> {
            if (Objects.nonNull(aError)) {
                created.fail(aError);
            }
            else {
                commandBus.register(chatGroupComponent.groupService());
                created.complete();
            }
        });

        return created.future();
    }

    protected AbstractTranslationClient createTranslationClient() {
        return new BingTranslationClient(vertx);
    }

    private Future<Void> createLoggerService() {
        loggerService = buildLoggerService(chatComponent.channelService());
        return Future.succeededFuture();
    }

    protected LoggerService buildLoggerService(final ChannelApplicationService channelApplicationService) {
        return new LoggerService(channelApplicationService);
    }

    private Future<Object> createUserRepository() {
        userRepository = new HBaseUserRepository(hbaseClient);
        return Future.succeededFuture();
    }

    private Future<Object> createPresenceService() {
        presenceService = buildPresenceService();
        return Future.succeededFuture();
    }

    protected AbstractUserPresenceService buildPresenceService() {
        return new UserPresenceService(vertx, presenceRepo);
    }

    private Future<Void> createPresenceRepo() {
        final Promise<Void> created = Promise.promise();
        final CompletableFuture<Void> buildRepo = new CompletableFuture<>();
        buildRepo.thenAccept(v -> created.complete());
        presenceRepo = buildPresenceRepo(buildRepo);
        return created.future();
    }

    protected AbstractPresenceRepository buildPresenceRepo(CompletableFuture<Void> buildRepo) {
        final RedisOptions redisOpts = new RedisOptions()
                 .setEndpoint(SocketAddress.inetSocketAddress(REDIS_PORT, REDIS_HOST))
                 .setMaxWaitingHandlers(65535);
         final VertxRedisClientProxy client = new VertxRedisClientProxy(vertx, redisOpts, buildRepo);

        return new RedisPresenceRepository(client, RedisNamespace.PRESENCE);
    }


    private Future<Void> createChatModerationService() {
        final Promise<Void> created = Promise.promise();

        chatModComponent = buildChatModerationComponent();

        chatModComponent.open(vertx, appService)
                .whenComplete((v, aError) -> {
                    if (Objects.isNull(aError)) {
                        created.complete();
                    }
                    else {
                        created.fail(aError.getCause());
                    }
                });

        return created.future();
    }

    private Future<Void> createGDPRService() {
        final Promise<Void> created = Promise.promise();

        gdprComponent = buildGDPRComponent();

        gdprComponent.open()
                .whenComplete((v, aError) -> {
                    if (Objects.isNull(aError)) {
                        created.complete();
                    }
                    else {
                        created.fail(aError.getCause());
                    }
                });

        return created.future();
    }

    private Future<Void> registerChatEvent() {
        chatComponent.eventBus().addListener(new MessageTranslatedEventListener(eventStreamHandler));
        return Future.succeededFuture();
    }

    protected Future<Void> createCommandBus() {
        final AbstractCluster cluster = new KafkaCluster(
                vertx,
                KAFKA_SERVERS,
                KAFKA_CLUSTER_TOPIC,
                KAFKA_CLUSTER_GROUP,
                rpcServiceHost,
                rpcServicePort
        );
        final ClusteredGrpcChannelFactory channelFactory = new ClusteredGrpcChannelFactory(vertx, cluster);

        commandBus = AbstractCommandBus.createClustered(vertx, channelFactory);

        Promise<Void> created = Promise.promise();

        cluster.join().whenComplete((aVoid, aError) -> {
            if (Objects.nonNull(aError)) {
                created.fail(aError);
            }
            else {
                created.complete();
            }
        });

        return created.future();
    }

    protected Future<Void> createRedisClient() {
        Promise<Void> created = Promise.promise();
        CompletableFuture<Void> clientCreated = new CompletableFuture<>();
        clientCreated.whenComplete((v, error) -> {
            if (Objects.nonNull(error)) {
                created.fail(error);
            }
            else {
                created.complete();
            }
        });
        final RedisOptions redisOpts = new RedisOptions()
                .setEndpoint(SocketAddress.inetSocketAddress(REDIS_PORT, REDIS_HOST))
                .setMaxWaitingHandlers(65535);
        redisClient = new VertxRedisClientProxy(vertx, redisOpts, clientCreated);

        return created.future();
    }

    private Future<Router> mountRoutes() {
        logger.info("\tmounting routes...");

        try {
            final String apiRootPath = "/" + API_VER;
            final HBaseSessionStore store = new HBaseSessionStore(vertx, hbaseClient);
            final USDConversionService conversionService = currencyConversionService();
            final Handler<RoutingContext> digestAuthHandler = buildSignatureAuthHandler();
            final Handler<RoutingContext> sessionAuthHandler = new SessionAuthHandler(store);
            final Set<RateLimitConfig> rateLimitConfigs = buildRateLimitConfigs();
            final RateLimitService rateLimitService = new RateLimitService(rateLimitConfigs, buildRateLimitRepository(),
                    RedisNamespace.RATE_LIMIT);
            final boolean httpLogging = ConfigService.instance().getBoolean("http.debug.logging");
            final RouterBuilder builder = new RouterBuilder(vertx, apiRootPath);

            Router router = builder
                    .enableHttpLogging(httpLogging)
                    .enableCORS(ALLOWED_CORS_URL)
                    .authHandler(AuthScheme.DIGEST, digestAuthHandler)
                    .authHandler(AuthScheme.SESSION_TOKEN, sessionAuthHandler)
                    .rateLimitService(rateLimitService)
                    .controller(buildAuthController(store))
                    .controller(new DigestEventsController(vertx, eventStreamHandler, conversionService, loggerService))
                    .controller(new QueriesController(vertx))
                    .controller(new LogController(vertx, loggerService))
                    .controller(new ApplicationConfigController(vertx, chatModComponent.contentModerationService(), appService))
                    .controller(new SessionEventsController(vertx, eventStreamHandler, conversionService, loggerService))
                    .controller(new ChatController(vertx, chatComponent.channelService()))
                    .controller(new GroupController(vertx, chatGroupComponent.groupService(), commandBus))
                    .controller(new UserGroupController(vertx, chatGroupComponent.userGroupQueryService(), commandBus, chatAccessComponent.authorizationService(), ROLE_NAME_ADMIN))
                    .controller(new ChatModerationController(vertx, chatModComponent.chatModerationService()))
                    .controller(new ContentModerationController(vertx, chatModComponent.contentModerationService()))
                    .controller(new TranslateController(vertx, chatComponent.translationService()))
                    .controller(new UserNotificationsController(vertx, chatComponent.userNotificationService()))
                    .controller(new UserController(vertx, userService))
                    .controller(new GDPRController(vertx, gdprComponent.GDPRService()))
                    .build();

            router.get(apiRootPath + "/status").handler(context -> WebUtils.doOK(context.response(), "Fizz is running"));

            return Future.succeededFuture(router);
        }
        catch (Exception ex) {
            return Future.failedFuture(ex);
        }
    }

    private Future<Router> mountRoutesInternal() {
        logger.info("mounting internal routes...");

        try {
            final String apiRootPath = "/" + API_VER;
            final boolean httpLogging = ConfigService.instance().getBoolean("http.debug.logging");
            final RouterBuilder builder = new RouterBuilder(vertx, apiRootPath);

            Router router = builder
                    .enableHttpLogging(httpLogging)
                    .controller(new UserGroupControllerInternal(vertx, chatGroupComponent.userGroupQueryService(),
                            commandBus, chatAccessComponent.authorizationService(), ROLE_NAME_ADMIN))
                    .build();

            return Future.succeededFuture(router);
        }
        catch (Exception ex) {
            return Future.failedFuture(ex);
        }
    }

    protected USDConversionService currencyConversionService() {
        return new CurrencyLayerUSDConversionService();
    }

    protected Handler<RoutingContext> buildSignatureAuthHandler()
    {
        try {
            ConfigService config = ConfigService.instance();
            URL kcURL = new URL(config.getString("iam.url"));
            SignatureAuthHandler authHandler;
            authHandler = new SignatureAuthHandler(new SignatureAuthProvider(new KCService(
                    kcURL,
                    config.getString("iam.realm"),
                    config.getString("iam.ingestion.id"),
                    config.getString("iam.ingestion.clientId"),
                    config.getString("iam.ingestion.secret")
            )));
            authHandler.addAuthority("publish:events");

            return authHandler;
        }
        catch (MalformedURLException ex)
        {
            logger.fatal(ex.getMessage());
            return null;
        }
    }

    protected Set<RateLimitConfig> buildRateLimitConfigs() {
        Set<RateLimitConfig> configs = new HashSet<>();
        ConfigService configService = ConfigService.instance();

        int scopeChannelMaxLimit = configService.getNumber("rate.limit.scope.channel.max").intValue();
        configs.add(new RateLimitConfig(RLScope.CHANNEL, scopeChannelMaxLimit));

        return configs;
    }

    private AuthController buildAuthController(final HBaseSessionStore aSessionStore) {
        final AuthorizationService authService = chatAccessComponent.authorizationService();
        final AbstractSubscriberRepository subRepo = chatComponent.subscriberRepo();

        return new AuthController(vertx, subRepo, authService, ROLE_NAME_ADMIN, ROLE_NAME_MUTED, aSessionStore, SESSION_TIMEOUT_MS);
    }

    protected AbstractRateLimitRepository buildRateLimitRepository() {
        return new RedisRateLimitRepository(redisClient);
    }
}
