package io.fizz.command.bus.impl.cluster.command;

import com.google.protobuf.ByteString;
import io.fizz.command.bus.impl.generated.ProxyServiceGrpc;
import io.fizz.command.bus.impl.generated.ReplyModel;
import io.fizz.command.bus.impl.generated.RequestModel;
import io.grpc.*;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxChannelBuilder;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class ProxyServiceTest {
    @Test
    @DisplayName("it should proxy requests correctly")
    public void proxyTest() throws Throwable {
        VertxTestContext testContext = new VertxTestContext();
        Checkpoint cp = testContext.checkpoint(2);
        Vertx vertx = Vertx.vertx();
        String address = UUID.randomUUID().toString();
        String request = UUID.randomUUID().toString();
        String response = UUID.randomUUID().toString();

        vertx.eventBus().<byte[]>consumer(address, aMessage -> {
            Assertions.assertEquals(request, new String(aMessage.body()));
            aMessage.reply(response.getBytes());
            cp.flag();
        });

        startServer(vertx, 3000).get();
        ProxyServiceGrpc.ProxyServiceVertxStub client = createClient(vertx, 3000);

        client.proxy(createRequest(address, request), aResult -> {
            Assertions.assertTrue(aResult.succeeded());

            String received = new String(aResult.result().getPayload().toByteArray());
            Assertions.assertEquals(response, received);
            cp.flag();
        });

        Assertions.assertTrue(testContext.awaitCompletion(5L, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should send failure result correctly")
    public void failedResponseTest() throws Throwable {
        VertxTestContext testContext = new VertxTestContext();
        Checkpoint cp = testContext.checkpoint(2);
        Vertx vertx = Vertx.vertx();
        String address = UUID.randomUUID().toString();

        vertx.eventBus().<byte[]>consumer(address, aMessage -> {
            aMessage.fail(500, "invalid_argument");
            cp.flag();
        });

        startServer(vertx, 3001).get();
        ProxyServiceGrpc.ProxyServiceVertxStub client = createClient(vertx,3001);

        client.proxy(createRequest(address, "test"), aResult -> {
            Assertions.assertTrue(aResult.succeeded());

            ReplyModel reply = aResult.result();
            Assertions.assertEquals(Status.Code.UNKNOWN.value(), reply.getStatus());
            Assertions.assertEquals("invalid_argument", reply.getCause());

            cp.flag();
        });

        Assertions.assertTrue(testContext.awaitCompletion(5L, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should proxy invalid responses correctly")
    public void invalidResponseTest() throws Throwable {
        VertxTestContext testContext = new VertxTestContext();
        Checkpoint cp = testContext.checkpoint(2);
        Vertx vertx = Vertx.vertx();
        String address = UUID.randomUUID().toString();

        vertx.eventBus().<byte[]>consumer(address, aMessage -> {
            Assertions.assertEquals("request", new String(aMessage.body()));
            aMessage.reply("invalid response");
            cp.flag();
        });

        startServer(vertx, 3002).get();
        ProxyServiceGrpc.ProxyServiceVertxStub client = createClient(vertx, 3002);

        client.proxy(createRequest(address, "request"), aResult -> {
            Assertions.assertTrue(aResult.succeeded());

            ReplyModel reply = aResult.result();
            Assertions.assertEquals(Status.UNKNOWN.getCode().value(), reply.getStatus());
            cp.flag();
        });

        Assertions.assertTrue(testContext.awaitCompletion(5L, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should proxy invalid request correctly")
    public void invalidAddressTest() throws Throwable {
        VertxTestContext testContext = new VertxTestContext();
        Checkpoint cp = testContext.checkpoint();
        Vertx vertx = Vertx.vertx();
        String address = UUID.randomUUID().toString();

        startServer(vertx, 3003).get();
        ProxyServiceGrpc.ProxyServiceVertxStub client = createClient(vertx, 3003);

        RequestModel request = RequestModel.newBuilder()
                .setTo(address)
                .build();

        client.proxy(request, aResult -> {
            Assertions.assertTrue(aResult.succeeded());

            ReplyModel reply = aResult.result();
            Assertions.assertEquals(Status.UNKNOWN.getCode().value(), reply.getStatus());
            cp.flag();
        });

        Assertions.assertTrue(testContext.awaitCompletion(5L, TimeUnit.SECONDS));
    }

    RequestModel createRequest(String aAddress, String aPayload) {
        return RequestModel.newBuilder()
                .setTo(aAddress)
                .setPayload(ByteString.copyFromUtf8(aPayload))
                .build();
    }

    CompletableFuture<Void> startServer(Vertx aVertx, int aPort) {
        CompletableFuture<Void> started = new CompletableFuture<>();

        VertxServerBuilder.forPort(aVertx, aPort)
                .addService(new ProxyService(aVertx))
                .build()
                .start(aResult -> {
                    if (aResult.succeeded()) {
                        started.complete(null);
                    }
                    else {
                        started.completeExceptionally(aResult.cause());
                    }
                });

        return started;
    }

    ProxyServiceGrpc.ProxyServiceVertxStub createClient(Vertx aVertx, int aPort) {
        ManagedChannel channel = VertxChannelBuilder.forAddress(aVertx, "localhost", aPort)
                .usePlaintext(true)
                .build();

        return ProxyServiceGrpc.newVertxStub(channel);
    }
}
