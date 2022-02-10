package io.fizz.command.bus.impl.cluster.kafka;

import io.fizz.command.bus.cluster.AbstractCluster;
import io.fizz.command.bus.impl.generated.KafkaClusterModel;
import io.fizz.command.bus.impl.generated.KafkaClusterNodeAssignmentModel;
import io.fizz.command.bus.impl.generated.KafkaClusterNodeModel;
import io.fizz.command.bus.impl.generated.KafkaClusterPartitionModel;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.command.bus.cluster.ClusterNode;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.BytesDeserializer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaCluster implements AbstractCluster {
    private static final LoggingService.Log log = LoggingService.getLogger(KafkaCluster.class);

    static class ClusterId {
        private final String value;

        public ClusterId() {
            this(UUID.randomUUID().toString());
        }

        public ClusterId(String aValue) {
            Utils.assertRequiredArgument(aValue, "invalid_cluster_id");

            this.value = aValue;
        }

        public String value() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClusterId clusterId = (ClusterId) o;
            return value.equals(clusterId.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    static class Registry {
        private final Map<ClusterId, KafkaCluster> clusterMap = new ConcurrentHashMap<>();

        void add(ClusterId aId, KafkaCluster aCluster) {
            Utils.assertRequiredArgument(aId, "invalid_cluster_id");
            Utils.assertRequiredArgument(aCluster, "invalid_cluster");

            clusterMap.put(aId, aCluster);
        }

        void remove(ClusterId aId) {
            Utils.assertRequiredArgument(aId, "invalid_cluster_id");

            clusterMap.remove(aId);
        }

        KafkaCluster get(ClusterId aId) {
            return clusterMap.get(aId);
        }
    }

    static final String KEY_CLUSTER_NODE_ID = "io.fizz.command.bus.node.id";
    static final String KEY_CLUSTER_NODE_HOST = "io.fizz.command.bus.node.host";
    static final String KEY_CLUSTER_NODE_PORT = "io.fizz.command.bus.node.port";

    static final Registry registry = new Registry();

    private final KafkaConsumer<byte[], byte[]> consumer;
    private final ClusterId id = new ClusterId();
    private final String topic;
    private Map<Integer, String> partitionNodeMap;
    private Map<String, ClusterNode> nodeMap;
    private int partitionCount = 0;

    public KafkaCluster(Vertx aVertx,final
                        String aBootstrapServers,
                        String aTopic,
                        String aGroupId,
                        String aHost,
                        int aPort) {
        Utils.assertRequiredArgument(aVertx, "invalid vertx instance");
        Utils.assertRequiredArgument(aBootstrapServers, "invalid kafka bootstrap servers");
        Utils.assertRequiredArgument(aTopic, "invalid kafka topic");
        Utils.assertRequiredArgument(aGroupId, "invalid consumer group");
        Utils.assertRequiredArgument(aHost, "invalid host");

        this.topic = aTopic;
        consumer = createConsumer(aVertx, aBootstrapServers, aGroupId, aHost, aPort);
        consumer.partitionsAssignedHandler(aPartitions -> log.debug("assigned: " + aPartitions.toString()));
        consumer.partitionsRevokedHandler(aPartitions -> log.debug("revoked: " + aPartitions.toString()));
        consumer.handler(record -> log.debug(record.record().key()));
    }

    @Override
    public CompletableFuture<Void> join() {
        log.debug("node: " + id() + " joining cluster");

        registry.add(id, this);

        return fetchPartitionsCount()
                .thenCompose(aPartitionsCount -> {
                    partitionCount = aPartitionsCount;
                    return subscribe();
                });
    }

    @Override
    public CompletableFuture<Void> leave() {
        log.debug("node: " + id() + " leaving cluster");

        CompletableFuture<Void> left = new CompletableFuture<>();

        consumer.unsubscribe(aResult -> {
            if (aResult.succeeded()) {
                left.complete(null);
            }
            else {
                Utils.failFuture(left, aResult.cause());
            }

            registry.remove(id);
        });

        return left;
    }

    @Override
    public String id() {
        return id.value();
    }

    @Override
    public int partition(byte[] aKey) {
        Utils.assertRequiredArgument(aKey, "invalid_key");

        if (partitionCount <= 0) {
            throw new IllegalStateException("cluster_not_initialized");
        }

        return Math.abs(Utils.murmur2(aKey)) % partitionCount;
    }

    @Override
    public ClusterNode select(byte[] aKey) {
        try {
            int partition = partition(aKey);
            String nodeId = Objects.nonNull(partitionNodeMap) ? partitionNodeMap.get(partition) : null;

            return get(nodeId);
        }
        catch (IllegalArgumentException | IllegalStateException ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public ClusterNode get(String aNodeId) {
        if (Objects.isNull(aNodeId)) {
            return null;
        }

        if (Objects.isNull(nodeMap)) {
            log.warn("tried to select node without initialization");
            return null;
        }

        return nodeMap.get(aNodeId);
    }

    protected void onClusterUpdated(KafkaClusterModel aModel) {
        log.debug(id() + ": received cluster update");

        Map<Integer, String> partitions = new HashMap<>();
        Map<String, ClusterNode> nodes = new HashMap<>();

        for (int ai = 0; ai < aModel.getAssignmentsCount(); ai++) {
            KafkaClusterNodeAssignmentModel assignmentModel = aModel.getAssignments(ai);
            ClusterNode node = node(assignmentModel.getNode());
            nodes.put(node.id(), node);

            for (int pi = 0; pi < assignmentModel.getPartitionsCount(); pi++) {
                KafkaClusterPartitionModel partitionModel = assignmentModel.getPartitions(pi);
                partitions.put(partitionModel.getPartition(), node.id());
            }
        }

        log.debug(id() + ": received " + nodes.size() + " nodes");

        partitionNodeMap = partitions;
        nodeMap = nodes;
    }

    private ClusterNode node(KafkaClusterNodeModel aModel) {
        return new ClusterNode(aModel.getId(), id(), aModel.getHost(),  aModel.getPort());
    }

    private KafkaConsumer<byte[],byte[]> createConsumer(final Vertx aVertx,
                                                        final String aBootstrapServers,
                                                        final String aGroupId,
                                                        final String aHost,
                                                        final int aPort) {
        Map<String, String> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, aBootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class.getName());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, aGroupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        // static group membership configs
        //config.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, id());
        //config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, Integer.toString(15*1000));
        config.put(KEY_CLUSTER_NODE_ID, id.value());
        config.put(KEY_CLUSTER_NODE_HOST, aHost);
        config.put(KEY_CLUSTER_NODE_PORT, Integer.toString(aPort));
        config.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, KafkaClusterAssignor.class.getName());

        return KafkaConsumer.create(aVertx, config);
    }

    private CompletableFuture<Integer> fetchPartitionsCount() {
        CompletableFuture<Integer> fetched = new CompletableFuture<>();

        consumer.partitionsFor(topic, aResult -> {
            if (aResult.succeeded()) {
                fetched.complete(aResult.result().size());
            }
            else {
                Utils.failFuture(fetched, aResult.cause());
            }
        });

        return fetched;
    }

    private CompletableFuture<Void> subscribe() {
        CompletableFuture<Void> subscribed = new CompletableFuture<>();

        consumer.subscribe(topic, aResult -> {
            if (aResult.succeeded()) {
                subscribed.complete(null);
            }
            else {
                Utils.failFuture(subscribed, aResult.cause());
            }
        });

        return subscribed;
    }
}
