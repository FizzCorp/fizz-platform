package io.fizz.analytics.common.hive;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractHiveTableSchema {
    private String tableName;
    private RowFormat rowFormat;
    private final List<ColumnDesc> partitionedCols = new ArrayList<>();
    private final List<ColumnDesc> nonPartitionedCols = new ArrayList<>();
    private final StructType sparkSchema;

    public enum RowFormat {
        CSV,
        JSON
    }

    public AbstractHiveTableSchema(String aTableName, RowFormat aFormat, final ColumnDesc[] aColumns) {
        if (Objects.isNull(aTableName)) {
            throw new IllegalArgumentException("Table name must be specified.");
        }
        if (Objects.isNull(aColumns) || aColumns.length <= 0) {
            throw new IllegalArgumentException("Can not instantiate a table schema without any columns.");
        }
        if (Objects.isNull(aFormat)) {
            throw new IllegalArgumentException("Must specify a row format for a table schema.");
        }

        tableName = aTableName;
        rowFormat = aFormat;

        final StructField[] fields = new StructField[aColumns.length];
        int fieldIdx = 0;
        for (ColumnDesc col: aColumns) {
            register(col);
            fields[fieldIdx++] = col.field();
        }
        sparkSchema = DataTypes.createStructType(fields);
    }

    public String getTableName() {
        return tableName;
    }

    public StructType schema() {
        return sparkSchema;
    }

    String genDropSQL() {
        return String.format("DROP TABLE IF EXISTS %s", tableName);
    }

    String genCreateSQL(String dataPath) {
        final StringBuilder builder = new StringBuilder();

        builder.append(String.format("CREATE TABLE %s (", tableName));
        for (int ci = 0; ci < nonPartitionedCols.size(); ci++) {
            ColumnDesc col = nonPartitionedCols.get(ci);

            if (ci > 0) {
                builder.append(", ");
            }
            builder.append(String.format("%s %s", col.title(), col.dataType().typeName()));
        }
        builder.append(") ");

        if (partitionedCols.size() > 0) {
            builder.append("PARTITIONED BY (");
            for (int ci = 0; ci < partitionedCols.size(); ci++) {
                ColumnDesc col = partitionedCols.get(ci);

                if (ci > 0) {
                    builder.append(", ");
                }
                builder.append(String.format("%s %s", col.title(), col.dataType().typeName()));
            }
            builder.append(") ");
        }

        appendFormat(builder);
        builder.append(String.format("LOCATION '%s' ", dataPath))
                .append("TBLPROPERTIES('serialization.null.format'='')");

        return builder.toString();
    }

    String genLoadDataSQL() {
        return String.format("MSCK REPAIR TABLE %s", tableName);
    }

    boolean isPartitioned() {
        return partitionedCols.size() > 0;
    }

    private void register(final ColumnDesc col) {
        if (col == null) {
            throw new IllegalArgumentException("Column descriptor must be specified for table schema.");
        }

        if (col.isPartitioned()) {
            partitionedCols.add(col);
        }
        else {
            nonPartitionedCols.add(col);
        }
    }

    private void appendFormat(StringBuilder builder) {
        if (rowFormat == RowFormat.CSV) {
            builder.append("ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' ")
                    .append("STORED AS TEXTFILE ");
        }
        else
        if (rowFormat == RowFormat.JSON) {
            builder.append("ROW FORMAT\n")
                    .append("SERDE 'org.openx.data.jsonserde.JsonSerDe'\n")
            .append("with serdeproperties ('paths'='");

            for (int ci = 0; ci < nonPartitionedCols.size(); ci++) {
                ColumnDesc col = nonPartitionedCols.get(ci);

                if (ci > 0) {
                    builder.append(", ");
                }
                builder.append(col.title());
            }

            builder.append("')\n");
        }
    }
}
