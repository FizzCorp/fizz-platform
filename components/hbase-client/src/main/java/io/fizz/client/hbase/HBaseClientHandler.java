package io.fizz.client.hbase;

import com.google.protobuf.ByteString;
import io.fizz.client.ConfigService;
import io.fizz.client.hbase.client.HBaseClientProxy;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.ColumnCountGetFilter;
import org.apache.hadoop.hbase.filter.ColumnRangeFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HBaseClientHandler {
    private static final String SERVICE_ADDRESS = ConfigService.config().getString("hbase.client.services.address");
    private static final LoggingService.Log logger = LoggingService.getLogger(HBaseClientHandler.class);

    private final Vertx vertx;
    private final AsyncConnection connection;
    private MessageConsumer<Buffer> consumer;

    public HBaseClientHandler(final AsyncConnection aConnection, final Vertx aVertx) {
        if (Objects.isNull(aConnection)) {
            throw new IllegalArgumentException("invalid hbase connection specified.");
        }
        if (Objects.isNull(aVertx)) {
            throw new IllegalArgumentException("invalid vertx instance specified.");
        }
        connection = aConnection;
        vertx = aVertx;
    }

    public void open() {
        if (!Objects.isNull(consumer)) {
            logger.warn("consumer already registered");
        }

        consumer = vertx.eventBus().consumer(SERVICE_ADDRESS);
        consumer.handler(aMessage -> onMessage(connection, aMessage));
    }

    public void close() {
        if (!Objects.isNull(consumer)) {
            consumer.unregister();
            consumer = null;
        }
    }

    private static void onMessage(final AsyncConnection aConnection, final Message<Buffer> aMessage) {
        final String operation = aMessage.headers().get(HBaseClientProxy.HEADER_OP);
        try {
            final byte[] data = aMessage.body().getBytes();
            CompletableFuture<Buffer> responded;

            switch (operation) {
                case HBaseClientProxy.OP_PUT: {
                    responded = put(aConnection, HBaseClientModels.Put.parseFrom(data))
                            .thenApply(aRes -> Buffer.buffer(Bytes.toBytes(aRes)));
                    break;
                }
                case HBaseClientProxy.OP_GET: {
                    responded = get(aConnection, HBaseClientModels.Get.parseFrom(data))
                            .thenApply(aRes -> Buffer.buffer(aRes.toByteArray()));
                    break;
                }
                case HBaseClientProxy.OP_SCAN: {
                    responded = scan(aConnection, HBaseClientModels.Scan.parseFrom(data))
                            .thenApply(aRes -> Buffer.buffer(aRes.toByteArray()));
                    break;
                }
                case HBaseClientProxy.OP_INCREMENT: {
                    responded = increment(aConnection, HBaseClientModels.Increment.parseFrom(data))
                            .thenApply(aRes -> Buffer.buffer(aRes.toByteArray()));
                    break;
                }
                case HBaseClientProxy.OP_DELETE: {
                    responded = delete(aConnection, HBaseClientModels.Delete.parseFrom(data))
                            .thenApply(aRes -> null);
                    break;
                }
                default: {
                    responded = Utils.failedFuture(new IllegalArgumentException("invalid action specified"));
                    break;
                }
            }

            responded.handle((aResponse, aError) -> {
                if (Objects.isNull(aError)) {
                    aMessage.reply(aResponse);
                } else {
                    logger.fatal("invalid action specified");
                    aMessage.fail(0, aError.getMessage());
                }
                return CompletableFuture.completedFuture(null);
            });
        } catch (Exception ex) {
            logger.fatal(ex.getMessage());
            if (Objects.nonNull(ex.getCause())) {
                logger.fatal(ex.getCause().getMessage());
            }
            aMessage.fail(0, ex.getMessage());
        }
    }

    private static CompletableFuture<Boolean> put(final AsyncConnection aConnection,
                                                  final HBaseClientModels.Put aPutModel) {
        logger.info("executing put: " + aPutModel.toString());

        final TableName tableName = getTableName(aPutModel.getTable());
        final AsyncTable<?> table = aConnection.getTable(tableName);

        final byte[] rowKey = aPutModel.getRowKey().toByteArray();
        final Put put = new Put(rowKey);
        final List<HBaseClientModels.ColumnValue> cols = aPutModel.getColumnsList();
        for (final HBaseClientModels.ColumnValue col : cols) {
            put.addColumn(
                    col.getKey().getFamily().toByteArray(),
                    col.getKey().getQualifier().toByteArray(),
                    col.getValue().toByteArray()
            );
        }

        if (aPutModel.getTtl() > 0) {
            put.setTTL(aPutModel.getTtl());
        }

        if (!aPutModel.hasCondition()) {
            return table.put(put).thenApply(v -> true);
        } else {
            final HBaseClientModels.ColumnValue cond = aPutModel.getCondition();
            final HBaseClientModels.ColumnCoord coord = cond.getKey();

            return table.checkAndMutate(rowKey, coord.getFamily().toByteArray())
                    .qualifier(coord.getQualifier().toByteArray())
                    .ifEquals(cond.getValue().toByteArray())
                    .thenPut(put);

        }
    }

    private static CompletableFuture<Void> delete(final AsyncConnection aConnection,
                                                  final HBaseClientModels.Delete aDeleteModel) {
        logger.info("executing put: " + aDeleteModel.toString());

        final TableName tableName = getTableName(aDeleteModel.getTable());
        final AsyncTable<?> table = aConnection.getTable(tableName);

        final byte[] rowKey = aDeleteModel.getRowKey().toByteArray();
        final Delete delete = new Delete(rowKey);


        final List<HBaseClientModels.ColumnCoord> cols = aDeleteModel.getColumnsList();
        for (final HBaseClientModels.ColumnCoord col : cols) {
            delete.addColumn(
                    col.getFamily().toByteArray(),
                    col.getQualifier().toByteArray()
            );

        }
        return table.delete(delete);
    }

    private static CompletableFuture<HBaseClientModels.Result> get(final AsyncConnection aConnection,
                                                                   final HBaseClientModels.Get aGetModel) {
        logger.info("executing get: " + aGetModel.toString());

        final TableName tableName = getTableName(aGetModel.getTable());
        final AsyncTable<?> table = aConnection.getTable(tableName);
        final Get get = new Get(aGetModel.getRowKey().toByteArray());

        for (int fi = 0; fi < aGetModel.getFamiliesCount(); fi++) {
            final byte[] family = aGetModel.getFamilies(fi).toByteArray();
            get.addFamily(family);
        }
        for (int ci = 0; ci < aGetModel.getColumnsCount(); ci++) {
            final HBaseClientModels.ColumnCoord coord = aGetModel.getColumns(ci);
            get.addColumn(coord.getFamily().toByteArray(), coord.getQualifier().toByteArray());
        }
        if (aGetModel.hasFilter()) {
            final HBaseClientModels.ColumnPaginationFilter filterModel = aGetModel.getFilter();
            final FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);

            if (!filterModel.getStart().isEmpty()) {
                filters.addFilter(
                        new ColumnRangeFilter(
                                filterModel.getStart().toByteArray(),
                                false,
                                null,
                                false
                        )
                );
            }
            if (filterModel.getLimit() > 0) {
                filters.addFilter(new ColumnCountGetFilter(filterModel.getLimit()));
            }

        }

        return table.get(get).thenApply(HBaseClientHandler::parseResult);
    }

        private static CompletableFuture<HBaseClientModels.Result> increment(final AsyncConnection aConnection,
                                                                                final HBaseClientModels.Increment aIncrementModel) {
            final TableName tableName = getTableName(aIncrementModel.getTable());
            final AsyncTable<?> table = aConnection.getTable(tableName);

            final Increment increment = new Increment(aIncrementModel.getRowKey().toByteArray());
            final HBaseClientModels.ColumnCoord coord = aIncrementModel.getColumn();
            increment.addColumn(coord.getFamily().toByteArray(), coord.getQualifier().toByteArray(), aIncrementModel.getAmount());

            return table.increment(increment).thenApply(HBaseClientHandler::parseResult);
        }


        private static CompletableFuture<HBaseClientModels.Scanner> scan(final AsyncConnection aConnection,
                                                                     final HBaseClientModels.Scan aScanModel) {
        logger.info("executing scan: " + aScanModel.toString());

        final TableName tableName = getTableName(aScanModel.getTable());
        final AsyncTable<?> table = aConnection.getTable(tableName);
        final Scan scan = new Scan();
        scan.withStartRow(aScanModel.getStartRowKey().toByteArray());
        scan.withStopRow(aScanModel.getEndRowKey().toByteArray());

        for (int fi = 0; fi < aScanModel.getFamiliesCount(); fi++) {
            scan.addFamily(aScanModel.getFamilies(fi).toByteArray());
        }
        for (int ci = 0; ci < aScanModel.getColumnsCount(); ci++) {
            final HBaseClientModels.ColumnCoord coord = aScanModel.getColumns(ci);
            scan.addColumn(coord.getFamily().toByteArray(), coord.getQualifier().toByteArray());
        }


        if (aScanModel.getLimit() > 0) {
            scan.setCaching(aScanModel.getLimit());
            scan.setFilter(new PageFilter(aScanModel.getLimit()));
        }


        scan.setReversed(aScanModel.getReversed());

        return table.scanAll(scan).thenApply(aResults -> {
            final HBaseClientModels.Scanner.Builder scannerModel = HBaseClientModels.Scanner.newBuilder();


            for (Result res : aResults) {
                final HBaseClientModels.Result resultModel = parseResult(res);
                scannerModel.addRows(resultModel);
            }

            return scannerModel.build();

        });
    }


    private static TableName getTableName(final HBaseClientModels.Table aTableModel) {
        return TableName.valueOf(
                aTableModel.getNamespace().toByteArray(),
                aTableModel.getName().toByteArray());
    }

    private static HBaseClientModels.Result parseResult(final Result aResult) {
        final NavigableMap<byte[], NavigableMap<byte[], byte[]>> families = aResult.getNoVersionMap();
        final HBaseClientModels.Result.Builder outRes = HBaseClientModels.Result.newBuilder();

        if (Objects.isNull(families) || families.isEmpty()) {
            return outRes.build();
        }

        for (NavigableMap.Entry<byte[], NavigableMap<byte[], byte[]>> familyEntry : families.entrySet()) {
            final ByteString family = toByteString(familyEntry.getKey());
            final NavigableMap<byte[], byte[]> columns = familyEntry.getValue();

            for (NavigableMap.Entry<byte[], byte[]> columnEntry : columns.entrySet()) {
                final HBaseClientModels.ColumnValue column = HBaseClientModels.ColumnValue.newBuilder()
                        .setKey(
                                HBaseClientModels.ColumnCoord.newBuilder()
                                        .setQualifier(toByteString(columnEntry.getKey()))
                                        .setFamily(family)
                                        .build()
                        )
                        .setValue(toByteString(columnEntry.getValue()))
                        .build();

                outRes.addColumns(column);
            }
        }

        return outRes.build();
    }

    private static ByteString toByteString(final byte[] data) {
        return ByteString.copyFrom(data);
    }
}
