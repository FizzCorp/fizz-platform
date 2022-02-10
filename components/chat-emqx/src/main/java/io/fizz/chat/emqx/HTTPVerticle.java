package io.fizz.chat.emqx;

import io.fizz.chat.emqx.infrastructure.ConfigService;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.LoggingService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;


public class HTTPVerticle extends AbstractVerticle {
    private static final LoggingService.Log log = LoggingService.getLogger(HTTPVerticle.class);

    private static final boolean HTTP_DEBUG = ConfigService.config().getBoolean("chat.emqx.http.debug.logging");

    private final int port;
    private HttpServer server;

    public HTTPVerticle(final int port) {
        this.port = port;
    }

    @Override
    public void start() {

        // configure and mount routes
        Router router = Router.router(vertx);
        if (HTTP_DEBUG) {
            router.route().handler(LoggerHandler.create());
        }
        router.get("/status").handler(context -> WebUtils.doOK(context.response(), "EMQX Chat is running"));
        router.route().handler(BodyHandler.create());

        // begin serving
        server = vertx.createHttpServer();
        server.requestHandler(router).listen(this.port);
    }

    @Override
    public void stop() throws Exception {
        server.close();
    }

}
