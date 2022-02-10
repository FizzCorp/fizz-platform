package io.fizz.client.hbase.client;

import com.google.protobuf.InvalidProtocolBufferException;
import io.fizz.client.ConfigService;
import io.fizz.client.hbase.HBaseClientModels;
import io.fizz.common.Utils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class HBaseClientProxy implements AbstractHBaseClient {
    static public final String HEADER_OP = "op";
    static public final String OP_PUT = "put";
    static public final String OP_GET = "get";
    static public final String OP_SCAN = "scan";
    static public final String OP_INCREMENT = "increment";
    static public final String OP_DELETE = "delete";

    private static final String SERVICE_ADDRESS = ConfigService.config().getString("hbase.client.services.address");

    private final Vertx vertx;

    public HBaseClientProxy(final Vertx aVertx) {
        Utils.assertRequiredArgument(aVertx, "invalid vertx instance specified.");

        vertx = aVertx;
    }

    @Override
    public CompletableFuture<Boolean> put(HBaseClientModels.Put aPut) {
        Utils.assertRequiredArgument(aPut, "invalid_put");

        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        vertx.eventBus().send(
                SERVICE_ADDRESS,
                Buffer.buffer(aPut.toByteArray()),
                new DeliveryOptions().addHeader(HEADER_OP, OP_PUT),
                (Handler<AsyncResult<Message<Buffer>>>) ar -> {
                    if (ar.succeeded()) {
                        boolean status = Bytes.toBoolean(ar.result().body().getBytes());
                        future.complete(status);
                    }
                    else {
                        future.completeExceptionally(new CompletionException(ar.cause()));
                    }
                }
        );
        return future;
    }

    @Override
    public CompletableFuture<Void> delete(HBaseClientModels.Delete aDelete) {
        Utils.assertRequiredArgument(aDelete, "invalid_delete");

        final CompletableFuture<Void> future = new CompletableFuture<>();
        vertx.eventBus().send(
                SERVICE_ADDRESS,
                Buffer.buffer(aDelete.toByteArray()),
                new DeliveryOptions().addHeader(HEADER_OP, OP_DELETE),
                (Handler<AsyncResult<Message<Buffer>>>)ar -> {
                    if (ar.succeeded()) {
                        future.complete(null);
                    }
                    else {
                        future.completeExceptionally(new CompletionException(ar.cause()));
                    }
                }
        );
        return future;
    }

    @Override
    public CompletableFuture<HBaseClientModels.Result> get(HBaseClientModels.Get aGet) {
        Utils.assertRequiredArgument(aGet, "invalid_get");

        final CompletableFuture<HBaseClientModels.Result> future = new CompletableFuture<>();
        vertx.eventBus().send(
                SERVICE_ADDRESS,
                Buffer.buffer(aGet.toByteArray()),
                new DeliveryOptions().addHeader(HEADER_OP, OP_GET),
                (Handler<AsyncResult<Message<Buffer>>>)ar -> {
                    if (ar.succeeded()) {
                        try {
                            final Buffer buffer = ar.result().body();
                            if (buffer.length() > 0) {
                                final byte[] data = buffer.getBytes();
                                final HBaseClientModels.Result opResult = HBaseClientModels.Result.parseFrom(data);
                                future.complete(opResult);
                            }
                            else {
                                future.complete(HBaseClientModels.Result.newBuilder().build());
                            }
                        }
                        catch (InvalidProtocolBufferException ex) {
                            future.completeExceptionally(new CompletionException(new IllegalArgumentException("malformed protobuf packet: " + ex.getMessage())));
                        }
                    }
                    else {
                        future.completeExceptionally(new CompletionException(ar.cause()));
                    }
                }
        );

        return future;
    }

    @Override
    public CompletableFuture<HBaseClientModels.Scanner> scan(HBaseClientModels.Scan aScan) {
        Utils.assertRequiredArgument(aScan, "invalid_scan");

        final CompletableFuture<HBaseClientModels.Scanner> future = new CompletableFuture<>();
        vertx.eventBus().send(
                SERVICE_ADDRESS,
                Buffer.buffer(aScan.toByteArray()),
                new DeliveryOptions().addHeader(HEADER_OP, OP_SCAN),
                (Handler<AsyncResult<Message<Buffer>>>)ar -> {
                    if (ar.succeeded()) {
                        try {
                            final byte[] data = ar.result().body().getBytes();
                            final HBaseClientModels.Scanner scanner = HBaseClientModels.Scanner.parseFrom(data);
                            future.complete(scanner);
                        }
                        catch (InvalidProtocolBufferException ex) {
                            future.completeExceptionally(new CompletionException(new IllegalArgumentException("malformed protobuf packet: " + ex.getMessage())));
                        }
                    }
                    else {
                        future.completeExceptionally(new CompletionException(ar.cause()));
                    }
                }
        );

        return future;
    }

    @Override
    public CompletableFuture<HBaseClientModels.Result> increment(HBaseClientModels.Increment aIncrement) {
        Utils.assertRequiredArgument(aIncrement, "invalid_increment");

        final CompletableFuture<HBaseClientModels.Result> future = new CompletableFuture<>();
        vertx.eventBus().send(
                SERVICE_ADDRESS,
                Buffer.buffer(aIncrement.toByteArray()),
                new DeliveryOptions().addHeader(HEADER_OP, OP_INCREMENT),
                (Handler<AsyncResult<Message<Buffer>>>)ar -> {
                    if (ar.succeeded()) {
                        try {
                            final Buffer buffer = ar.result().body();
                            if (buffer.length() > 0) {
                                final byte[] data = buffer.getBytes();
                                final HBaseClientModels.Result opResult = HBaseClientModels.Result.parseFrom(data);
                                future.complete(opResult);
                            }
                            else {
                                future.complete(HBaseClientModels.Result.newBuilder().build());
                            }
                        }
                        catch (InvalidProtocolBufferException ex) {
                            future.completeExceptionally(new CompletionException(new IllegalArgumentException("malformed protobuf packet: " + ex.getMessage())));
                        }
                    }
                    else {
                        future.completeExceptionally(new CompletionException(ar.cause()));
                    }
                }
        );

        return future;
    }

}
