package io.fizz.analytics.jobs.metricsRollup.transformer;

import com.google.common.collect.Lists;
import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveTranslationTableSchema;
import io.fizz.common.LoggingService;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class TranslationEventTransformer implements AbstractTransformer<Row,Row>, Serializable {
    @Override
    public Dataset<Row> transform(final Dataset<Row> sourceDS, final HiveTime time) {
        if (Objects.isNull(sourceDS)) {
            throw new IllegalArgumentException("invalid source data set specified.");
        }

        return sourceDS
        .flatMap((FlatMapFunction<Row, Row>) row -> {
            final List<Row> rows = Lists.newArrayList();

            String id = HiveProfileEnrichedEventTableSchema.id(row);
            String appId = HiveProfileEnrichedEventTableSchema.appId(row);
            String platform = HiveProfileEnrichedEventTableSchema.platform(row);
            String build = HiveProfileEnrichedEventTableSchema.build(row);
            String custom01 = HiveProfileEnrichedEventTableSchema.custom01(row);
            String custom02 = HiveProfileEnrichedEventTableSchema.custom02(row);
            String custom03 = HiveProfileEnrichedEventTableSchema.custom03(row);
            String age = HiveProfileEnrichedEventTableSchema.age(row);
            String spend = HiveProfileEnrichedEventTableSchema.spend(row);
            String year = HiveProfileEnrichedEventTableSchema.year(row);
            String month = HiveProfileEnrichedEventTableSchema.month(row);
            String day = HiveProfileEnrichedEventTableSchema.day(row);

            JSONObject fields = new JSONObject(HiveProfileEnrichedEventTableSchema.fields(row));
            String src = fields.getString(HiveEventFields.TRANS_LANG_FROM.value());
            JSONArray destArray = new JSONArray(fields.getString(HiveEventFields.TRANS_LANG_TO.value()));
            int length = fields.getInt(HiveEventFields.TRANS_TEXT_LEN.value());

            for (int i = destArray.length() - 1; i >= 0; i--) {
                String dest = destArray.getString(i);
                rows.add(new HiveTranslationTableSchema.RowBuilder()
                        .setId(id)
                        .setAppId(appId)
                        .setPlatform(platform)
                        .setBuild(build)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setAge(age)
                        .setSpend(spend)
                        .setLength(length)
                        .setSrc(src)
                        .setDest(dest)
                        .setYear(year)
                        .setMonth(month)
                        .setDay(day)
                        .get()
                );
            }

            return rows.iterator();
        }, RowEncoder.apply(new HiveTranslationTableSchema().schema()));
    }
}
