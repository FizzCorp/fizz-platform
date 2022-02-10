package io.fizz.command.bus.cluster;

import java.util.concurrent.CompletableFuture;

public interface AbstractCluster {
    CompletableFuture<Void> join();
    CompletableFuture<Void> leave();
    String id();
    int partition(byte[] aKey);
    ClusterNode select(byte[] aKey);
    ClusterNode get(String aNodeId);
}
