package io.fizz.gateway.http;

import io.fizz.client.HBaseClientVerticle;
import io.fizz.command.bus.impl.cluster.command.ProxyService;
import io.fizz.common.ConfigService;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.gateway.http.services.handler.eventstream.AbstractEventStreamClientHandler;
import io.fizz.gateway.http.services.handler.eventstream.KafkaEventStreamServiceClientHandler;
import io.fizz.gateway.http.verticles.HTTPVerticle;
import io.fizz.gateway.http.verticles.KafkaEventStreamVerticle;
import io.vertx.core.*;
import io.vertx.grpc.VertxServerBuilder;

import java.util.ArrayList;
import java.util.List;

public class Application extends AbstractVerticle {
    static final LoggingService.Log logger = LoggingService.getLogger(Application.class);

    static int API_PORT = ConfigService.instance().getNumber("http.port").intValue();
    static int API_PORT_INTERNAL = ConfigService.instance().getNumber("http.internal.port").intValue();
    static int PROXY_PORT = ConfigService.instance().getNumber("proxy.port").intValue();

    private List<HTTPVerticle> services = new ArrayList<>();
    protected String rpcServiceHost;

    AbstractEventStreamClientHandler eventStreamHandler;

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(Application.class.getName());
    }

    @Override
    public void start(Future<Void> future) {
        bootstrap(future);
    }

    @Override
    public void stop() {
        for (HTTPVerticle service: services) {
            vertx.undeploy(service.deploymentID());
        }
        vertx.close();
    }

    private void bootstrap(Future<Void> completionFuture) {
        logger.warn("===== Application Setup Started =====");

        buildPipeline()
        .compose(v -> {
            logger.warn("===== Application Setup Completed =====");
            completionFuture.complete();
        }, completionFuture);
    }

    protected Future<Void> buildPipeline() {
        return startEventStreamService()
                .compose(v -> createEventStreamHandler())
                .compose(v -> startHBaseClient())
                .compose(v -> openRPCServer())
                .compose(v -> openHttpServers());
    }

    private Future<String> startEventStreamService() {
        logger.info("starting event stream service...");
        final List<Future> futures = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Future<String> created = Future.future();
            futures.add(created);
            vertx.deployVerticle(eventStreamService(), created);
        }

        final Future<String> compositeFuture = Future.future();
        CompositeFuture.all(futures).setHandler(ar -> {
            if (ar.succeeded()) {
                compositeFuture.complete();
            }
            else {
                compositeFuture.fail(ar.cause());
                logger.fatal(ar.cause());
            }
        });

        return compositeFuture;
    }

    protected Future<String> startHBaseClient() {
        logger.info("starting container service...");
        Future<String> created = Future.future();
        vertx.deployVerticle(HBaseClientVerticle.class.getName(), created);
        return created;
    }

    protected String eventStreamService() {
        return KafkaEventStreamVerticle.class.getName();
    }

    protected Future<Void> createEventStreamHandler() {
        logger.info("creating event stream handler");
        eventStreamHandler = new KafkaEventStreamServiceClientHandler(vertx);
        return Future.succeededFuture();
    }

    protected Future<Void> openRPCServer() {
        return host().compose(aHost -> {
            logger.debug("using host ip address: " + aHost);

            rpcServiceHost = aHost;

            Promise<Void> started = Promise.promise();

            vertx.deployVerticle(
                    () -> new AbstractVerticle() {
                        @Override
                        public void start(Future<Void> aStarted) {
                            VertxServerBuilder
                                    .forPort(vertx, PROXY_PORT)
                                    .addService(new ProxyService(vertx))
                                    .build()
                                    .start(aStarted);
                        }
                    },
                    new DeploymentOptions().setInstances(1),
                    aResult -> {
                        if (aResult.succeeded()) {
                            started.complete();
                        }
                        else {
                            started.fail(aResult.cause());
                        }
                    }
            );

            return started.future();
        });
    }

    protected Future<String> host() {
        return Future.succeededFuture(Utils.getHostname());
    }


    private Future<Void> openHttpServers() {
        logger.info("opening HTTP servers...");

        final Future<Void> compositeFuture = Future.future();
        final List<Future> futures = new ArrayList<>();
        final int servicesCount = servicesCount();

        logger.info(String.format("\tOpening %d HTTP servers...", servicesCount));

        for(int si = 0; si < servicesCount; si++) {
            final HTTPVerticle service = buildHTTPService();
            final Future<Void> future = Future.future();
            futures.add(future);
            vertx.deployVerticle(service, res -> {
                if (res.succeeded()) {
                    future.complete();
                }
                else {
                    future.fail(res.cause());
                }
            });
            services.add(service);
        }

        CompositeFuture.all(futures).setHandler(ar -> {
            if (ar.succeeded()) {
                compositeFuture.complete();
            }
            else {
                compositeFuture.fail(ar.cause());
                logger.fatal(ar.cause());
            }
        });

        return compositeFuture;
    }

    protected int servicesCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    protected HTTPVerticle buildHTTPService() {
        return new HTTPVerticle(
                API_PORT,
                API_PORT_INTERNAL,
                eventStreamHandler,
                rpcServiceHost,
                PROXY_PORT
        );
    }
}
