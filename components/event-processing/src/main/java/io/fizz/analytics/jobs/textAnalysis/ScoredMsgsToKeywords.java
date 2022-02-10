package io.fizz.analytics.jobs.textAnalysis;

import com.google.common.collect.Lists;
import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveKeywordsTableSchema;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.StructType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class ScoredMsgsToKeywords implements AbstractTransformer<Row,Row> {
    @Override
    public Dataset<Row> transform(Dataset<Row> aSourceDS, HiveTime time) {
        if (Objects.isNull(aSourceDS)) {
            throw new IllegalArgumentException("invalid source data set");
        }

        final HiveKeywordsTableSchema keywordsSchema = new HiveKeywordsTableSchema();
        final StructType schema = keywordsSchema.schema();

        return aSourceDS
        .flatMap((FlatMapFunction<Row, Row>) row -> {
            final int fieldIdx = row.fieldIndex(SentimentAnalysis.COL_OUTPUT);
            final JSONObject sentiment = Objects.isNull(row.getString(fieldIdx)) ? null : new JSONObject(row.getString(fieldIdx));
            final List<Row> rows = Lists.newArrayList();

            if (!Objects.isNull(sentiment)) {
                final JSONArray keywords = sentiment.getJSONArray(SentimentAnalysis.KEY_KEYWORDS);
                for (int ki = 0; ki < keywords.length(); ki++) {
                    final JSONObject obj = keywords.getJSONObject(ki);
                    rows.add(new GenericRowWithSchema(new Object[]{
                            obj.getString(SentimentAnalysis.KEY_TEXT),
                            obj.getDouble(SentimentAnalysis.KEY_SCORE),
                            0.0, 0.0, 0.0, 0.0, 0.0,
                            HiveProfileEnrichedEventTableSchema.appId(row),
                            HiveProfileEnrichedEventTableSchema.year(row),
                            HiveProfileEnrichedEventTableSchema.month(row),
                            HiveProfileEnrichedEventTableSchema.day(row)
                    }, schema));
                }
            }
            return rows.iterator();
        }, RowEncoder.apply(keywordsSchema.schema()));
    }
}
