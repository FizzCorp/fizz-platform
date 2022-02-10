package io.fizz.gdpr.job.anonymize.hbase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.fizz.gdpr.job.AbstractExecutor;
import io.fizz.gdpr.job.anonymize.hbase.model.GDPRUser;
import io.fizz.gdpr.model.GDPRRequest;
import io.fizz.gdpr.repository.GDPRRequestRepository;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;

import java.util.ArrayList;
import java.util.List;

public class Executor extends AbstractExecutor {
    public static void main(String[] args) {
        try {
            System.exit(ToolRunner.run(HBaseConfiguration.create(), new Executor(), args));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int run(String[] args) {
        super.run(args);

        try {
            List<GDPRUser> gdprUsers = getGDPRUsers();

            if (gdprUsers.size() == 0) {
                return 0;
            }

            Gson gson = new GsonBuilder().create();
            String gdprUsersJson = gson.toJson(
                    gdprUsers,
                    new TypeToken<List<GDPRUser>>() {}.getType());

            getConf().set("gdpr_users", gdprUsersJson);

            Job job = Job.getInstance(getConf(), "gdpr");
            job.setJarByClass(Executor.class);

            Scan scan = new Scan();
            scan.addFamily(Bytes.toBytes(configService.get("chat.hbase.message.cf")));

            TableName tableName = TableName.valueOf(
                    configService.get("chat.hbase.namespace"),
                    configService.get("chat.hbase.message.table")
            );

            TableMapReduceUtil.initTableMapperJob(
                    tableName,
                    scan,
                    ChannelMessageMapper.class,
                    ImmutableBytesWritable.class,
                    ImmutableBytesWritable.class,
                    job
            );

            TableMapReduceUtil.initTableReducerJob(
                    tableName.getNameAsString(),
                    ChannelMessageReducer.class,
                    job);

            return job.waitForCompletion(true) ? 0 : 1;
        } catch (Exception e) {
            System.out.println(e);
            return 1;
        }
    }

    private List<GDPRUser> getGDPRUsers() {
        final List<GDPRUser> users = new ArrayList<>();

        final List<GDPRRequest> requests = GDPRRequestRepository.fetch();

        for (GDPRRequest request: requests) {
            users.add(new GDPRUser(request.getAppId(), request.getUserId(), request.isClearMessageData()));
        }

        return users;
    }
}
