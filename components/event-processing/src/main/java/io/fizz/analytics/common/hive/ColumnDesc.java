package io.fizz.analytics.common.hive;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;

public class ColumnDesc {
    private final StructField field;
    private final boolean isPartitioned;

    public String title() {
        return field.name();
    }

    DataType dataType() {
        return field.dataType();
    }

    StructField field() {
        return field;
    }

    boolean isPartitioned() {
        return isPartitioned;
    }

    public ColumnDesc(String title, final DataType dataType, boolean isNullable, boolean isPartitioned) {
        field = DataTypes.createStructField(title, dataType, isNullable);
        this.isPartitioned = isPartitioned;
    }
}
