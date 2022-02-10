package io.fizz.command.bus.impl.cluster.command;

import com.google.protobuf.ByteString;
import io.fizz.command.bus.cluster.AbstractCluster;
import io.fizz.command.bus.impl.CommandBus;
import io.fizz.command.bus.impl.ConfigService;
import io.fizz.command.bus.cluster.ClusteredGrpcChannelFactory;
import io.fizz.command.bus.impl.generated.ProxyServiceGrpc;
import io.fizz.command.bus.impl.generated.ReplyModel;
import io.fizz.command.bus.impl.generated.RequestModel;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.command.bus.AbstractCommand;
import io.fizz.command.bus.cluster.ClusterNode;
import io.grpc.Channel;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

public class ClusteredCommandBusProxy extends CommandBus {
    private static final LoggingService.Log log = LoggingService.getLogger(ClusteredCommandBusProxy.class);
    private static final long RPC_TIMEOUT = ConfigService.config().getNumber("command.bus.rpc.timeout.ms").longValue();

    private final ClusteredGrpcChannelFactory channelFactory;
    private final Map<Integer, Executor> executorMap = new HashMap<>();
    private final AbstractCommandBusMetrics metrics;

    public ClusteredCommandBusProxy(Vertx aVertx, ClusteredGrpcChannelFactory aChannelFactory) {
        this(aVertx, aChannelFactory, null);
    }

    public ClusteredCommandBusProxy(Vertx aVertx,
                                    ClusteredGrpcChannelFactory aChannelFactory,
                                    AbstractCommandBusMetrics aMetrics) {
        super(aVertx);

        Utils.assertRequiredArgument(aChannelFactory, "invalid channel factory");

        this.channelFactory = aChannelFactory;
        this.metrics = aMetrics;

        String address = aChannelFactory.cluster().id();
        vertx.eventBus().consumer(address, this::on);
    }

    @Override
    public <T> CompletableFuture<T> execute(AbstractCommand aCommand) {
        try {
            Executor executor = executor(aCommand.key());
            CompletableFuture<T> executed = new CompletableFuture<>();

            executor.execute(() -> {
                ClusterNode node = channelFactory.cluster().select(aCommand.key());
                if (Objects.isNull(node)) {
                    executor.retry();
                    return;
                }

                commandExecutionStarted();
                CompletableFuture<ServiceResult<T>> handled =
                        node.isLocal() ? handleLocal(aCommand) : handleRemote(node, aCommand);
                handled.whenComplete((aResult, aError) -> {
                    commandExecutionEnded();
                    if (Objects.isNull(aError)) {
                        if (aResult.succeeded()) {
                            executed.complete(aResult.reply());
                        }
                        else {
                            executed.completeExceptionally(aResult.cause());
                        }
                        executor.next();
                    }
                    else {
                        commandFailed();
                        log.error(aError.getMessage());
                        executor.retry();
                    }
                });
            });

            return executed;
        }
        catch (Throwable th) {
            return Utils.failedFuture(th);
        }
    }

    private Executor executor(byte[] aKey) {
        AbstractCluster cluster = channelFactory.cluster();
        int partition = cluster.partition(aKey);
        Executor executor = executorMap.get(partition);

        if (Objects.isNull(executor)) {
            executor = new Executor(vertx);
            executorMap.put(partition, executor);
            executor.start();
        }

        return executor;
    }

    protected <T> CompletableFuture<ServiceResult<T>> handleLocal(AbstractCommand aCommand) {
        CompletableFuture<ServiceResult<T>> handled = new CompletableFuture<>();

        super.<T>handle(aCommand)
                .whenComplete((aResult, aError) -> {
                    ServiceResult<T> result = new ServiceResult<>(Objects.isNull(aError), aError, aResult);
                    handled.complete(result);
                });

        return handled;
    }

    private <T> CompletableFuture<ServiceResult<T>> handleRemote(ClusterNode aNode, AbstractCommand aCommand) {
        CompletableFuture<ServiceResult<T>> executed = new CompletableFuture<>();
        ProxyServiceGrpc.ProxyServiceVertxStub client = client(aNode);
        RequestModel request = makeRequest(aNode.id(), CommandSerde.serialize(aCommand));

        commandSent();
        client.withDeadlineAfter(RPC_TIMEOUT, TimeUnit.MILLISECONDS)
                .proxy(request, aResult -> {
                    commandReplied();
                    if (aResult.succeeded()) {
                        ReplyModel reply = aResult.result();
                        try {
                            byte[] payload = reply.getPayload().toByteArray();
                            ServiceResult<T> res = ServiceResultSerde.deserialize(payload);
                            executed.complete(res);
                        }
                        catch (Exception ex) {
                            executed.completeExceptionally(ex);
                        }
                    }
                    else {
                        executed.completeExceptionally(aResult.cause());
                    }
                });

        return executed;
    }

    private void on(Message<byte[]> aMessage) {
        try {
            commandReceived();
            AbstractCommand command = CommandSerde.deserialize(aMessage.body());
            handle(command)
                .whenComplete((aReply, aError) -> {
                    ServiceResult<Object> result = Objects.nonNull(aError) ?
                            new ServiceResult<>(false, error(aError), null) :
                            new ServiceResult<>(true, null, aReply);

                    aMessage.reply(ServiceResultSerde.serialize(result));
                });
        }
        catch (Throwable th) {
            ServiceResult<Object> result = new ServiceResult<>(false, th, null);
            aMessage.reply(ServiceResultSerde.serialize(result));
        }
    }

    private Throwable error(Throwable aError) {
        return aError instanceof CompletionException ? aError.getCause() : aError;
    }

    private RequestModel makeRequest(String aTo, byte[] aPayload) {
        return RequestModel.newBuilder()
                .setTo(aTo)
                .setPayload(ByteString.copyFrom(aPayload))
                .build();
    }

    private ProxyServiceGrpc.ProxyServiceVertxStub client(ClusterNode aNode) {
        Channel channel = channelFactory.build(aNode);
        return ProxyServiceGrpc.newVertxStub(channel);
    }

    private void commandExecutionStarted() {
        if (Objects.nonNull(metrics)) {
            metrics.commandExecutionStarted();
        }
    }

    private void commandExecutionEnded() {
        if (Objects.nonNull(metrics)) {
            metrics.commandExecutionEnded();
        }
    }

    private void commandSent() {
        if (Objects.nonNull(metrics)) {
            metrics.commandSent();
        }
    }

    private void commandReplied() {
        if (Objects.nonNull(metrics)) {
            metrics.commandReplied();
        }
    }

    private void commandFailed() {
        if (Objects.nonNull(metrics)) {
            metrics.commandFailed();
        }
    }

    private void commandReceived() {
        if (Objects.nonNull(metrics)) {
            metrics.commandReceived();
        }
    }
}
