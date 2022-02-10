package io.fizz.analytics.common.hive;

import org.apache.spark.sql.types.DataTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AbstractHiveTableSchemaTest {
    static class MockHiveTableSchema extends AbstractHiveTableSchema{
        public MockHiveTableSchema(String aTableName, RowFormat aFormat, ColumnDesc[] aColumns) {
            super(aTableName, aFormat, aColumns);
        }
    }

    @Test
    @DisplayName("it should create valid partitioned table schema")
    void basicValidSchemaTest() {
        final String tableName = "mock_table";
        final AbstractHiveTableSchema.RowFormat format = AbstractHiveTableSchema.RowFormat.CSV;
        final ColumnDesc[] columns = new ColumnDesc[] {
            new ColumnDesc("col1", DataTypes.StringType, true, true)
        };

        final MockHiveTableSchema schema = new MockHiveTableSchema(tableName, format, columns);
        assert (schema.getTableName().equals(tableName));
        assert (schema.isPartitioned());
    }

    @Test
    @DisplayName("it should create valid non-partitioned table schema")
    void basicValidNonPartitionedSchemaTest() {
        final String tableName = "mock_table";
        final AbstractHiveTableSchema.RowFormat format = AbstractHiveTableSchema.RowFormat.CSV;
        final ColumnDesc[] columns = new ColumnDesc[] {
                new ColumnDesc("col1", DataTypes.StringType, true, false)
        };

        final MockHiveTableSchema schema = new MockHiveTableSchema(tableName, format, columns);
        assert (schema.getTableName().equals(tableName));
        assert (!schema.isPartitioned());
    }

    @Test
    @DisplayName("it should not create table schema for invalid table name")
    void invalidTableNameTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new MockHiveTableSchema(null, null, null);
        });
    }

    @Test
    @DisplayName("it should not create table schema for invalid row format")
    void invalidRowFormatTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new MockHiveTableSchema("mock", null, null);
        });
    }

    @Test
    @DisplayName("it should not create table schema for invalid columns")
    void invalidColumnsTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new MockHiveTableSchema("mock", AbstractHiveTableSchema.RowFormat.CSV, null);
        });
    }

    @Test
    @DisplayName("it should not create table schema for no columns")
    void noColumnsTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new MockHiveTableSchema("mock", AbstractHiveTableSchema.RowFormat.CSV, new ColumnDesc[]{});
        });
    }
}
