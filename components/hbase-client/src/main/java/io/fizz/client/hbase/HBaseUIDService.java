package io.fizz.client.hbase;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.ByteString;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.Utils;
import io.fizz.common.application.AbstractUIDService;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class HBaseUIDService implements AbstractUIDService {
    private static final ByteString NULL_VALUE = ByteString.copyFrom(new byte[]{});
    private static final SecureRandom randomGenerator = new SecureRandom(Bytes.toBytes(System.currentTimeMillis()));

    private static final byte[] TABLE_NAME = Bytes.toBytes("tbl_uid");

    private static final ByteString CF_COUNTER = ByteString.copyFrom(Bytes.toBytes("ctr"));
    private static final ByteString COL_COUNTER_VALUE = ByteString.copyFrom(Bytes.toBytes("v"));
    private static final ByteString CF_ID = ByteString.copyFrom(Bytes.toBytes("id"));
    private static final ByteString COL_ID_VALUE = ByteString.copyFrom(Bytes.toBytes("v"));
    private static final byte[] RK_GLOBAL_ID_NS = new byte[]{0};

    private final AbstractHBaseClient client;
    private final HBaseClientModels.Table TABLE_UID;
    private final Cache<ByteString, Long> idCache;

    public HBaseUIDService(final AbstractHBaseClient aClient, final String aNamespace) {
        Utils.assertRequiredArgument(aClient, "invalid hbase client specified");
        Utils.assertRequiredArgument(aNamespace, "invalid namespace specified");

        client = aClient;
        TABLE_UID = buildTable(Bytes.toBytes(aNamespace), TABLE_NAME);
        idCache = CacheBuilder.newBuilder().maximumSize(50000).build();
    }

    @Override
    public CompletableFuture<Long> nextId() {
        return incrementCounter(RK_GLOBAL_ID_NS);
    }

    @Override
    public CompletableFuture<Long> nextId(byte[] aNamespace) {
        if (isInvalidNamespace(aNamespace)) {
            return Utils.failedFuture(new IllegalArgumentException("invalid namespace specified."));
        }

        return incrementCounter(aNamespace);
    }

    @Override
    public CompletableFuture<Long> findId(byte[] aName) {
        final ByteString rowKey = buildRowKey(RK_GLOBAL_ID_NS, aName);

        return fetchMappedId(rowKey);
    }

    @Override
    public CompletableFuture<Long> createOrFindId(byte[] aName, boolean aRandomizeId) {
        return generateRandomId(RK_GLOBAL_ID_NS, aName, aRandomizeId);
    }

    @Override
    public CompletableFuture<Long> createOrFindId(byte[] aNamespace, byte[] aName, boolean aRandomizeId) {
        if (isInvalidNamespace(aNamespace)) {
            return Utils.failedFuture(new IllegalArgumentException("invalid namespace specified."));
        }

        return generateRandomId(aNamespace, aName, aRandomizeId);
    }

    private CompletableFuture<Long> incrementCounter(byte[] aNamespace) {
        final CompletableFuture<Long> future = new CompletableFuture<>();

        client.increment(
                HBaseClientModels.Increment.newBuilder()
                        .setTable(TABLE_UID)
                        .setRowKey(ByteString.copyFrom(aNamespace))
                        .setColumn(HBaseClientModels.ColumnCoord.newBuilder()
                                .setFamily(CF_COUNTER)
                                .setQualifier(COL_COUNTER_VALUE)
                                .build())
                        .setAmount(1L)
                        .build()
        )
                .handle((res, error) -> {
                    if (!Objects.isNull(error)) {
                        future.completeExceptionally(error.getCause());
                    }
                    else {
                        if (res.getColumnsCount() <= 0) {
                            future.completeExceptionally(
                                    new CompletionException(new IOException("No columns returned in increment operation."))
                            );
                        } else {
                            final byte[] value = res.getColumns(0).getValue().toByteArray();
                            future.complete(Bytes.toLong(value));
                        }
                    }
                    return CompletableFuture.completedFuture(null);
                });

        return future;
    }

    private CompletableFuture<Long> generateRandomId(byte[] aNamespace, byte[] aName, boolean aRandomizeId) {
        if (Objects.isNull(aName) || aName.length <= 0) {
            return Utils.failedFuture(new IllegalArgumentException("invalid name specified."));
        }

        final ByteString rowKey = buildRowKey(aNamespace, aName);

        return fetchMappedId(rowKey)
                .thenCompose(aId -> {
                    if (Objects.isNull(aId)) {
                        return createMappedId(aNamespace, rowKey, aRandomizeId);
                    }
                    else {
                        return CompletableFuture.completedFuture(aId);
                    }
                });
    }

    private ByteString buildRowKey(byte[] aNamespace, byte[] aName) {
        final byte[] buffer = new byte[aNamespace.length + aName.length];
        System.arraycopy(aNamespace, 0, buffer, 0, aNamespace.length);
        System.arraycopy(aName, 0, buffer, aNamespace.length, aName.length);
        return ByteString.copyFrom(buffer);
    }

    private boolean isInvalidNamespace(byte[] aNamespace) {
        return Objects.isNull(aNamespace) ||
                aNamespace.length <= 0 ||
                Arrays.equals(aNamespace, RK_GLOBAL_ID_NS);
    }

    private CompletableFuture<Long> createMappedId(final byte[] aNamespace,
                                                   final ByteString aRowKey,
                                                   boolean aRandomizeId) {
        return incrementCounter(aNamespace)
                .thenCompose(aSeqId -> aRandomizeId ? buildRandomId(aSeqId) : CompletableFuture.completedFuture(aSeqId))
                .thenCompose(aId -> putMappedId(aRowKey, aId))
                .thenCompose(aId -> {
                    if (Objects.isNull(aId)) {
                        return fetchMappedId(aRowKey);
                    }
                    else {
                        return CompletableFuture.completedFuture(aId);
                    }
                });
    }

    private CompletableFuture<Long> fetchMappedId(final ByteString aRowKey) {
        final Long cachedId = idCache.getIfPresent(aRowKey);
        if (Objects.nonNull(cachedId)) {
            return CompletableFuture.completedFuture(cachedId);
        }

        return client.get(
                HBaseClientModels.Get.newBuilder()
                        .setTable(TABLE_UID)
                        .setRowKey(aRowKey)
                        .addColumns(
                                HBaseClientModels.ColumnCoord.newBuilder()
                                        .setFamily(CF_ID)
                                        .setQualifier(COL_ID_VALUE)
                                        .build()
                        )
                        .build()
        )
                .thenCompose(res -> {
                    if (res.getColumnsCount() > 0) {
                        final HBaseClientModels.ColumnValue col = res.getColumns(0);
                        final Long id = Bytes.toLong(col.getValue().toByteArray());

                        idCache.put(aRowKey, id);

                        return CompletableFuture.completedFuture(id);
                    }
                    else {
                        return CompletableFuture.completedFuture(null);
                    }
                });
    }

    private CompletableFuture<Long> putMappedId(final ByteString aRowKey, long aId) {
        final HBaseClientModels.ColumnCoord coord = HBaseClientModels.ColumnCoord.newBuilder()
                .setFamily(CF_ID)
                .setQualifier(COL_ID_VALUE)
                .build();

        return client.put(
                HBaseClientModels.Put.newBuilder()
                        .setTable(TABLE_UID)
                        .setRowKey(aRowKey)
                        .addColumns(
                                HBaseClientModels.ColumnValue.newBuilder()
                                        .setKey(coord)
                                        .setValue(ByteString.copyFrom(Bytes.toBytes(aId)))
                                        .build()
                        )
                        .setCondition(
                                HBaseClientModels.ColumnValue.newBuilder()
                                        .setKey(coord)
                                        .setValue(NULL_VALUE)
                                        .build()
                        )
                        .build()
        )
                .thenCompose(res -> {
                    if (res) {
                        idCache.put(aRowKey, aId);
                        return CompletableFuture.completedFuture(aId);
                    }
                    else {
                        return CompletableFuture.completedFuture(null);
                    }
                });
    }

    private CompletableFuture<Long> buildRandomId(long aSequenceNumber) {
        final byte[] random = new byte[2];
        randomGenerator.nextBytes(random);

        // <14bits random><50bits sequence id)
        // this will generate 1.125 quadrillion unique random ids
        long id = ((long)random[1]<<56) | // write 8 bits
                (((long)random[0]<<48)&0x00FC00000000000000L) | // write 6 bits
                aSequenceNumber&0x0003FFFFFFFFFFFFL; // write 50 bits for sequence
        return CompletableFuture.completedFuture(id);
    }

    private static HBaseClientModels.Table buildTable(final byte[] aNamespace, final byte[] aTableName) {
        return HBaseClientModels.Table.newBuilder()
                .setNamespace(ByteString.copyFrom(aNamespace))
                .setName(ByteString.copyFrom(aTableName))
                .build();
    }

}
