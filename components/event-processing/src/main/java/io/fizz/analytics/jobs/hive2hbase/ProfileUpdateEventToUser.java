package io.fizz.analytics.jobs.hive2hbase;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.adapter.UserRowAdapter;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.domain.User;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.json.JSONObject;

import java.util.Objects;

public class ProfileUpdateEventToUser implements AbstractTransformer<Row, Row> {
    @Override
    public Dataset<Row> transform(Dataset<Row> sourceDS, HiveTime time) {
        if (Objects.isNull(sourceDS)) {
            throw new IllegalArgumentException("invalid source data set");
        }

        return sourceDS
                .map((MapFunction<Row, Row>) row -> {
                    final JSONObject fields = new JSONObject(HiveProfileEnrichedEventTableSchema.fields(row));
                    final long firstTimeActive = fields.getLong(HiveEventFields.PROFILE_OLDEST_ACTIVITY_TS.value());
                    final long lastTimeActive = fields.getLong(HiveEventFields.PROFILE_LATEST_ACTIVITY_TS.value());
                    final long amountSpentInCents  = fields.getLong(HiveEventFields.PROFILE_SPENT.value());
                    final double sentimentSum = fields.getDouble(HiveEventFields.PROFILE_SENTIMENT_SUM.value());
                    final long messagesCounts = fields.getLong(HiveEventFields.PROFILE_MESSAGE_COUNT.value());

                    User user = new User.Builder()
                            .setId(HiveProfileEnrichedEventTableSchema.userId(row))
                            .setAppId(HiveProfileEnrichedEventTableSchema.appId(row))
                            .setFirstTimeActiveTS(firstTimeActive)
                            .setLastTimeActiveTS(lastTimeActive)
                            .setAmountSpentInCents(amountSpentInCents)
                            .setLocation(HiveProfileEnrichedEventTableSchema.countryCode(row))
                            .setBuild(HiveProfileEnrichedEventTableSchema.build(row))
                            .setPlatform(HiveProfileEnrichedEventTableSchema.platform(row))
                            .setCustom01(HiveProfileEnrichedEventTableSchema.custom01(row))
                            .setCustom02(HiveProfileEnrichedEventTableSchema.custom02(row))
                            .setCustom03(HiveProfileEnrichedEventTableSchema.custom03(row))
                            .setSentimentSum(sentimentSum, messagesCounts)
                            .get();

                    return UserRowAdapter.toGenericRow(user);
                }, RowEncoder.apply(UserRowAdapter.schema));
    }
}
