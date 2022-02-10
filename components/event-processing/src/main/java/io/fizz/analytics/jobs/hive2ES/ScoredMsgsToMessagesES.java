package io.fizz.analytics.jobs.hive2ES;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.common.infastructure.model.TextMessageES;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScoredMsgsToMessagesES implements AbstractTransformer<Row, TextMessageES> {
    @Override
    public Dataset<TextMessageES> transform(Dataset<Row> sourceDS, HiveTime time) {
        if (Objects.isNull(sourceDS)) {
            throw new IllegalArgumentException("invalid source data set");
        }

        return sourceDS
                .map((MapFunction<Row,TextMessageES>) row -> {
                    final JSONObject fields = new JSONObject(HiveProfileEnrichedEventTableSchema.fields(row));

                    final String content = fields.getString(HiveEventFields.TEXT_MESSAGE_CONTENT.value());
                    final String nick = fields.has(HiveEventFields.TEXT_MESSAGE_NICK.value())
                            ? fields.getString(HiveEventFields.TEXT_MESSAGE_NICK.value()) : null;
                    final String channel = fields.getString(HiveEventFields.TEXT_MESSAGE_CHANNEL.value());
                    final double sentimentScore = fields.has(HiveEventFields.SENTIMENT_SCORE.value()) ?
                            fields.getDouble(HiveEventFields.SENTIMENT_SCORE.value()) : 0.0;

                    return new TextMessageES(
                            HiveProfileEnrichedEventTableSchema.id(row),
                            HiveProfileEnrichedEventTableSchema.appId(row),
                            HiveProfileEnrichedEventTableSchema.countryCode(row),
                            HiveProfileEnrichedEventTableSchema.userId(row),
                            nick,
                            content,
                            channel,
                            HiveProfileEnrichedEventTableSchema.platform(row),
                            HiveProfileEnrichedEventTableSchema.build(row),
                            HiveProfileEnrichedEventTableSchema.custom01(row),
                            HiveProfileEnrichedEventTableSchema.custom02(row),
                            HiveProfileEnrichedEventTableSchema.custom03(row),
                            HiveProfileEnrichedEventTableSchema.occurredOn(row),
                            sentimentScore,
                            HiveProfileEnrichedEventTableSchema.age(row),
                            HiveProfileEnrichedEventTableSchema.spend(row),
                            parseKeywords(fields)
                    );
                }, Encoders.bean(TextMessageES.class));
    }

    private static List<String> parseKeywords(final JSONObject aFields) {
        if (Objects.isNull(aFields) || !aFields.has(HiveEventFields.TEXT_MESSAGE_KEYWORDS.value())) {
            return null;
        }

        final List<String> outKeywords = new ArrayList<>();
        final JSONArray keywords = aFields.getJSONArray(HiveEventFields.TEXT_MESSAGE_KEYWORDS.value());
        for (int ki = 0; ki < keywords.length(); ki++) {
            outKeywords.add(keywords.getString(ki));
        }

        return outKeywords;
    }
}
