package io.fizz.analytics.jobs.metricsRollup.rollup;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.hive.AbstractTableDataSource;
import io.fizz.analytics.common.sink.AbstractSink;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.jobs.metricsRollup.aggregator.translation.TranslationCharCountAggregator;
import io.fizz.analytics.jobs.metricsRollup.aggregator.translation.TranslationCountAggregator;
import io.fizz.analytics.jobs.metricsRollup.projectors.AggregationProjector;
import io.fizz.analytics.jobs.metricsRollup.projectors.Grain;
import io.fizz.analytics.jobs.metricsRollup.aggregator.*;
import io.fizz.common.LoggingService;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Objects;

public class MetricsRollupJobEvent implements AbstractMetricsRollupJob {
    private static final LoggingService.Log logger = LoggingService.getLogger(MetricsRollupJobEvent.class);

    private final SparkSession spark;
    private final String[] segments;
    private final AggregationProjector projector;

    public MetricsRollupJobEvent(final SparkSession aSpark,
                                 final AbstractTableDataSource aSource, final String aUniqueColumn,
                                 final AbstractSink<Row> aSink, final HiveTime aTime,
                                 final String[] aSegments) {
        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("invalid spark session");
        }
        if (Objects.isNull(aSink)) {
            throw new IllegalArgumentException("invalid data sink");
        }
        if (Objects.isNull(aSegments)) {
            throw new IllegalArgumentException("invalid segments");
        }
        spark = aSpark;
        segments = aSegments;
        projector = new AggregationProjector(aSource, aUniqueColumn, aTime);
        projector.addSink(aSink);
    }

    public void runProjection() {
        logger.info("Running aggregations on events");

        for (String segment: segments) {
            runSessionProjection(segment);
            runTranslationProjection(segment);
            runActionProjection(segment);
            runNewUsersProjection(segment);
            runSentimentProjection(segment);
            runRevenueProjection(segment);
        }

        projector.cleanup();
    }

    private void runSessionProjection(final String aSegment) {
        logger.info("Running aggregations on sessions events");

        //SessionAggregator
        projector
        .project(Grain.DAILY, new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, aSegment));

        projector
        .project(Grain.MONTHLY, new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_MONTHLY, aSegment));

        //SessionAttributeAggregator
        projector
        .project(Grain.DAILY, new SessionAttributeAggregator(spark, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_DAILY, aSegment));

        projector
        .project(Grain.MONTHLY, new SessionAttributeAggregator(spark, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_MONTHLY, aSegment));

        //ActiveUserAggregator
        projector
        .project(Grain.DAILY, new ActiveUserAggregator(HiveDefines.MetricId.ACTIVE_USERS_DAILY, aSegment));

        projector
        .project(Grain.MONTHLY, new ActiveUserAggregator(HiveDefines.MetricId.ACTIVE_USERS_MONTHLY, aSegment));

        //PayingUsersSessionAggregator
        projector
        .project(Grain.DAILY, new PayingUsersSessionAggregator(spark, HiveDefines.MetricId.ACTIVE_PAYING_USER_USERS_DAILY, aSegment));
    }

    private void runTranslationProjection(final String aSegment) {
        logger.info("Running aggregations on translation events");

        projector
        .project(Grain.DAILY, new TranslationCountAggregator(spark, HiveDefines.MetricId.TRANSLATION_COUNT_DAILY, aSegment));

        projector
        .project(Grain.MONTHLY, new TranslationCountAggregator(spark, HiveDefines.MetricId.TRANSLATION_COUNT_MONTHLY, aSegment));

        projector
        .project(Grain.DAILY, new TranslationCharCountAggregator(spark, HiveDefines.MetricId.CHARS_TRANSLATED_DAILY, aSegment));

        projector
        .project(Grain.MONTHLY, new TranslationCharCountAggregator(spark, HiveDefines.MetricId.CHARS_TRANSLATED_MONTHLY, aSegment));
    }

    private void runActionProjection(final String aSegment) {
        logger.info("Running aggregations on action events");

        projector
        .project(Grain.DAILY, new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, aSegment));

        projector
        .project(Grain.MONTHLY, new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_MONTHLY, aSegment));
    }

    private void runNewUsersProjection(final String aSegment) {
        logger.info("Running aggregations on new users events");

        projector
        .project(Grain.DAILY, new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, aSegment));

        projector
        .project(Grain.MONTHLY, new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_MONTHLY, aSegment));
    }

    private void runSentimentProjection(final String aSegment) {
        logger.info("Running aggregations on sentiment events");

        projector
        .project(Grain.DAILY, new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, aSegment));

        projector
        .project(Grain.MONTHLY, new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_MONTHLY, aSegment));
    }

    private void runRevenueProjection(final String aSegment) {
        logger.info("Running aggregations on sentiment events");

        projector
        .project(Grain.DAILY, new RevenueAggregator(spark, HiveDefines.MetricId.REVENUE_DAILY, aSegment));

        projector
        .project(Grain.MONTHLY, new RevenueAggregator(spark, HiveDefines.MetricId.REVENUE_MONTHLY, aSegment));
    }
}
