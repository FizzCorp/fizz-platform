package io.fizz.client.hbase;

import io.fizz.client.hbase.client.MockHBaseClient;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class HBaseUIServiceTest {
    private static final int TEST_TIMEOUT = 1500;
    private static final byte[] ID_NAMESPACE = Bytes.toBytes("global");

    private static Vertx vertx;
    private static HBaseUIDService idService;

    @BeforeAll
    static void setUp(VertxTestContext aContext) {
        vertx = Vertx.vertx();
        idService = new HBaseUIDService(new MockHBaseClient(), "test_namespace");

        aContext.completeNow();
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        if (!Objects.isNull(vertx)) {
            vertx.close(res -> aContext.completeNow());
        }
        aContext.completeNow();
    }

    @Test
    @DisplayName("it should generate id in sequence")
    void generateSequenceIdTest(VertxTestContext aContext) throws InterruptedException {
        idService.nextId(ID_NAMESPACE)
                .thenCompose(id -> {
                    Assertions.assertEquals((long)id, 1L);
                    return idService.nextId(ID_NAMESPACE);
                })
                .thenCompose(id -> {
                    Assertions.assertEquals((long)id, 2L);
                    return idService.nextId(ID_NAMESPACE);
                })
                .thenCompose(id -> {
                    Assertions.assertEquals((long)id, 3L);
                    return idService.nextId(Bytes.toBytes(UUID.randomUUID().toString()));
                })
                .handle((id, error) -> {
                    Assertions.assertTrue(Objects.isNull(error));
                    Assertions.assertEquals((long)id, 1L);
                    aContext.completeNow();
                    return null;
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should map an id to a random id")
    void generateRandomIdTest(VertxTestContext aContext) throws InterruptedException {
        final byte[] name = Bytes.toBytes("my ugly://and_non_conformant_id \uD83D\uDE05");
        idService.createOrFindId(ID_NAMESPACE, name, true)
                .thenCompose(aCreatedId -> {
                    return idService.createOrFindId(ID_NAMESPACE, name, true)
                            .thenCompose(aMappedId -> {
                                Assertions.assertEquals(aCreatedId, aMappedId);
                                return idService.createOrFindId(ID_NAMESPACE, Bytes.toBytes(UUID.randomUUID().toString()), true);
                            })
                            .thenCompose(aMappedId -> {
                                Assertions.assertNotEquals(aCreatedId, aMappedId);
                                return idService.createOrFindId(Bytes.toBytes(UUID.randomUUID().toString()), name, true);
                            })
                            .handle((aMappedId, error) -> {
                                Assertions.assertNotEquals(aCreatedId, aMappedId);
                                aContext.completeNow();
                                return null;
                            });
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should fail when invalid namespace is specified for sequential id")
    void invalidSeqNamespaceTest(VertxTestContext aContext) throws InterruptedException {
        idService.nextId(null)
                .handle((aId, aError) -> {
                    Assertions.assertTrue(aError instanceof CompletionException);
                    Assertions.assertTrue(aError.getCause() instanceof IllegalArgumentException);
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(null);
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));

    }

    @Test
    @DisplayName(("it should fail when namespace is missing for mapped id"))
    void missingMappedIdNamespaceTest(VertxTestContext aContext) throws InterruptedException {
        idService.createOrFindId(null, Bytes.toBytes("test"), true)
                .handle((aId, aError) -> {
                    Assertions.assertTrue(aError instanceof CompletionException);
                    Assertions.assertTrue(aError.getCause() instanceof IllegalArgumentException);
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(null);
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName(("it should fail when namespace is reserved for mapped id"))
    void reservedMappedIdNamespaceTest(VertxTestContext aContext) throws InterruptedException {
        idService.createOrFindId(new byte[]{0}, Bytes.toBytes("test"), true)
                .handle((aId, aError) -> {
                    Assertions.assertTrue(aError instanceof CompletionException);
                    Assertions.assertTrue(aError.getCause() instanceof IllegalArgumentException);
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(null);
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName(("it should fail when invalid name is specified for mapped id"))
    void missingMappedIdNameTest(VertxTestContext aContext) throws InterruptedException {
        idService.createOrFindId(ID_NAMESPACE, null, true)
                .handle((aId, aError) -> {
                    Assertions.assertTrue(aError instanceof CompletionException);
                    Assertions.assertTrue(aError.getCause() instanceof IllegalArgumentException);
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(null);
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName(("it should fail when empty name is specified for mapped id"))
    void emptyMappedIdNameTest(VertxTestContext aContext) throws InterruptedException {
        idService.createOrFindId(ID_NAMESPACE, Bytes.toBytes(""), true)
                .handle((aId, aError) -> {
                    Assertions.assertTrue(aError instanceof CompletionException);
                    Assertions.assertTrue(aError.getCause() instanceof IllegalArgumentException);
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(null);
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

}
