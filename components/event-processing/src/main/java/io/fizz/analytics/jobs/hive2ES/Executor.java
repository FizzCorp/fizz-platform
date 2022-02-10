package io.fizz.analytics.jobs.hive2ES;

import io.fizz.analytics.common.Utils;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.common.ConfigService;
import io.fizz.analytics.common.HiveTime;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.hive.HiveTableDataSource;
import io.fizz.common.domain.EventType;
import io.fizz.common.infastructure.model.TextMessageES;
import io.fizz.analytics.jobs.AbstractJobExecutor;
import io.fizz.analytics.common.source.hive.HiveActionTableSchema;
import org.apache.http.HttpHost;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;

public class Executor extends AbstractJobExecutor implements Serializable {
    private static LoggingService.Log logger = LoggingService.getLogger(Executor.class);

    @Override
    public void execute() throws Exception {
        logger.info("Running job to export chat messages from hive to ElasticSearch");

        final HiveTime time = Utils.previousDay();

        final HiveTableDataSource eventsStore = new HiveTableDataSource(spark, new HiveProfileEnrichedEventTableSchema(), dataPath + "/" + HiveProfileEnrichedEventTableSchema.TABLE_NAME);
        final Dataset<Row> eventsDS = eventsStore.scanForLastDaysFrom(0, time);

        Dataset<Row> scoredMessageDS = eventsDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.TEXT_MESSAGE_SENT.value());

        final String esHost = ConfigService.instance().getString("elasticsearch.host");
        final int esPort = ConfigService.instance().getNumber("elasticsearch.port").intValue();
        final String esProtocol = ConfigService.instance().getString("elasticsearch.protocol");
        final String esIndex = ConfigService.instance().getString("elasticsearch.message.index");
        final String esResource = ConfigService.instance().getString("elasticsearch.message.resource");
        final int esBatchSize = ConfigService.instance().getNumber("elasticsearch.batch.size").intValue();

        logger.info("adding messages to " + esProtocol + "://" + esHost + ":" + esPort);

        final Dataset<TextMessageES> esMsgsDS = new ScoredMsgsToMessagesES().transform(scoredMessageDS, null);
        int noOfPartitions = esMsgsDS.rdd().partitions().length;
        System.out.println("Number of partitions: " + noOfPartitions);
        esMsgsDS.foreachPartition((ForeachPartitionFunction<TextMessageES>) iterator -> {
            if (!iterator.hasNext()) {
                return;
            }

            final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(esHost, esPort, esProtocol)));
            int requestsPerPartition = 0;
            while (iterator.hasNext()) {
                final BulkRequest request = getNextBatch(iterator, esIndex, esResource, esBatchSize);
                System.out.println("Bulk Request Size: " + request.numberOfActions());
                client.bulk(request);
                requestsPerPartition += request.numberOfActions();
                Thread.sleep(500);
            }
            System.out.println("Number of actions in a partition: " + requestsPerPartition);
            client.close();
        });
    }

    private BulkRequest getNextBatch(final Iterator<TextMessageES> aIterator, final String aEsIndex,
                                     final String aEsResource, final int aEsBatchSize) {
        final BulkRequest request = new BulkRequest();

        int batchSize = 0;
        while (aIterator.hasNext()) {
            final TextMessageES message = aIterator.next();
            request.add(
                    new IndexRequest(aEsIndex, aEsResource, message.getId()).source(new JSONObject(message).toString(), XContentType.JSON)
            );
            batchSize++;
            if (batchSize >= aEsBatchSize) {
                break;
            }
        }
        return request;
    }

    public static void main (String[] args) throws Exception {
        System.setProperty("es.set.netty.runtime.available.processors", "false");

        final Executor instance = new Executor();

        instance.init();
        instance.execute();

        logger.info("=== exiting application");
    }

    private static String extractActionId (final Row row) {
        final String extensions = row.getString(row.fieldIndex(HiveActionTableSchema.COL_EXTENSIONS));
        return new JSONObject(extensions).getString("action_id");
    }

    private static String extractContent (final Row row) {
        final String fields = row.getString(row.fieldIndex(HiveActionTableSchema.COL_FIELDS));
        return new JSONObject(fields).getString("msg");
    }
}
