package io.fizz.gateway.persistence;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

public interface AbstractEventStream {
    void put(final List<String> records, final Handler<AsyncResult<Integer>> handler);
}
