package io.fizz.gateway.http.ratelimit;

import io.fizz.chatcommon.domain.RedisNamespace;
import io.fizz.common.Utils;
import io.fizz.gateway.http.annotations.RLScope;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RateLimitService {
    private final Map<RLScope, RateLimitConfig> configs = new HashMap<>();
    private final AbstractRateLimitRepository repository;
    private final RedisNamespace redisNamespace;

    public RateLimitService(final Set<RateLimitConfig> aConfigs,
                            final AbstractRateLimitRepository aRepository,
                            final RedisNamespace aRedisNamespace) {
        Utils.assertRequiredArgument(aConfigs, "invalid rate limit configs specified");
        Utils.assertRequiredArgument(aRepository, "invalid rate limit repository specified");
        Utils.assertRequiredArgument(aRedisNamespace, "invalid redis namespace specified");

        for (RateLimitConfig config : aConfigs) {
            configs.put(config.scope(), config);
        }

        repository = aRepository;
        redisNamespace = aRedisNamespace;
    }

    public CompletableFuture<Boolean> handle(final String aAppId,
                                             final String aUserId,
                                             final RLScope aScope,
                                             final String aContext) {

        String rlContextKey = buildRateLimitContextKey(aAppId, aUserId, aScope, aContext);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        repository.incrementRate(rlContextKey)
                .thenAccept((rate) -> {
                    future.complete(rate <= configs.get(aScope).limit());
                });
        return future;
    }

    private String buildRateLimitContextKey(final String aAppId,
                                            final String aUserId,
                                            final RLScope aScope,
                                            final String aContext) {
        StringBuilder keyBuilder = new StringBuilder()
                .append(redisNamespace.value())
                .append(":").append(aScope)
                .append(":").append(aAppId);
        if (aScope == RLScope.USER) {
            keyBuilder.append(":").append(aUserId);
        }
        if (Objects.nonNull(aContext) && !aContext.isEmpty()) {
            keyBuilder.append(":").append(aContext);
        }
        long time = Utils.now()/1000;
        keyBuilder.append(":").append(time);
        return keyBuilder.toString();
    }
}
