package io.fizz.gateway.http.ratelimit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MockRateLimitRepository implements AbstractRateLimitRepository {

    Map<String, Integer> repo = new HashMap<>();

    @Override
    public CompletableFuture<Integer> incrementRate(String aKey) {
        if (!repo.containsKey(aKey)) {
            repo.put(aKey, 0);
        }

        int value = repo.get(aKey);
        value++;
        repo.put(aKey, value);
        return CompletableFuture.completedFuture(value);
    }

    @Override
    public CompletableFuture<Integer> getRate(String aKey) {
        return CompletableFuture.completedFuture(repo.getOrDefault(aKey, 0));
    }
}
