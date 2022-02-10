package io.fizz.analytics.common.projections.aggregation;

import io.fizz.analytics.common.source.hive.HiveKeywordsTableSchema;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

import java.util.Iterator;

public class SentimentScoreCounter implements Function<Tuple2<String,Iterable<Row>>, Row> {
    public static final String COL_APP_ID = "aid";
    public static final String COL_SCORE = "score";
    public static final String COL_COUNT = "count";

    public static final StructType schema = DataTypes.createStructType(
            new StructField[] {
                    DataTypes.createStructField(COL_APP_ID, DataTypes.StringType, false),
                    DataTypes.createStructField(COL_SCORE, DataTypes.IntegerType, false),
                    DataTypes.createStructField(COL_COUNT, DataTypes.IntegerType, false)
            }
    );

    public static Encoder<Row> encoder() {
        return RowEncoder.apply(schema);
    }

    @Override
    public Row call(Tuple2<String, Iterable<Row>> aGroup) {
        int count = 0;
        int value = 0;
        String appId = null;
        final Iterator<Row> iterator = aGroup._2().iterator();

        while (iterator.hasNext()) {
            final Row row = iterator.next();
            if (appId == null) {
                final double score = row.getDouble(row.fieldIndex(HiveKeywordsTableSchema.COL_SENTIMENT_SCORE));
                if (score < 0.0) {
                    value = -1;
                }
                else
                if (score > 0.0) {
                    value = 1;
                }
                appId = row.getString(row.fieldIndex(HiveKeywordsTableSchema.COL_APP_ID));
            }
            count++;
        }

        return new GenericRowWithSchema(new Object[] {
                appId, value, count
        }, schema);
    }
}
