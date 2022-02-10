package io.fizz.gateway.http.services.proxy.eventstream;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.List;

@ProxyGen
public interface AbstractEventStreamClient {
    static AbstractEventStreamClient createProxy(final Vertx vertx, final String address) {
        return new AbstractEventStreamClientVertxEBProxy(vertx, address);
    }

    @Fluent
    AbstractEventStreamClient put(final List<String> records, final Handler<AsyncResult<Integer>> handler);
}
