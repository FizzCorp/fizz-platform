package io.fizz.command.bus.impl.cluster.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import io.fizz.command.bus.impl.generated.*;
import io.fizz.common.LoggingService;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.Configurable;
import org.apache.kafka.common.TopicPartition;

import java.nio.ByteBuffer;
import java.util.*;

public class KafkaClusterAssignor extends CooperativeStickyAssignor implements Configurable {
    private static final LoggingService.Log log = LoggingService.getLogger(KafkaClusterAssignor.class);

    private String nodeId;
    private String nodeHost;
    private int nodePort;
    private Map<String, Integer> partitionsPerTopic;

    public KafkaClusterAssignor() {
    }

    @Override
    public String name() {
        return "io-fizz-cluster-assignor";
    }

    @Override
    public void configure(Map<String, ?> aMap) {
        nodeId = aMap.get(KafkaCluster.KEY_CLUSTER_NODE_ID).toString();
        nodeHost = aMap.get(KafkaCluster.KEY_CLUSTER_NODE_HOST).toString();
        nodePort = Integer.parseInt(aMap.get(KafkaCluster.KEY_CLUSTER_NODE_PORT).toString());
    }

    @Override
    public ByteBuffer subscriptionUserData(Set<String> topics) {
        KafkaClusterNodeModel node = KafkaClusterNodeModel.newBuilder()
                .setId(nodeId)
                .setHost(nodeHost)
                .setPort(nodePort)
                .build();

        return ByteBuffer.wrap(node.toByteArray());
    }

    @Override
    public void onAssignment(Assignment aAssignment, ConsumerGroupMetadata aMetadata) {
        log.debug("received assignment for generation: " + aMetadata.generationId());

        try {
            KafkaClusterModel clusterModel = KafkaClusterModel.parseFrom(aAssignment.userData());
            KafkaCluster cluster = KafkaCluster.registry.get(new KafkaCluster.ClusterId(nodeId));
            if (Objects.nonNull(cluster)) {
                cluster.onClusterUpdated(clusterModel);
            }
        }
        catch (InvalidProtocolBufferException ex) {
            log.error("protobuf parsing failed: " + ex.getMessage());
        }
    }

    @Override
    public Map<String, List<TopicPartition>> assign(Map<String, Integer> aPartitionsPerTopic,
                                                    Map<String, Subscription> aSubscriptions) {
        partitionsPerTopic = aPartitionsPerTopic;
        return super.assign(aPartitionsPerTopic, aSubscriptions);
    }

    @Override
    public GroupAssignment assign(Cluster aMetadata, GroupSubscription aGroupSubscription) {
        GroupAssignment groupAssignment = super.assign(aMetadata, aGroupSubscription);
        Map<String, Assignment> rawAssignments = groupAssignment.groupAssignment();
        ByteBuffer userData = clusterNodeMeta(aGroupSubscription.groupSubscription(), rawAssignments);

        Map<String, Assignment> assignments = new HashMap<>();

        for (Map.Entry<String, Assignment> entry: rawAssignments.entrySet()) {
            Assignment rawAssignment = entry.getValue();
            Assignment assignment = new Assignment(rawAssignment.partitions(), userData);
            assignments.put(entry.getKey(), assignment);
        }

        return new GroupAssignment(assignments);
    }

    ByteBuffer clusterNodeMeta(Map<String, Subscription> aSubscriptions, Map<String, Assignment> aAssignments) {
        KafkaClusterModel.Builder builder = KafkaClusterModel.newBuilder();
        Map<String, KafkaClusterNodeModel> consumerNodeMap = buildConsumerNodeMap(aSubscriptions);

        for (Map.Entry<String, Integer> entry: partitionsPerTopic.entrySet()) {
            builder.addTopics(KafkaClusterTopicModel.newBuilder()
                    .setTopic(entry.getKey())
                    .setPartitions(entry.getValue())
                    .build());
        }

        for (Map.Entry<String, Assignment> entry: aAssignments.entrySet()) {
            KafkaClusterNodeModel node = consumerNodeMap.get(entry.getKey());
            KafkaClusterNodeAssignmentModel assignmentModel = buildAssignment(node, entry.getValue());
            builder.addAssignments(assignmentModel);
        }

        return ByteBuffer.wrap(builder.build().toByteArray());
    }

    Map<String, KafkaClusterNodeModel> buildConsumerNodeMap(Map<String, Subscription> aSubscriptions) {
        Map<String, KafkaClusterNodeModel> nodes = new HashMap<>();

        for (Map.Entry<String, Subscription> entry: aSubscriptions.entrySet()) {
            String consumerId = entry.getKey();
            Subscription subscription = entry.getValue();

            try {
                KafkaClusterNodeModel node = KafkaClusterNodeModel.parseFrom(subscription.userData());
                nodes.put(consumerId, node);
            }
            catch (InvalidProtocolBufferException ex) {
                log.fatal(ex.getMessage());
            }
        }

        return nodes;
    }

    KafkaClusterNodeAssignmentModel buildAssignment(KafkaClusterNodeModel aNode, Assignment aAssignment) {
        KafkaClusterNodeAssignmentModel.Builder builder = KafkaClusterNodeAssignmentModel.newBuilder();

        builder.setNode(aNode);

        for (TopicPartition partition: aAssignment.partitions()) {
            builder.addPartitions(KafkaClusterPartitionModel.newBuilder()
                    .setTopic(partition.topic())
                    .setPartition(partition.partition())
                    .build());
        }

        return builder.build();
    }
}
