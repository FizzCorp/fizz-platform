package io.fizz.analytics.common.hive;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import org.junit.jupiter.api.Test;

public class ColumnDescTest {
    @Test
    void validateColumnDescData() {
        final String title = "test_col";
        final DataType type = DataTypes.StringType;
        final boolean isPartitioned = true;

        final ColumnDesc col = new ColumnDesc(title, type, false, isPartitioned);
        assert (col.title().equals(title));
        assert (col.dataType().equals(type));
        assert (col.isPartitioned() == isPartitioned);
        assert (col.field().dataType().equals(type));
    }
}
