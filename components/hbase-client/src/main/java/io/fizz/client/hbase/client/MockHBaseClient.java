package io.fizz.client.hbase.client;

import com.google.protobuf.ByteString;
import io.fizz.client.hbase.HBaseClientModels;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class MockHBaseClient implements AbstractHBaseClient {
    private static final byte[] NULL_VALUE = new byte[]{};

    private static class Cell {
        public byte[] value;
        public long ttl;
        public long created;
    }
    private static class Cells extends ConcurrentHashMap<String,Cell> {}
    private static class Rows extends ConcurrentHashMap<String,Cells> {}
    private static class ColumnFamilies extends ConcurrentHashMap<String,Rows> {}
    private static class Tables extends ConcurrentHashMap<String,ColumnFamilies> {}
    private static class Namespaces extends ConcurrentHashMap<String,Tables> {}

    private static final Object syncLock = new Object();
    private static final Namespaces db = new Namespaces();

    @Override
    public CompletableFuture<Boolean> put(HBaseClientModels.Put aPutModel) {
        synchronized (syncLock) {
            final ByteString namespace = aPutModel.getTable().getNamespace();
            final ByteString table = aPutModel.getTable().getName();
            final ByteString rowKey = aPutModel.getRowKey();

            if (aPutModel.hasCondition()) {
                boolean matches = matches(namespace, table, rowKey, aPutModel.getCondition());
                if (!matches) {
                    final CompletableFuture<Boolean> future = new CompletableFuture<>();
                    future.complete(false);
                    return future;
                }
            }

            long now = new Date().getTime();
            for (int ci = 0; ci < aPutModel.getColumnsCount(); ci++) {
                final HBaseClientModels.ColumnValue col = aPutModel.getColumns(ci);
                final HBaseClientModels.ColumnCoord coord = col.getKey();
                final ByteString family = coord.getFamily();
                final Cells cells = createOrFindCells(namespace, table, family, rowKey);
                final ByteString qualifier = coord.getQualifier();
                final Cell cell = new Cell();

                cell.value = col.getValue().toByteArray();
                cell.created = now;
                cell.ttl = aPutModel.getTtl();

                cells.put(encodeKey(qualifier.toByteArray()), cell);
            }

            return CompletableFuture.completedFuture(true);
        }
    }

    @Override
    public CompletableFuture<Void> delete(HBaseClientModels.Delete aDeleteModel) {
        synchronized (syncLock) {
            final ByteString namespace = aDeleteModel.getTable().getNamespace();
            final ByteString table = aDeleteModel.getTable().getName();
            final String rowKey = encodeKey(aDeleteModel.getRowKey().toByteArray());

            if (aDeleteModel.getColumnsCount() > 0) {
                for (int ci = 0; ci < aDeleteModel.getColumnsCount(); ci++) {
                    final HBaseClientModels.ColumnCoord coord = aDeleteModel.getColumns(ci);
                    final Cells cells = createOrFindCells(namespace, table, coord.getFamily(), aDeleteModel.getRowKey());
                    cells.remove(encodeKey(coord.getQualifier().toByteArray()));
                }
            }
            else {
                final ColumnFamilies families = createOrFindFamilies(namespace, table);
                for (Map.Entry<String, Rows> family : families.entrySet()) {
                    family.getValue().remove(rowKey);
                }
            }

            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<HBaseClientModels.Result> get(HBaseClientModels.Get aGetModel) {
        synchronized (syncLock) {
            final ByteString namespace = aGetModel.getTable().getNamespace();
            final ByteString table = aGetModel.getTable().getName();
            final ByteString rowKey = aGetModel.getRowKey();
            final HBaseClientModels.Result.Builder res = HBaseClientModels.Result.newBuilder();

            long now = new Date().getTime();

            for (ByteString family: aGetModel.getFamiliesList()) {
                final Cells cells = createOrFindCells(namespace, table, family, rowKey);
                final List<Object> keys = buildSortedKeysList(cells);
                for (Object key: keys) {
                    Cell cell = cells.get(key.toString());
                    if (cell.ttl > 0 && now - cell.created >= cell.ttl) {
                        continue;
                    }
                    if (aGetModel.hasFilter()) {
                        HBaseClientModels.ColumnPaginationFilter filterModel = aGetModel.getFilter();
                        if (filterModel.getLimit() > 0 &&
                                res.getColumnsCount() >= filterModel.getLimit()) {
                            break;
                        }
                        if (!filterModel.getStart().isEmpty() &&
                                compare(filterModel.getStart().toByteArray(), decodeKey(key.toString())) >= 0) {

                            continue;
                        }
                    }

                    res.addColumns(buildColumnValue(family, decodeKey(key.toString()), cell.value));
                }
            }

            for (int ci = 0; ci < aGetModel.getColumnsCount(); ci++) {
                final HBaseClientModels.ColumnCoord coord = aGetModel.getColumns(ci);
                final byte[] value = findCellValue(namespace, table, coord.getFamily(), rowKey, coord.getQualifier());
                if (Objects.isNull(value)) {
                    continue;
                }

                res.addColumns(HBaseClientModels.ColumnValue.newBuilder()
                        .setKey(coord)
                        .setValue(ByteString.copyFrom(value))
                        .build());
            }

            return CompletableFuture.completedFuture(res.build());
        }
    }

    @Override
    public CompletableFuture<HBaseClientModels.Scanner> scan(HBaseClientModels.Scan aScanModel) {
        synchronized (syncLock) {
            final byte[] startRowKey = aScanModel.getReversed() ?
                                       aScanModel.getEndRowKey().toByteArray() :
                                       aScanModel.getStartRowKey().toByteArray();
            final byte[] stopRowKey = aScanModel.getReversed() ?
                                      aScanModel.getStartRowKey().toByteArray() :
                                      aScanModel.getEndRowKey().toByteArray();
            final ByteString namespace = aScanModel.getTable().getNamespace();
            final ByteString table = aScanModel.getTable().getName();
            final int limit = aScanModel.getLimit() == 0 ? Integer.MAX_VALUE : aScanModel.getLimit();

            final HBaseClientModels.Scanner.Builder scanner = HBaseClientModels.Scanner.newBuilder();
            long now = new Date().getTime();

            final List<HBaseClientModels.Result> resultRows = new ArrayList<>();

            for (int fi = 0; fi < aScanModel.getFamiliesCount(); fi++) {
                final ByteString family = aScanModel.getFamilies(fi);
                final Rows rows = createOrFindRows(namespace, table, family);
                int rowCount = 0;
                final List<Object> sortedRowKeys = buildSortedKeysList(rows);

                for (final Object key: sortedRowKeys) {
                    final byte[] rowKey = decodeKey(key.toString());
                    if (compare(startRowKey, rowKey) > 0) {
                        continue;
                    }
                    if (compare(stopRowKey, rowKey) < 0) {
                        continue;
                    }

                    final HBaseClientModels.Result.Builder res = HBaseClientModels.Result.newBuilder();
                    final Cells cells = rows.get(key.toString());
                    for (final Map.Entry<String, Cell> entry: cells.entrySet()) {
                        Cell cell = entry.getValue();
                        if (cell.ttl > 0 && now - cell.created >= cell.ttl) {
                            continue;
                        }

                        res.addColumns(buildColumnValue(family, decodeKey(entry.getKey()), cell.value));
                    }
                    //scanner.addRows(res.build());
                    resultRows.add(res.build());

                    if (limit > 0 && ++rowCount >= limit) {
                        break;
                    }
                }
            }

            if (aScanModel.getReversed()) {
                Collections.reverse(resultRows);
            }

            for (final HBaseClientModels.Result resultRow: resultRows) {
                scanner.addRows(resultRow);
            }

            assert (aScanModel.getColumnsCount() <= 0);

            return CompletableFuture.completedFuture(scanner.build());
        }
    }

    private List<Object> buildSortedKeysList(final Map<String,?> aRows) {
        final Object[] keys = aRows.keySet().toArray();
        final List<byte[]> sortedByteKeys = new ArrayList<>();

        for (Object key: keys) {
            sortedByteKeys.add(decodeKey(key.toString()));
        }
        sortedByteKeys.sort(this::compare);

        final List<Object> sortedKeys = new ArrayList<>();
        for (byte[] key: sortedByteKeys) {
            sortedKeys.add(encodeKey(key));
        }

        return sortedKeys;
    }

    @Override
    public CompletableFuture<HBaseClientModels.Result> increment(HBaseClientModels.Increment aIncrementModel) {
        final HBaseClientModels.ColumnCoord coord = aIncrementModel.getColumn();
        final ByteString namespace = aIncrementModel.getTable().getNamespace();
        final ByteString table = aIncrementModel.getTable().getName();
        final ByteString family = coord.getFamily();
        final ByteString rowKey = aIncrementModel.getRowKey();
        final ByteString qualifier = coord.getQualifier();

        synchronized (syncLock) {
            final Cells cells = createOrFindCells(namespace, table, family, rowKey);
            Cell cell = cells.get(encodeKey(qualifier.toByteArray()));
            long counter = aIncrementModel.getAmount();

            if (Objects.isNull(cell)) {
                cell = new Cell();
                cell.ttl = 0;
                cell.created = new Date().getTime();
                cell.value = null;
            }

            if (!Objects.isNull(cell.value)) {
                counter = Bytes.toLong(cell.value);
                counter += aIncrementModel.getAmount();
            }

            cell.value = Bytes.toBytes(counter);
            cells.put(encodeKey(qualifier.toByteArray()), cell);

            final HBaseClientModels.Result res = HBaseClientModels.Result.newBuilder()
                    .addColumns(
                            HBaseClientModels.ColumnValue.newBuilder()
                                    .setKey(coord)
                                    .setValue(ByteString.copyFrom(Bytes.toBytes(counter)))
                                    .build())
                    .build();

            return CompletableFuture.completedFuture(res);
        }
    }

    private byte[] findCellValue(ByteString aNamespace, ByteString aTable, ByteString aFamily, ByteString aRowKey, ByteString aQualifier) {
        final Cells cells = createOrFindCells(aNamespace, aTable, aFamily, aRowKey);
        final Cell cell = cells.get(encodeKey(aQualifier.toByteArray()));
        return Objects.isNull(cell) ? null : cell.value;
    }

    private Cells createOrFindCells(ByteString aNamespace, ByteString aTable, ByteString aFamily, ByteString aRowKey) {
        final Rows rows = createOrFindRows(aNamespace, aTable, aFamily);
        final String key = encodeKey(aRowKey.toByteArray());
        Cells cells = rows.get(key);
        if (Objects.isNull(cells)) {
            cells = new Cells();
            rows.put(key, cells);
        }
        return cells;
    }

    private Rows createOrFindRows(ByteString aNamespace, ByteString aTable, ByteString aFamily) {
        ColumnFamilies families = createOrFindFamilies(aNamespace, aTable);

        final String key = encodeKey(aFamily.toByteArray());
        Rows rows = families.get(key);
        if (Objects.isNull(rows)) {
            rows = new Rows();
            families.put(key, rows);
        }

        return rows;
    }

    private ColumnFamilies createOrFindFamilies(ByteString aNamespace, ByteString aTable) {
        String key = encodeKey(aNamespace.toByteArray());
        Tables tables = db.get(key);
        if (Objects.isNull(tables)) {
            tables = new Tables();
            db.put(key, tables);
        }

        key = encodeKey(aTable.toByteArray());
        ColumnFamilies families = tables.get(key);
        if (Objects.isNull(families)) {
            families = new ColumnFamilies();
            tables.put(key, families);
        }

        return families;
    }

    private HBaseClientModels.ColumnValue buildColumnValue(final ByteString family, final byte[] qualifier, final byte[] value) {
        return HBaseClientModels.ColumnValue.newBuilder()
                .setKey(HBaseClientModels.ColumnCoord.newBuilder()
                        .setFamily(family)
                        .setQualifier(ByteString.copyFrom(qualifier))
                        .build())
                .setValue(ByteString.copyFrom(value))
                .build();
    }

    private String encodeKey(final byte[] aKey) {
        return Base64.getEncoder().encodeToString(aKey);
    }

    private byte[] decodeKey(final String aKey) {
        return Base64.getDecoder().decode(aKey);
    }

    private int compare(byte[] left, byte[] right) {
        for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
            int a = (left[i] & 0xff);
            int b = (right[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return left.length - right.length;
    }

    private boolean matches(final ByteString aNamespace,
                            final ByteString aTable,
                            final ByteString aRowKey,
                            final HBaseClientModels.ColumnValue condition) {
        final HBaseClientModels.ColumnCoord coord = condition.getKey();
        final byte[] lValue = condition.getValue().toByteArray();
        final byte[] rValue = findCellValue(aNamespace, aTable, coord.getFamily(), aRowKey, coord.getQualifier());

        return Arrays.equals(lValue, Objects.isNull(rValue) ? NULL_VALUE : rValue);
    }
}
