package io.fizz.command.bus.impl.cluster.kafka;

import io.fizz.command.bus.impl.generated.KafkaClusterModel;
import io.fizz.command.bus.impl.generated.KafkaClusterNodeAssignmentModel;
import io.fizz.command.bus.impl.generated.KafkaClusterNodeModel;
import io.fizz.common.LoggingService;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

/*
* The following tests do not perform validations but will simulate a scenario. The user is supposed
* validate the behaviour of the cluster using the emitted information.
* */
@Disabled
public class KafkaClusterTest {
    private static final LoggingService.Log log = LoggingService.getLogger(KafkaClusterTest.class);

    private static class TestCluster extends KafkaCluster {
        private static final LoggingService.Log log = LoggingService.getLogger(TestCluster.class);

        public TestCluster(Vertx aVertx,
                           String aBootstrapServers,
                           String aTopic,
                           String aGroupId,
                           String aHost,
                           int aPort) {
            super(aVertx, aBootstrapServers, aTopic, aGroupId, aHost, aPort);
        }

        @Override
        protected void onClusterUpdated(KafkaClusterModel aModel) {
            super.onClusterUpdated(aModel);

            log.debug("----------------------- CLUSTER REPORT -----------------------");
            log.debug("size of cluster: " + aModel.getAssignmentsCount());
            for (int ai = 0; ai < aModel.getAssignmentsCount(); ai++) {
                KafkaClusterNodeAssignmentModel model = aModel.getAssignments(ai);
                log.debug("---------------------");
                emitNode(model.getNode());
                emitPartitions(model);
                log.debug("---------------------");
            }
            log.debug("--------------------------------------------------------------");
        }

        private void emitNode(KafkaClusterNodeModel aNode) {
            log.debug(
                    "node: {id: " + aNode.getId() + ", host: " + aNode.getHost() + ", port: " + aNode.getPort() + "}"
            );
        }

        private void emitPartitions(KafkaClusterNodeAssignmentModel aAssignment) {
            StringBuilder builder = new StringBuilder();

            builder.append("partitions: [");
            for (int pi = 0; pi < aAssignment.getPartitionsCount(); pi++) {
                if (pi > 0) {
                    builder.append(",");
                }

                builder.append(aAssignment.getPartitions(pi).getPartition());
            }
            builder.append("]");

            log.debug(builder.toString());
        }
    }

    private final String TOPIC = "io.fizz.cluster";

    @Test
    @DisplayName("it should maintain memberships and assign partitions during rolling restarts")
    public void rollingRestartTest() throws InterruptedException, ExecutionException {
        final Vertx vertx = Vertx.vertx();
        final KafkaCluster cluster1 = buildTestCluster(vertx, 3000);
        final KafkaCluster cluster2 = buildCluster(vertx, 3001);
        final KafkaCluster cluster3 = buildCluster(vertx, 3002);

        emitStageStart("Initial Cluster");
        cluster1.join().get();
        cluster2.join().get();
        cluster3.join().get();
        Thread.sleep(10*1000L);

        emitStageStart("Node 1 Leaves");
        cluster1.leave().get();
        Thread.sleep(10*1000L);

        emitStageStart("Node 1 Joins");
        cluster1.join().get();
        Thread.sleep(10*1000L);

        emitStageStart("Node 2 Leaves");
        cluster2.leave().get();
        Thread.sleep(10*1000L);

        emitStageStart("Node 2 Joins");
        cluster2.join().get();
        Thread.sleep(10*1000L);

        emitStageStart("Node 3 Leaves");
        cluster3.leave().get();
        Thread.sleep(10*1000L);

        emitStageStart("Node 3 Joins");
        cluster3.join().get();
        Thread.sleep(10*1000L);
    }

    @Test
    @DisplayName("it should maintain memberships and assign partitions when scaling up")
    public void scaleUpTest() throws InterruptedException, ExecutionException {
        final Vertx vertx = Vertx.vertx();
        final KafkaCluster cluster1 = buildTestCluster(vertx, 3000);
        final KafkaCluster cluster2 = buildCluster(vertx, 3001);
        final KafkaCluster cluster3 = buildCluster(vertx, 3002);
        final KafkaCluster cluster4 = buildCluster(vertx, 3003);

        emitStageStart("Initial Cluster");
        cluster1.join().get();
        cluster2.join().get();
        Thread.sleep(10*1000L);

        emitStageStart("Node 3 & 4 Join");
        cluster3.join().get();
        cluster4.join().get();
        Thread.sleep(10*1000L);
    }

    /*
    * The procedure to run the scale down test is as follows:
    * 1. run scaleDownPersistentMembersTest. this includes members that will remain in the cluster
    * 2. the cluster report should show two nodes (with all partitions assigned amongst them)
    * 3. run scaleDownNonPersistentMembersTest. this includes members that will simulate node failure
    * 4. the cluster report in the persistent group should show now 4 nodes
    * 5. stop the scaleDownNonPersistentMembersTest (to simulate failure of node 3 & 4)
    * 6. the cluster report should show that there are 2 nodes left will all partitions assigned
    *
    * */
    @Test
    public void scaleDownPersistentMembersTest() throws InterruptedException, ExecutionException {
        final Vertx vertx = Vertx.vertx();
        final KafkaCluster cluster1 = buildTestCluster(vertx, 3001);
        final KafkaCluster cluster2 = buildCluster(vertx, 3002);

        emitStageStart("Initial Cluster");
        cluster1.join().get();
        cluster2.join().get();
        Thread.sleep(300*1000L);
    }

    @Test
    @DisplayName("it should maintain memberships and assign partitions when scaling down")
    public void scaleDownNonPersistentMembersTest() throws InterruptedException, ExecutionException {
        final Vertx vertx = Vertx.vertx();
        KafkaCluster cluster3 = buildCluster(vertx, 3003);
        KafkaCluster cluster4 = buildCluster(vertx, 3004);

        emitStageStart("Initial Cluster");
        cluster3.join().get();
        cluster4.join().get();
        Thread.sleep(300*1000L);
    }

    private void emitStageStart(final String aTitle) {
        log.debug("============================= " + aTitle + " =============================");
    }

    private KafkaCluster buildTestCluster(Vertx aVertx, int aPort) {
        return new TestCluster(
                aVertx,
                "localhost:9092",
                TOPIC,
                "io.fizz.command.bus",
                "localhost",
                aPort
        );
    }

    private KafkaCluster buildCluster(Vertx aVertx, int aPort) {
        return new KafkaCluster(
                aVertx,
                "localhost:9092",
                TOPIC,
                "io.fizz.command.bus",
                "localhost",
                aPort
        );
    }
}
