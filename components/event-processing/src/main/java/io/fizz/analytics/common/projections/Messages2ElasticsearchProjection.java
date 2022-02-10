package io.fizz.analytics.common.projections;

import io.fizz.common.ConfigService;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.source.hive.HiveActionTableSchema;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.Row;
import org.json.JSONObject;

import java.net.URL;
import java.util.Iterator;

public class Messages2ElasticsearchProjection {
    private static final LoggingService.Log logger = LoggingService.getLogger(Messages2ElasticsearchProjection.class);
    private static final String INDEX_NAME = "messages";
    private static final String INDEX_TYPE = "doc";
    private static final String ACTION_META_TEMPLATE = "{\"index\":{\"_index\":\"%s\",\"_type\":\"%s\",\"_id\":\"%s\"}}%n";
    private static final String FIELD_ACTION_ID = "action_id";
    private static final String FIELD_ACTION_CONTENT = "msg";
    private static final String KEY_ACTION_ID = "actionId";
    private static final String KEY_APP_ID = "appId";
    private static final String KEY_ROOM_ID = "roomId";
    private static final String KEY_SENDER_ID = "senderId";
    private static final String KEY_SENDER_NICK = "senderNick";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_CONTENT = "content";

    public static void project (final JavaRDD<Row> messagesDD) throws Exception {
        final String esProtocol = ConfigService.instance().getString("elasticsearch.protocol");
        final String esHost = ConfigService.instance().getString("elasticsearch.host");
        final int esPort = ConfigService.instance().getNumber("elasticsearch.port").intValue();
        final URL url = new URL(esProtocol, esHost, esPort, "");
        final String esEndpoint = url.toURI() + "/_bulk";

        messagesDD
            .foreachPartition((VoidFunction<Iterator<Row>>) iterator -> {
                final StringBuilder builder = new StringBuilder();

                while (iterator.hasNext()) {
                    final Row row = iterator.next();
                    final String extension = row.getString(row.fieldIndex(HiveActionTableSchema.COL_EXTENSIONS));
                    final String fields = row.getString(row.fieldIndex(HiveActionTableSchema.COL_FIELDS));
                    final JSONObject extensionJSON = new JSONObject(extension);
                    final JSONObject fieldsJSON = new JSONObject(fields);
                    final String actionId = extensionJSON.getString(FIELD_ACTION_ID);
                    final String content = fieldsJSON.getString(FIELD_ACTION_CONTENT);
                    final String ACTION_META = String.format(ACTION_META_TEMPLATE, INDEX_NAME, INDEX_TYPE, actionId);

                    builder.append(ACTION_META);
                    final JSONObject requestBody = new JSONObject();
                    requestBody.put(KEY_ACTION_ID, actionId);
                    requestBody.put(KEY_APP_ID, row.getString(row.fieldIndex(HiveActionTableSchema.COL_APP_ID)));
                    requestBody.put(KEY_ROOM_ID, row.getString(row.fieldIndex(HiveActionTableSchema.COL_ROOM_ID)));
                    requestBody.put(KEY_SENDER_ID, row.getString(row.fieldIndex(HiveActionTableSchema.COL_SENDER_ID)));
                    requestBody.put(KEY_SENDER_NICK, row.getString(row.fieldIndex(HiveActionTableSchema.COL_NICK)));
                    requestBody.put(KEY_TIMESTAMP, row.getString(row.fieldIndex(HiveActionTableSchema.COL_TIMESTAMP)));
                    requestBody.put(KEY_CONTENT, content);
                    builder.append(requestBody.toString());
                    builder.append("\n");
                }

                final String requestBuffer = builder.toString();
                if (requestBuffer.length() > 0) {
                    HttpClient client = HttpClientBuilder.create().build();
                    @SuppressWarnings("deprecation") StringEntity requestEntity = new StringEntity(
                            requestBuffer,
                            "application/json",
                            "UTF-8");
                    HttpPost post = new HttpPost(esEndpoint);
                    post.setEntity(requestEntity);

                    HttpResponse response = client.execute(post);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        logger.error(response.toString());
                    }
                }
            });
    }
}
