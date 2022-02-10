package io.fizz.command.bus.cluster;

import io.fizz.common.Utils;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxChannelBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClusteredGrpcChannelFactory {
    private final AbstractCluster cluster;
    private final Vertx vertx;
    private final Map<String, ManagedChannel> channels = new HashMap<>();

    public ClusteredGrpcChannelFactory(Vertx aVertx, AbstractCluster aCluster) {
        Utils.assertRequiredArgument(aVertx, "invalid vertx instance");
        Utils.assertRequiredArgument(aCluster, "invalid cluster");

        this.cluster = aCluster;
        this.vertx = aVertx;
    }

    public AbstractCluster cluster() {
        return cluster;
    }

    public Channel build(byte[] aKey) {
        Utils.assertRequiredArgument(aKey, "invalid key");

        ClusterNode node = cluster.select(aKey);
        if (Objects.isNull(node)) {
            return null;
        }

        return build(node);
    }

    public Channel build(ClusterNode aNode) {
        ManagedChannel channel = channels.get(aNode.id());
        if (Objects.isNull(channel)) {
            channel = VertxChannelBuilder
                    .forAddress(vertx, aNode.host(), aNode.port())
                    .usePlaintext(true)
                    .build();

            channels.put(aNode.id(), channel);
        }

        return channel;
    }
}
