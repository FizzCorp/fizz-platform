package io.fizz.command.bus.impl.cluster.command;

import io.fizz.command.bus.CommandHandler;
import io.fizz.command.bus.cluster.AbstractCluster;
import io.fizz.command.bus.cluster.ClusteredGrpcChannelFactory;
import io.fizz.command.bus.impl.cluster.kafka.KafkaCluster;
import io.fizz.common.LoggingService;
import io.fizz.command.bus.AbstractCommand;
import io.fizz.command.bus.AbstractCommandBus;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * The following test the features of the clustered version of the command bus.
 * These tests are not fully automated and require the person running the tests to view the output for validation.
 *
 * The tests are performed as follows:
 * 1. Run Kafka locally (follow the instructions here: https://kafka.apache.org/quickstart)
 * 2. Create a topic "io.fizz.cluster" will 4 partitions.
 * 3. Run testCluster1 (this will run a cluster that sends messages over the command bus)
 * 4. Run testCluster2 (this will run a cluster that only executes commands received over the bus)
 *
 * The message emitter in each cluster will output some stats:
 * 1. The count of commands sent, acks received for command, command received for execution and errors.
 * 2. A frequency distribution of messages sent by key.
 * 3. A frequency distribution of messages received by key.
 *
 * These stats should be correlated to make sure the command bus is fulfilling its guarantees in different
 * scenarios. For instance stopping test cluster 2 simulate a process being terminated. This should not result
 * in any errors or lost commands. Also the request rate should not be stalled for all partitions.
 *
 * */
@Disabled
@ExtendWith(VertxExtension.class)
public class ClusteredCommandBusTest {
    private static class TestCommand implements AbstractCommand {
        private final byte[] key;

        public TestCommand(byte aKey) {
            this.key = new byte[]{aKey};
        }

        @Override
        public byte[] key() {
            return key;
        }
    }

    public static class CommandBusMetrics implements AbstractCommandBusMetrics {
        private static final LoggingService.Log log = LoggingService.getLogger(CommandBusMetrics.class);

        private int executionStarted = 0;
        private int executionEnded = 0;
        private int errors = 0;
        private int sent = 0;
        private int replied = 0;
        private int received = 0;
        private final int[] receivedKeys;
        private final int[] sentKeys;

        public CommandBusMetrics(int numKeys) {
            receivedKeys = new int[numKeys];
            sentKeys = new int[numKeys];
        }

        public void receivedKey(int aKey) {
            receivedKeys[aKey]++;
        }

        public void sentKey(int aKey) {
            sentKeys[aKey]++;
        }

        @Override
        public void commandExecutionStarted() {
            executionStarted++;
        }

        @Override
        public void commandExecutionEnded() {
            executionEnded++;
        }

        @Override
        public void commandSent() {
            sent++;
        }

        @Override
        public void commandReplied() {
            replied++;
        }

        @Override
        public void commandReceived() {
            received++;
        }

        @Override
        public void commandFailed() {
            errors++;
        }

        public void print() {
            log.debug("executions started: " + executionStarted + ", executions ended: " + executionEnded);
            log.debug("sent: " + sent + ", replied: " + replied + ", received: " + received + ", errors: " + errors);
            print("messages sent", sentKeys);
            print("messages received", receivedKeys);
        }

        private void print(String aTitle, int[] keys) {
            StringBuilder builder = new StringBuilder();
            builder.append(aTitle).append(": [");
            for (int ki = 0; ki < receivedKeys.length; ki++) {
                if (ki > 0) {
                    builder.append(", ");
                }
                builder.append(ki);
                builder.append(":");
                builder.append(keys[ki]);
            }
            builder.append("]");

            log.debug(builder.toString());
        }
    }

    public static class MessageEmitter {
        public static final byte[] messageKeys = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        private static final int RESET_TICKS = 30;

        private int ticks = 0;
        private boolean canSend = true;
        private final AbstractCommandBus bus;
        private final CommandBusMetrics metrics;

        public MessageEmitter(AbstractCommandBus aBus, CommandBusMetrics aMetrics) {
            this.bus = aBus;
            this.metrics = aMetrics;
        }

        public void setCanSend(boolean canSend) {
            this.canSend = canSend;
        }

        public void tick() {
            ticks++;
            if (ticks >= RESET_TICKS) {
                metrics.print();
                ticks = 0;
            }
            if (canSend) {
                send();
            }
        }

        @CommandHandler
        public CompletableFuture<Object> onCommand(TestCommand aCommand) {
            int key = aCommand.key()[0];
            metrics.receivedKey(key);
            return CompletableFuture.completedFuture(1);
        }

        public void send() {
            byte key = key();
            metrics.sentKey(key);
            bus.execute(new TestCommand(key));
        }

        private byte key() {
            int idx = (int)(Math.random()*messageKeys.length-1);
            return messageKeys[idx];
        }
    }

    private static class Server {
        private final Vertx vertx = Vertx.vertx();

        void open(int aPort, boolean aCanSend) throws Throwable {
            startRPCServer(vertx, aPort).get();

            AbstractCluster cluster = createCluster(vertx, aPort);
            cluster.join().get();

            ClusteredGrpcChannelFactory channelFactory = new ClusteredGrpcChannelFactory(vertx, cluster);
            CommandBusMetrics metrics = new CommandBusMetrics(MessageEmitter.messageKeys.length);
            AbstractCommandBus bus = AbstractCommandBus.createClustered(vertx, channelFactory, metrics);

            MessageEmitter emitter = new MessageEmitter(bus, metrics);
            emitter.setCanSend(aCanSend);
            bus.register(emitter);
            vertx.setPeriodic(50L, aTimerId -> emitter.tick());
        }

        private CompletableFuture<VertxServer> startRPCServer(Vertx aVertx, int aPort) {
            CompletableFuture<VertxServer> created = new CompletableFuture<>();

            VertxServer rpcServer = VertxServerBuilder
                    .forPort(aVertx, aPort)
                    .addService(new ProxyService(aVertx))
                    .build();

            rpcServer.start(aResult -> {
                if (aResult.succeeded()) {
                    created.complete(rpcServer);
                }
                else {
                    created.completeExceptionally(aResult.cause());
                }
            });

            return created;
        }

        protected AbstractCluster createCluster(Vertx aVertx, int aPort) {
            final String TOPIC = "io.fizz.cluster";

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

    @Test
    public void testCluster1() throws Throwable {
        VertxTestContext testContext = new VertxTestContext();
        Server server = new Server();
        server.open(3010, true);
        Assertions.assertTrue(testContext.awaitCompletion(300, TimeUnit.SECONDS));
    }

    @Test
    public void testCluster2() throws Throwable {
        VertxTestContext testContext = new VertxTestContext();
        Server server = new Server();
        server.open(3011, false);

        Assertions.assertTrue(testContext.awaitCompletion(300, TimeUnit.SECONDS));
    }
}
