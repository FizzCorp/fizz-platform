package io.fizz.chat.emqx;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.fizz.chat.emqx.infrastructure.ConfigService;
import io.fizz.chat.emqx.infrastructure.EMQXConnectedEvent;
import io.fizz.chat.emqx.infrastructure.EMQXDisconnectedEvent;
import io.fizz.chat.emqx.infrastructure.hooks.ExHookProvider;
import io.fizz.chat.emqx.infrastructure.listener.EMQXConnectionEventListener;
import io.fizz.chat.emqx.infrastructure.messaging.EMQXEventPublisher;
import io.fizz.chat.emqx.infrastructure.service.AuthService;
import io.fizz.chat.presence.persistence.AbstractPresenceRepository;
import io.fizz.chat.presence.persistence.InMemoryPresenceRepository;
import io.fizz.chat.presence.persistence.RedisPresenceRepository;
import io.fizz.chatcommon.domain.RedisNamespace;
import io.fizz.chatcommon.domain.events.AbstractEventPublisher;
import io.fizz.chatcommon.domain.events.DomainEventBus;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.VertxRedisClientProxy;
import io.fizz.chatcommon.infrastructure.serde.JsonEventSerde;
import io.fizz.client.HBaseClientVerticle;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.HBaseClientProxy;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.fizz.common.LoggingService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.Session;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.redis.client.RedisOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Application extends AbstractVerticle {
    private static final LoggingService.Log log = LoggingService.getLogger(Application.class);

    private static final int API_PORT = ConfigService.config().getNumber("chat.emqx.http.port").intValue();
    static int RPC_SERVER_PORT = ConfigService.config().getNumber("rpc.port").intValue();
    private static final String REDIS_HOST = ConfigService.config().getString("redis.host");
    private static final int REDIS_PORT = ConfigService.config().getNumber("redis.port").intValue();

    private final List<AbstractVerticle> services = new ArrayList<>();

    private AuthService authService;
    private AbstractHBaseClient hbaseClient;
    private AbstractPresenceRepository presenceRepo;
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();
    private final Cache<String, Session> cachedSessions = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
    DomainEventBus eventBus;
    AbstractEventPublisher eventPublisher;


    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(Application.class.getName());
    }

    @Override
    public void start(Future<Void> future) {
        CompletableFuture<Void> bootstrapCompleted = new CompletableFuture<>();
        bootstrapCompleted.handle((V, error) -> {
            if (Objects.isNull(error)) {
                future.complete();
            }
            else {
                future.fail(error);
            }
            return null;
        });
        bootstrap(bootstrapCompleted);
    }

    @Override
    public void stop() {
        for (AbstractVerticle service: services) {
            vertx.undeploy(service.deploymentID());
        }
        vertx.close();
    }

    private void bootstrap(CompletableFuture<Void> future) {
        log.warn("===== Application Setup Started =====");
        startHBaseClient()
        .thenCompose(v -> initPresenceRepo())
        .thenCompose(v -> createDomainEventBus())
        .thenCompose(v -> createEventPublisher())
        .thenCompose(v -> registerRPCEvent())
        .thenCompose(v -> createAuthService())
        .thenCompose(v -> openRPCServer())
        .thenCompose(v -> openHttpServers())
        .whenComplete((v, error) -> {
            if (Objects.isNull(error)) {
                future.complete(null);
                log.warn("===== Application Setup Completed =====");
            }
            else {
                log.error("Application Setup Exception = "+error.getMessage());
                future.completeExceptionally(error);
            }
        });
    }

    protected CompletableFuture<Void> startHBaseClient() {
        log.info("starting container service...");
        CompletableFuture<Void> created = new CompletableFuture<>();

        boolean mock = ConfigService.config().getBoolean("chat.emqx.mock.session.stores");

        if (mock) {
            hbaseClient = new MockHBaseClient();
            created.complete(null);
        }
        else {
            HBaseClientVerticle verticle = new HBaseClientVerticle();
            vertx.deployVerticle(verticle, event -> {
                if (event.succeeded()) {
                    created.complete(null);
                }
                else {
                    created.completeExceptionally(event.cause());
                }
            });
            services.add(verticle);

            hbaseClient = new HBaseClientProxy(vertx);
        }
        return created;
    }

    protected CompletableFuture<Void> initPresenceRepo() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        boolean mock = ConfigService.config().getBoolean("chat.emqx.mock.presence.repo");
        if (mock) {
            presenceRepo = new InMemoryPresenceRepository();
            future.complete(null);
        }
        else {
            final RedisOptions redisOpts = new RedisOptions()
                    .setEndpoint(SocketAddress.inetSocketAddress(REDIS_PORT, REDIS_HOST))
                    .setMaxWaitingHandlers(65535);
            final VertxRedisClientProxy client = new VertxRedisClientProxy(vertx, redisOpts, future);

            presenceRepo = new RedisPresenceRepository(client, RedisNamespace.PRESENCE);
        }

        return future;
    }

    private CompletableFuture<Void> createDomainEventBus() {
        eventBus = new DomainEventBus();
        return CompletableFuture.completedFuture(null);
    }


    private CompletableFuture<Void> createEventPublisher() {
        eventPublisher = new EMQXEventPublisher(
                new JsonEventSerde(),
                new DomainEventType[]{
                        EMQXConnectedEvent.TYPE,
                        EMQXDisconnectedEvent.TYPE
                }
        );

        eventBus.register(eventPublisher);
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> registerRPCEvent() {
        eventBus.addListener(new EMQXConnectionEventListener(
                vertx,
                cachedSessions,
                presenceRepo,
                connections
        ));
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> createAuthService() {
        this.authService = new AuthService(
                vertx,
                hbaseClient,
                cachedSessions
        );
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> openRPCServer() {
        log.info("opening gRPC server...");
        CompletableFuture<Void> started = new CompletableFuture<>();
        vertx.deployVerticle(
                () -> new AbstractVerticle() {
                    @Override
                    public void start(Future<Void> aStarted) {
                        VertxServerBuilder
                                .forPort(vertx, RPC_SERVER_PORT)
                                .addService(new ExHookProvider(authService, eventBus))
                                .build()
                                .start(aStarted);
                    }
                },
                new DeploymentOptions().setInstances(1),
                aResult -> {
                    if (aResult.succeeded()) {
                        started.complete(null);
                    }
                    else {
                        started.completeExceptionally(aResult.cause());
                    }
                }
        );

        return started;
    }

    private CompletableFuture<Void> openHttpServers() {
        log.info("opening HTTP servers...");

        final CompletableFuture<Void> compositeFuture = new CompletableFuture<>();
        final List<CompletableFuture<Void>> futures = new ArrayList<>();

        log.info(String.format("\tOpening %d HTTP servers...", 1));

        final HTTPVerticle service = buildHTTPService();
        final CompletableFuture<Void> future = new CompletableFuture<>();
        futures.add(future);
        vertx.deployVerticle(service, res -> {
            if (res.succeeded()) {
                future.complete(null);
            }
            else {
                future.completeExceptionally(res.cause());
            }
        });
        services.add(service);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .handle((v, error) -> {
                    if (Objects.isNull(error)) {
                        compositeFuture.complete(null);
                    }
                    else {
                        compositeFuture.completeExceptionally(error);
                        log.fatal(error);
                    }
                    return null;
                });

        return compositeFuture;
    }

    protected HTTPVerticle buildHTTPService() {
        return new HTTPVerticle(
                API_PORT
        );
    }

}
