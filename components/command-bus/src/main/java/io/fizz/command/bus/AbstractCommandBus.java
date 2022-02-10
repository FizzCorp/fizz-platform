package io.fizz.command.bus;

import io.fizz.command.bus.impl.CommandBus;
import io.fizz.command.bus.cluster.ClusteredGrpcChannelFactory;
import io.fizz.command.bus.impl.cluster.command.ClusteredCommandBusProxy;
import io.fizz.command.bus.impl.cluster.command.AbstractCommandBusMetrics;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;

public interface AbstractCommandBus {
    static AbstractCommandBus create(Vertx aVertx) {
        return new CommandBus(aVertx);
    }

    static AbstractCommandBus createClustered(Vertx aVertx, ClusteredGrpcChannelFactory aChannelFactory) {
        return new ClusteredCommandBusProxy(aVertx, aChannelFactory);
    }

    static AbstractCommandBus createClustered(Vertx aVertx,
                                              ClusteredGrpcChannelFactory aChannelFactory,
                                              AbstractCommandBusMetrics aMetrics) {
        return new ClusteredCommandBusProxy(aVertx, aChannelFactory, aMetrics);
    }

    void register(Object aHandler);
    <T> CompletableFuture<T> execute(AbstractCommand aCommand);
}
