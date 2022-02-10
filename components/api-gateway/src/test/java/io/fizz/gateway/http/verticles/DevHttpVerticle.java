package io.fizz.gateway.http.verticles;

import io.fizz.gateway.http.services.handler.eventstream.AbstractEventStreamClientHandler;
import io.vertx.core.Future;
import io.vertx.ext.auth.AuthProvider;

public class DevHttpVerticle extends MockHttpVerticle {
    public DevHttpVerticle(int aPort,
                           int aPortInternal,
                           AbstractEventStreamClientHandler aEventStream,
                           final String aRPCServiceHost,
                           final int aRPCServicePort) {
        super(aPort, aPortInternal, aEventStream, aRPCServiceHost, aRPCServicePort);
    }

    @Override
    protected AuthProvider getAuthProvider() {
        return (jsonObject, handler) -> handler.handle(Future.succeededFuture());
    }
}
