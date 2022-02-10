package io.fizz.gateway.http.ratelimit;

import io.fizz.chatcommon.infrastructure.VertxRedisClientProxy;
import io.fizz.common.Utils;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RedisRateLimitRepository implements AbstractRateLimitRepository {

    private final int REDIS_KEY_EXPIRY_SECONDS = 60;
    private final VertxRedisClientProxy client;

    public RedisRateLimitRepository(final VertxRedisClientProxy aClient) {
        Utils.assertRequiredArgument(aClient, "invalid redis client specified");
        client = aClient;
    }

    @Override
    public CompletableFuture<Integer> incrementRate(String aKey) {
        Utils.assertRequiredArgument(aKey, "invalid_key");

        CompletableFuture<Integer> future = new CompletableFuture<>();

        List<Request> requests = new ArrayList<Request>() {{
           add(Request.cmd(Command.INCR).arg(aKey));
           add(Request.cmd(Command.EXPIRE).arg(aKey).arg(REDIS_KEY_EXPIRY_SECONDS));
        }};

        client.batch(requests)
                .handle(((response, error) -> {
                    if (Objects.isNull(error)) {
                        future.complete(response.get(0).toInteger());
                    }
                    else {
                        future.completeExceptionally(error);
                    }
                    return null;
                }));

        return future;
    }

    @Override
    public CompletableFuture<Integer> getRate(String aKey) {
        Utils.assertRequiredArgument(aKey, "invalid_key");

        CompletableFuture<Integer> future = new CompletableFuture<>();
        client.send(Request.cmd(Command.GET).arg(aKey))
                .handle(((response, error) -> {
                    if (Objects.isNull(error)) {
                        future.complete(Objects.nonNull(response) ? response.toInteger() : 0);
                    }
                    else {
                        future.completeExceptionally(error);
                    }
                    return null;
                }));

        return future;
    }
}
