package io.fizz.chatcommon.infrastructure;

import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Response;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class VertxRedisClientProxy {
    private static final LoggingService.Log log = LoggingService.getLogger(VertxRedisClientProxy.class);

    private Redis client;
    private final CompletableFuture<Void> startFuture;

    public VertxRedisClientProxy(final Vertx aVertx, final RedisOptions aOpts, final CompletableFuture<Void> startFuture) {
        this.startFuture = startFuture;
        create(aVertx, aOpts, this::onConnected);
    }

    public CompletableFuture<Response> send(Request aRequest) {
        if (Objects.isNull(aRequest)) {
            return Utils.failedFuture(new IllegalArgumentException("invalid redis request"));
        }
        if (Objects.isNull(client)) {
            return Utils.failedFuture(new IOException("client not connected"));
        }

        CompletableFuture<Response> sent = new CompletableFuture<>();

        client.send(aRequest, aResult -> {
            if (aResult.succeeded()) {
                sent.complete(aResult.result());
            }
            else {
                sent.completeExceptionally(aResult.cause());
            }
        });

        return sent;
    }

    public CompletableFuture<List<Response>> batch(List<Request> aRequests) {
        if (Objects.isNull(aRequests)) {
            return Utils.failedFuture(new IllegalArgumentException("invalid redis requests"));
        }
        if (Objects.isNull(client)) {
            return Utils.failedFuture(new IOException("client not connected"));
        }

        CompletableFuture<List<Response>> sent = new CompletableFuture<>();

        client.batch(aRequests, aResult -> {
            if (aResult.succeeded()) {
                sent.complete(aResult.result());
            }
            else {
                sent.completeExceptionally(aResult.cause());
            }
        });

        return sent;
    }



    private void onConnected(AsyncResult<Redis> aResult) {
        client = aResult.result();
        if (!startFuture.isDone()) {
            startFuture.complete(null);
        }
    }

    private static void create(final Vertx aVertx,
                               final RedisOptions aRedisOpts,
                               final Handler<AsyncResult<Redis>> onConnected) {
        createRedisClient(aVertx, aRedisOpts, onConnected, 0);
    }

    private static void createRedisClient(final Vertx aVertx,
                                          final RedisOptions aRedisOpts,
                                          final Handler<AsyncResult<Redis>> onConnected,
                                          final int aRetry) {
        log.debug("trying to connect to redis");
        Redis.createClient(aVertx, aRedisOpts)
                .connect(aResult -> {
                    if (aResult.succeeded()) {
                        Redis client = aResult.result();
                        client.exceptionHandler(e -> {
                            log.error(e.getMessage());
                            createRedisClient(aVertx, aRedisOpts, onConnected, 0);
                        });
                        client.endHandler(aVoid -> {
                           log.debug("connection to redis ended");
                            createRedisClient(aVertx, aRedisOpts, onConnected, 0);
                        });

                        log.debug("connected to redis successfully");
                        onConnected.handle(aResult);
                    }
                    else {
                        backoff(
                            aVertx,
                            aRetry + 1,
                            aVoid -> createRedisClient(aVertx, aRedisOpts, onConnected, aRetry + 1)
                        );
                    }
                });
    }

    private static void backoff(final Vertx aVertx, final int aRetry, final Handler<Void> onRetry) {
        // backoff to a max of 5min retry
        long backoff = (long)(Math.pow(2, Math.min(aRetry, 15))*15);

        aVertx.setTimer(backoff, timer -> onRetry.handle(null));
    }
}
