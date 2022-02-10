package io.fizz.client.hbase;

import com.google.protobuf.ByteString;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.HBaseClientProxy;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class HBaseClientTest {
    private static final int TEST_TIMEOUT = 15;
    private static final byte[] NS_DATA = Bytes.toBytes("default");
    private static final byte[] TABLE_DATA = Bytes.toBytes("tbl_data");
    private static final byte[] CF_DATA = Bytes.toBytes("d");
    private static final byte[] ROW_KEY_1 = Bytes.toBytes("rowKey001");
    private static final byte[] COL_QUALIFIER_1 = Bytes.toBytes("key1");
    private static final byte[] CELL_VALUE_1 = Bytes.toBytes("value1");
    private static final byte[] ROW_KEY_2 = Bytes.toBytes("rowKey002");
    private static final byte[] COL_QUALIFIER_2 = Bytes.toBytes("key2");
    private static final byte[] CELL_VALUE_2 = Bytes.toBytes("value2");

    private static Vertx vertx;
    private static AbstractHBaseClient proxy;
    private static AbstractHBaseClient proxy2;
    private static HBaseClientHandler handler;

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws InterruptedException, ExecutionException, IOException {
        final boolean mock = true;

        vertx = Vertx.vertx();
        if (mock) {
            proxy = new MockHBaseClient();
            proxy2 = new MockHBaseClient();
        }
        else {
            final Configuration conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.quorum", "localhost");
            conf.set("hbase.zookeeper.property.clientPort", "2181");
            conf.set("hbase.cluster.distributed", "false");
            conf.set("hbase.client.retries.number", "2");
            conf.set("hbase.rpc.timeout", "100");
            conf.set("hbase.client.pause", "100");
            conf.set("timeout", "100");
            final AsyncConnection connection = ConnectionFactory.createAsyncConnection(conf).get();

            proxy = new HBaseClientProxy(vertx);
            proxy2 = new HBaseClientProxy(vertx);
            handler = new HBaseClientHandler(connection, vertx);
            handler.open();
        }
        aContext.completeNow();
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        if (!Objects.isNull(handler)) {
            handler.close();
        }
        if (!Objects.isNull(vertx)) {
            vertx.close(res -> aContext.completeNow());
        }
    }

    @Test
    @DisplayName("it should put data into hbase table")
    void putTest(VertxTestContext aContext) throws InterruptedException {
        proxy.put(
            buildPut(ROW_KEY_1, COL_QUALIFIER_1, CELL_VALUE_1)
        )
        .thenCompose(status -> {
            Assertions.assertTrue(status);
            return proxy.get(
                    HBaseClientModels.Get.newBuilder()
                            .setRowKey(ByteString.copyFrom(ROW_KEY_1))
                            .setTable(buildTable(NS_DATA, TABLE_DATA))
                            .addFamilies(ByteString.copyFrom(CF_DATA))
                            .build()
            );
        })
        .handle((res, error) -> {
            Assertions.assertEquals(res.getColumnsCount(), 1);

            final HBaseClientModels.ColumnValue col = res.getColumns(0);
            final byte[] qualifier = col.getKey().getQualifier().toByteArray();
            final byte[] value = col.getValue().toByteArray();

            Assertions.assertTrue(Objects.isNull(error));
            Assertions.assertTrue(equals(col.getKey().getFamily().toByteArray(), CF_DATA));
            Assertions.assertTrue(equals(qualifier, COL_QUALIFIER_1));
            Assertions.assertTrue(equals(value, CELL_VALUE_1));

            aContext.completeNow();
            return null;
        });
        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    //@Disabled
    @Test
    @DisplayName("it should get all cells for all family in the specified rows")
    void scanWithFamilyTest(VertxTestContext aContext) throws InterruptedException {
        final byte[] startKey = Bytes.toBytes("rowKey001");
        final byte[] stopKey = Bytes.toBytes("rowKey003");
        stopKey[stopKey.length-1] += 1;

        proxy.put(buildPut(ROW_KEY_1, COL_QUALIFIER_1, CELL_VALUE_1))
        .thenCompose(b -> proxy.put(buildPut(ROW_KEY_2, COL_QUALIFIER_2, CELL_VALUE_2)))
        .thenCompose(b -> {
            // scan using second proxy to validate multi proxy usage
            return proxy2.scan(
                    HBaseClientModels.Scan.newBuilder()
                    .setStartRowKey(ByteString.copyFrom(startKey))
                    .setEndRowKey(ByteString.copyFrom(stopKey))
                    .setTable(buildTable(NS_DATA, TABLE_DATA))
                    .addFamilies(ByteString.copyFrom(CF_DATA))
                    .build()
            );
        })
        .handle((res, error) -> {
            Assertions.assertTrue(Objects.isNull(error));
            Assertions.assertEquals(res.getRowsCount(), 2);

            Assertions.assertEquals(res.getRows(0).getColumnsCount(), 1);
            Assertions.assertTrue(equals(res.getRows(0).getColumns(0).getValue().toByteArray(), CELL_VALUE_1));

            Assertions.assertEquals(res.getRows(1).getColumnsCount(), 1);
            Assertions.assertTrue(equals(res.getRows(1).getColumns(0).getValue().toByteArray(), CELL_VALUE_2));

            aContext.completeNow();
            return null;
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should get all cells for all family in the specified rows")
    void reverseScanWithFamilyTest(VertxTestContext aContext) throws InterruptedException {
        final byte[] startKey = Bytes.toBytes("rowKey003");

        proxy.put(buildPut(ROW_KEY_1, COL_QUALIFIER_1, CELL_VALUE_1))
                .thenCompose(b -> proxy.put(buildPut(ROW_KEY_2, COL_QUALIFIER_2, CELL_VALUE_2)))
                .thenCompose(b -> {
                    // scan using second proxy to validate multi proxy usage
                    return proxy2.scan(
                            HBaseClientModels.Scan.newBuilder()
                                    .setStartRowKey(ByteString.copyFrom(startKey))
                                    .setTable(buildTable(NS_DATA, TABLE_DATA))
                                    .addFamilies(ByteString.copyFrom(CF_DATA))
                                    .setReversed(true)
                                    .build()
                    );
                })
                .handle((res, error) -> {
                    Assertions.assertTrue(Objects.isNull(error));
                    Assertions.assertEquals(res.getRowsCount(), 2);

                    Assertions.assertEquals(res.getRows(0).getColumnsCount(), 1);
                    Assertions.assertTrue(equals(res.getRows(0).getColumns(0).getValue().toByteArray(), CELL_VALUE_2));

                    Assertions.assertEquals(res.getRows(1).getColumnsCount(), 1);
                    Assertions.assertTrue(equals(res.getRows(1).getColumns(0).getValue().toByteArray(), CELL_VALUE_1));

                    aContext.completeNow();
                    return null;
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    void columnRangeScanTest() {
        final ByteString rowKey = ByteString.copyFromUtf8(UUID.randomUUID().toString());
        final ByteString col1 = ByteString.copyFromUtf8("00");
        final ByteString col2 = ByteString.copyFromUtf8("01");
        final ByteString col3 = ByteString.copyFromUtf8("02");
        final ByteString col4 = ByteString.copyFromUtf8("03");

        HBaseClientModels.Put putOp = HBaseClientModels.Put.newBuilder()
                .setTable(buildTable(NS_DATA, TABLE_DATA))
                .setRowKey(rowKey)
                .addColumns(buildColumnValue(col1, col1))
                .addColumns(buildColumnValue(col2, col2))
                .addColumns(buildColumnValue(col3, col3))
                .addColumns(buildColumnValue(col4, col4))
                .build();

        proxy.put(putOp)
                .thenCompose(aSaved -> {
                    Assertions.assertTrue(aSaved);

                    return proxy.get(HBaseClientModels.Get.newBuilder()
                            .setTable(buildTable(NS_DATA, TABLE_DATA))
                            .setRowKey(rowKey)
                            .addFamilies(ByteString.copyFrom(CF_DATA))
                            .setFilter(HBaseClientModels.ColumnPaginationFilter.newBuilder()
                                    .setLimit(2)
                                    .build())
                            .build());
                })
                .thenCompose(aResult -> {
                    Assertions.assertEquals(aResult.getColumnsCount(), 2);

                    HBaseClientModels.ColumnValue resCol1 = aResult.getColumns(0);
                    HBaseClientModels.ColumnValue resCol2 = aResult.getColumns(1);

                    Assertions.assertTrue(resCol1.getValue().equals(col1) || resCol2.getValue().equals(col1));
                    Assertions.assertTrue(resCol1.getValue().equals(col2) || resCol2.getValue().equals(col2));

                    return proxy.get(HBaseClientModels.Get.newBuilder()
                            .setTable(buildTable(NS_DATA, TABLE_DATA))
                            .setRowKey(rowKey)
                            .addFamilies(ByteString.copyFrom(CF_DATA))
                            .setFilter(HBaseClientModels.ColumnPaginationFilter.newBuilder()
                                    .setLimit(2)
                                    .setStart(resCol2.getKey().getQualifier())
                                    .build())
                            .build());
                })
                .thenApply(aResult -> {
                    Assertions.assertEquals(aResult.getColumnsCount(), 2);

                    HBaseClientModels.ColumnValue resCol1 = aResult.getColumns(0);
                    HBaseClientModels.ColumnValue resCol2 = aResult.getColumns(1);

                    Assertions.assertTrue(resCol1.getValue().equals(col3) || resCol2.getValue().equals(col3));
                    Assertions.assertTrue(resCol1.getValue().equals(col4) || resCol2.getValue().equals(col4));

                    return null;
                });
    }

    @Test
    @DisplayName("it should increment counter")
    void incrementTest(VertxTestContext aContext) throws InterruptedException {
        final byte[] COL_COUNTER = Bytes.toBytes("counter");
        final long amount = 5;
        final HBaseClientModels.Increment increment = HBaseClientModels.Increment.newBuilder()
                .setTable(buildTable(NS_DATA, TABLE_DATA))
                .setRowKey(ByteString.copyFrom(COL_COUNTER))
                .setAmount(amount)
                .setColumn(
                        HBaseClientModels.ColumnCoord.newBuilder()
                                .setFamily(ByteString.copyFrom(CF_DATA))
                                .setQualifier(ByteString.copyFrom(Bytes.toBytes("c")))
                                .build()
                )
                .build();

        proxy.increment(increment)
        .thenCompose(res -> proxy.increment(increment))
        .handle((res, error) -> {
            Assertions.assertTrue(Objects.isNull(error));
            Assertions.assertEquals(res.getColumnsCount(), 1);

            final HBaseClientModels.ColumnValue counter = res.getColumns(0);
            final long counterValue = Bytes.toLong(counter.getValue().toByteArray());
            Assertions.assertEquals(counterValue, amount*2);

            aContext.completeNow();
            return null;
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }


    private HBaseClientModels.ColumnValue buildColumnValue(final ByteString aQualifier, final ByteString aValue) {
        return HBaseClientModels.ColumnValue.newBuilder()
                .setKey(HBaseClientModels.ColumnCoord.newBuilder()
                        .setFamily(ByteString.copyFrom(CF_DATA))
                        .setQualifier(aQualifier)
                        .build())
                .setValue(aValue)
                .build();
    }

    private HBaseClientModels.Table buildTable(final byte[] aNamespace, final byte[] aName) {
        return HBaseClientModels.Table.newBuilder()
        .setNamespace(ByteString.copyFrom(aNamespace))
        .setName(ByteString.copyFrom(aName))
        .build();
    }

    private boolean equals(final byte[] aLValue, final byte[] aRValue) {
        final String lvalue = Base64.getEncoder().encodeToString(aLValue);
        final String rvalue = Base64.getEncoder().encodeToString(aRValue);
        return lvalue.equals(rvalue);
    }

    private HBaseClientModels.Put buildPut(final byte[] aRowKey, final byte[] aQualifier, final byte[] aValue) {
        return HBaseClientModels.Put.newBuilder()
            .setRowKey(ByteString.copyFrom(aRowKey))
            .setTable(buildTable(NS_DATA, TABLE_DATA))
            .addColumns(
                HBaseClientModels.ColumnValue.newBuilder()
                .setKey(
                    HBaseClientModels.ColumnCoord.newBuilder()
                    .setFamily(ByteString.copyFrom(CF_DATA))
                    .setQualifier(ByteString.copyFrom(aQualifier))
                    .build()
                )
                .setValue(ByteString.copyFrom(aValue))
                .build()
            )
            .build();
    }
}
