package io.fizz.chat.presence.persistence;

import io.fizz.chat.presence.Presence;
import io.fizz.chatcommon.domain.RedisNamespace;
import io.fizz.chatcommon.infrastructure.VertxRedisClientProxy;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.Request;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RedisPresenceRepository implements AbstractPresenceRepository {
    private static final int KEY_TTL = 12*60*60;
    private final VertxRedisClientProxy client;
    private final RedisNamespace redisNamespace;

    public RedisPresenceRepository(final VertxRedisClientProxy aClient, final RedisNamespace aRedisNamespace) {
        Utils.assertRequiredArgument(aClient, "invalid redis client");
        Utils.assertRequiredArgument(aRedisNamespace, "invalid redis namespace");

        this.client = aClient;
        redisNamespace = aRedisNamespace;
    }

    @Override
    public CompletableFuture<Presence> get(final ApplicationId aAppId, final UserId aUserId) {
        final Request req = Request.cmd(Command.GET)
                .arg(key(aAppId, aUserId));

        return client.send(req)
                .thenApply(aResponse -> {
                    if (Objects.isNull(aResponse)) {
                        return null;
                    }
                    else {
                        return new Presence(Long.parseLong(aResponse.toString()));
                    }
                });
    }

    @Override
    public CompletableFuture<Void> put(ApplicationId aAppId, UserId aUserId, Presence aPresence) {
        final Request req = Request.cmd(Command.SET)
                .arg(key(aAppId, aUserId))
                .arg(aPresence.lastSeen())
                .arg("EX")
                .arg(KEY_TTL);

        return client.send(req)
                .thenApply(aResponse -> null);
    }

    private String key(final ApplicationId aAppId, final UserId aUserId) {
        return redisNamespace.value() + ":{" + aUserId.qualifiedValue(aAppId) + "}";
    }
}
