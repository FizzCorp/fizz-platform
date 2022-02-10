package io.fizz.client.hbase;

import com.google.protobuf.ByteString;
import io.fizz.client.hbase.client.HBaseClientProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

class HBaseClientProxyTest {
    private final ByteString namespace = ByteString.copyFromUtf8("test");
    private final ByteString tableName = ByteString.copyFromUtf8("test_table");
    private final ByteString rowKey = ByteString.copyFromUtf8("key");
    private final ByteString columnFamily = ByteString.copyFromUtf8("cf");
    private final Buffer valueResult = Buffer.buffer("1");
    private final HBaseClientModels.Result rowResult = HBaseClientModels.Result.newBuilder()
            .addColumns(HBaseClientModels.ColumnValue.newBuilder()
                    .setKey(HBaseClientModels.ColumnCoord.newBuilder()
                            .setFamily(columnFamily)
                            .build())
                    .setValue(ByteString.copyFromUtf8("value"))
                    .build())
            .build();

    private final HBaseClientModels.Table table = HBaseClientModels.Table.newBuilder()
            .setNamespace(namespace)
            .setName(tableName)
            .build();

    @Test
    void putTest() {
        final HBaseClientModels.Put put = HBaseClientModels.Put.newBuilder()
                .setRowKey(rowKey)
                .setTable(table)
                .build();

        final HBaseClientProxy client = new HBaseClientProxy(buildVertx(buffer -> {
            try {
                final HBaseClientModels.Put sentPut = HBaseClientModels.Put.parseFrom(buffer.getBytes());
                Assertions.assertEquals(sentPut.getTable().getName(), tableName);
                Assertions.assertEquals(sentPut.getTable().getNamespace(), namespace);
                Assertions.assertEquals(sentPut.getRowKey(), rowKey);
            }
            catch (Exception ex ){
                System.out.println(ex.getMessage());
            }
        }, valueResult));
        client.put(put);
    }

    @Test
    void getTest() {
        final HBaseClientModels.Get get = HBaseClientModels.Get.newBuilder()
                .setRowKey(rowKey)
                .setTable(table)
                .build();

        final HBaseClientProxy client = new HBaseClientProxy(buildVertx(aBuffer -> {
            try {
                HBaseClientModels.Get sentGet = HBaseClientModels.Get.parseFrom(aBuffer.getBytes());
                Assertions.assertEquals(sentGet.getTable().getName(), tableName);
                Assertions.assertEquals(sentGet.getTable().getNamespace(), namespace);
                Assertions.assertEquals(sentGet.getRowKey(), rowKey);
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage()  );
            }
        }, Buffer.buffer(rowResult.toByteArray())));

        client.get(get);
    }

    @Test
    void deleteTest() {
        final HBaseClientModels.Delete delete = HBaseClientModels.Delete.newBuilder()
                .setRowKey(rowKey)
                .setTable(table)
                .build();

        final HBaseClientProxy client = new HBaseClientProxy(buildVertx(aBuffer -> {
            try {
                HBaseClientModels.Delete sentDelete = HBaseClientModels.Delete.parseFrom(aBuffer.getBytes());
                Assertions.assertEquals(sentDelete.getTable().getName(), tableName);
                Assertions.assertEquals(sentDelete.getTable().getNamespace(), namespace);
                Assertions.assertEquals(sentDelete.getRowKey(), rowKey);
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, valueResult));

        client.delete(delete);
    }

    @Test
    void scanTest() {
        final HBaseClientModels.Scan scan = HBaseClientModels.Scan.newBuilder()
                .setStartRowKey(rowKey)
                .setEndRowKey(rowKey)
                .setTable(table)
                .build();

        final HBaseClientProxy client = new HBaseClientProxy(buildVertx(aBuffer -> {
            try {
                HBaseClientModels.Scan sentScan = HBaseClientModels.Scan.parseFrom(aBuffer.getBytes());
                Assertions.assertEquals(sentScan.getTable().getName(), tableName);
                Assertions.assertEquals(sentScan.getTable().getNamespace(), namespace);
                Assertions.assertEquals(sentScan.getStartRowKey(), rowKey);
                Assertions.assertEquals(sentScan.getEndRowKey(), rowKey);
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, valueResult));

        client.scan(scan);
    }

    @Test
    void incrementTest() {
        final HBaseClientModels.Increment increment = HBaseClientModels.Increment.newBuilder()
                .setAmount(10)
                .setRowKey(rowKey)
                .setTable(table)
                .build();

        final HBaseClientProxy client = new HBaseClientProxy(buildVertx(aBuffer -> {
            try {
                HBaseClientModels.Increment sentIncrement = HBaseClientModels.Increment.parseFrom(aBuffer.getBytes());
                Assertions.assertEquals(sentIncrement.getTable().getName(), tableName);
                Assertions.assertEquals(sentIncrement.getTable().getNamespace(), namespace);
                Assertions.assertEquals(sentIncrement.getRowKey(), rowKey);
                Assertions.assertEquals(sentIncrement.getAmount(), 10);
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, Buffer.buffer(rowResult.toByteArray())));

        client.increment(increment);
    }

    private Vertx buildVertx(Consumer<Buffer> aConsumer, Buffer aResult) {
        Vertx vertx = mock(Vertx.class);
        EventBus bus = mock(EventBus.class);

        when(vertx.eventBus()).thenReturn(bus);

        when(bus.send(anyString(), any(Object.class))).then(i -> bus);

        when(bus.send(anyString(), any(Object.class), any(DeliveryOptions.class), any()))
                .then(i -> {
                    Handler<AsyncResult<Message<Object>>> handler = i.getArgument(3);
                    AsyncResult<Message<Object>> ar = mock(AsyncResult.class);
                    Message<Object> message = mock(Message.class);

                    when(message.body()).thenReturn(aResult);
                    when(ar.succeeded()).thenReturn(true);
                    when(ar.result()).thenReturn(message);
                    handler.handle(ar);

                    aConsumer.accept(i.getArgument(1));
                    return bus;
                });

        return vertx;
    }
}
