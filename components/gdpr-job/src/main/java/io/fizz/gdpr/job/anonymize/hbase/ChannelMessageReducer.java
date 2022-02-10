package io.fizz.gdpr.job.anonymize.hbase;

import io.fizz.common.LoggingService;
import io.fizz.gdpr.job.ConfigService;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.json.JSONObject;

import java.io.IOException;

public class ChannelMessageReducer extends TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
    private static final LoggingService.Log logger = new LoggingService.Log(ChannelMessageReducer.class);

    private ConfigService configService;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        configService = new ConfigService(context.getConfiguration());
    }

    @Override
    protected void reduce(ImmutableBytesWritable key, Iterable<ImmutableBytesWritable> values, Context context) throws IOException, InterruptedException {
        for(ImmutableBytesWritable x: values) {
            final String value = Bytes.toString(x.get());
            final JSONObject data = new JSONObject(value);

            data.getJSONObject("from").put("value", configService.get("gdpr.anonymize.id"));
            data.put("nick", configService.get("gdpr.anonymize.nick"));

            final Put put = new Put(key.get());
            put.addColumn(
                    configService.get("chat.hbase.message.cf").getBytes(),
                    "data".getBytes(),
                    data.toString().getBytes()
            );

            context.write(key, put);
        }
    }
}
