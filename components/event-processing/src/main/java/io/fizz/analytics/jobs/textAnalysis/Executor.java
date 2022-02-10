package io.fizz.analytics.jobs.textAnalysis;

import io.fizz.analytics.common.Utils;
import io.fizz.analytics.common.nlp.AWSComprehendService;
import io.fizz.analytics.common.nlp.AbstractTextAnalysisService;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveSentimentEventTableSchema;
import io.fizz.common.ConfigService;
import io.fizz.analytics.common.HiveTime;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.hive.HiveTableDataSource;
import io.fizz.analytics.common.nlp.WatsonNLUService;
import io.fizz.analytics.common.sink.HiveTableSink;
import io.fizz.analytics.common.source.hive.HiveKeywordsTableSchema;
import io.fizz.common.domain.*;
import io.fizz.analytics.jobs.AbstractJobExecutor;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.*;

import java.util.Objects;

public class Executor extends AbstractJobExecutor {
    private static final LoggingService.Log logger = LoggingService.getLogger(Executor.class);
    private static final String TEXT_ANALYSIS_CLIENT = ConfigService.instance().getString("nlu.text.analysis.client");

    public static void main (String[] args) {
        final Executor instance = new Executor();

        instance.init();
        instance.execute();

        logger.info("=== exiting application");
    }

    @Override
    public void execute() {
        final HiveTime time = Utils.previousDay();

        AbstractTextAnalysisService textAnalysisService = null;
        if (TEXT_ANALYSIS_CLIENT.equals("ibm")) {
            final String NLU_API_KEY = ConfigService.instance().getString("nlu.api.key");
            textAnalysisService = new WatsonNLUService(NLU_API_KEY);
        } else if (TEXT_ANALYSIS_CLIENT.equals("aws")) {
            final String AWS_COMPREHEND_REGION = ConfigService.instance().getString("aws.nlu.comprehend.region");
            textAnalysisService = new AWSComprehendService(AWS_COMPREHEND_REGION);
        }

        if (Objects.isNull(textAnalysisService)) {
            logger.warn("No Client available for text analysis [aws,ibm]: Unknown Client: " + TEXT_ANALYSIS_CLIENT);
            return;
        }

        // read events
        final HiveTableDataSource eventsStore = new HiveTableDataSource(
                spark,
                new HiveRawEventTableSchema(),
                dataPath + "/" + HiveRawEventTableSchema.TABLE_NAME
        );
        final Dataset<Row> eventsDS = eventsStore.scanForLastDaysFrom(0, time);

        // filter message events
        final Dataset<Row> messagesDS = eventsDS.filter(
            (FilterFunction<Row>)row -> HiveRawEventTableSchema.eventType(row) == EventType.TEXT_MESSAGE_SENT.value()
        );

        // filter non-message events
        final Dataset<Row> nonMessagesDS = eventsDS.filter(
            (FilterFunction<Row>)row -> HiveRawEventTableSchema.eventType(row) != EventType.TEXT_MESSAGE_SENT.value()
        );

        // perform analysis on messages
        final Dataset<Row> sentimentAnalyzedDS = new SentimentAnalysis(spark, textAnalysisService).transform(messagesDS, null).cache();

        // store results of analysis
        final Dataset<Row> sentimentEventDS = new SentimentMessageToSentimentEvent().transform(sentimentAnalyzedDS, null);
        final HiveTableSink sentimentEventSink = new HiveTableSink(
                spark,
                new HiveSentimentEventTableSchema(),
                outputPath + "/" + HiveSentimentEventTableSchema.TABLE_NAME,
                SaveMode.Overwrite
        );
        sentimentEventSink.put(sentimentEventDS.union(nonMessagesDS));

        // store keywords
        final Dataset<Row> keywordsDS = new ScoredMsgsToKeywords().transform(sentimentAnalyzedDS, null);
        final HiveTableSink keywordsSink = new HiveTableSink(
                spark,
                new HiveKeywordsTableSchema(),
                outputPath + "/" + HiveKeywordsTableSchema.TABLE_NAME,
                SaveMode.Overwrite
        );
        keywordsSink.put(keywordsDS);

        sentimentAnalyzedDS.unpersist();
    }
}
