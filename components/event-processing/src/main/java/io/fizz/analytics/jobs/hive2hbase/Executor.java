package io.fizz.analytics.jobs.hive2hbase;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.Utils;
import io.fizz.analytics.common.adapter.UserRowAdapter;
import io.fizz.analytics.common.hive.HiveTableDataSource;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.analytics.jobs.AbstractJobExecutor;
import io.fizz.analytics.common.repository.HBaseUserRepository;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.EventType;
import io.fizz.analytics.domain.User;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.List;

public class Executor extends AbstractJobExecutor {
    private static final LoggingService.Log logger = LoggingService.getLogger(Executor.class);

    public static void main (String[] args) {
        final Executor instance = new Executor();

        instance.init();
        try {
            instance.execute();
        }
        catch (Exception ex) {
            logger.fatal("Hbase User save job failed with exception: " + ex.getMessage());
        }

        logger.info("=== exiting application");
    }

    @Override
    public void execute() throws Exception {
        logger.info("Saving updated profile to hbase.");

        final HiveTime time = Utils.previousDay();

        final HiveTableDataSource eventsStore = new HiveTableDataSource(spark, new HiveProfileEnrichedEventTableSchema(), dataPath + "/" + HiveProfileEnrichedEventTableSchema.TABLE_NAME);
        final Dataset<Row> eventsDS = eventsStore.scanForLastDaysFrom(0, time);

        final Dataset<Row> profileUpdateDS = eventsDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.PROFILE_UPDATED.value());

        final Dataset<Row> userDS = new ProfileUpdateEventToUser().transform(profileUpdateDS, null);

        final HBaseUserRepository userRepo = new HBaseUserRepository();

        userDS.foreachPartition((ForeachPartitionFunction<Row>) iterator -> {
            if (!iterator.hasNext()) {
                return;
            }
            List<User> users = new ArrayList<>();
            while (iterator.hasNext()) {
                final Row row = iterator.next();
                users.add(UserRowAdapter.toUser(row));
            }
            logger.info("saving profile in hbase");
            userRepo.save(users);
        });

        logger.info("Shutdown hbase saving job");
    }
}

