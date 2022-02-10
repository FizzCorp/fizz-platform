package io.fizz.gdpr.job.anonymize.hbase;

import io.fizz.common.LoggingService;
import io.fizz.gdpr.job.ConfigService;
import io.fizz.gdpr.job.anonymize.hbase.model.GDPRUser;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChannelMessageMapper extends TableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {
    private static final LoggingService.Log logger = new LoggingService.Log(ChannelMessageMapper.class);

    private ConfigService configService;
    private final List<GDPRUser> gdprUsers = new ArrayList<>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);

        configService = new ConfigService(context.getConfiguration());

        final JSONArray gdprUsersJson = new JSONArray(context.getConfiguration().get("gdpr_users"));
        for (int i = 0; i < gdprUsersJson.length(); i++) {
            final JSONObject gdprUser = gdprUsersJson.getJSONObject(i);
            gdprUsers.add(new GDPRUser(
                    gdprUser.getString("appId"),
                    gdprUser.getString("userId"),
                    gdprUser.getBoolean("clearUserData")
            ));
        }
    }

    @Override
    protected void map(ImmutableBytesWritable rowkey, Result columns, Context context) throws IOException, InterruptedException {
        final String value = Bytes.toString(columns.getValue(configService.get("chat.hbase.message.cf").getBytes(), "data".getBytes()));
        final JSONObject data = new JSONObject(value);

        if (data.has("appId") && data.has("from")) {
            String appId = data.getString("appId");
            String userId = data.getJSONObject("from").getString("value");

            final GDPRUser gdprUser = new GDPRUser(appId, userId, false);

            int index = gdprUsers.indexOf(gdprUser);
            if (index != -1) {
                final GDPRUser requestGDPRUser = gdprUsers.get(index);

                if (requestGDPRUser.isClearUserData()) {
                    data.remove("data");
                }

                byte[] outBuffer = data.toString().getBytes();
                context.write(rowkey, new ImmutableBytesWritable(outBuffer, 0, outBuffer.length));
            }
        }
    }

}
