package io.fizz.gateway.http;

import io.fizz.gateway.http.verticles.DevHttpVerticle;
import io.fizz.gateway.http.verticles.HTTPVerticle;
import io.vertx.core.Vertx;

public class DevApplication extends MockApplication {
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(DevApplication.class.getName());
    }

    @Override
    protected int servicesCount() {
        return 1;
    }

    @Override
    protected HTTPVerticle buildHTTPService() {
        return new DevHttpVerticle(API_PORT, API_PORT_INTERNAL, eventStreamHandler, rpcServiceHost, PROXY_PORT);
    }
}
