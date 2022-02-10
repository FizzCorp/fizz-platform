package io.fizz.analytics.common.repository;

import io.fizz.analytics.domain.User;
import io.fizz.common.ConfigService;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class HBaseUserRepository implements AbstractUserRepository, Serializable {
    private static final LoggingService.Log logger = LoggingService.getLogger(HBaseUserRepository.class);
    private static Connection connection;

    private static TableName TABLE_NAME = TableName.valueOf(Bytes.toBytes("analytics"), Bytes.toBytes("tbl_user"));

    private static final byte[] CF_PROFILE = Bytes.toBytes("p");
    private static final byte[] COL_ID = Bytes.toBytes("id");
    private static final byte[] COL_APP_ID = Bytes.toBytes("aid");
    private static final byte[] COL_FIRST_SESSION_TIME = Bytes.toBytes("fst");
    private static final byte[] COL_LAST_SESSION_TIME = Bytes.toBytes("lst");
    private static final byte[] COL_AMOUNT_SPENT = Bytes.toBytes("spent");
    private static final byte[] COL_LOCATION = Bytes.toBytes("loc");
    private static final byte[] COL_BUILD = Bytes.toBytes("build");
    private static final byte[] COL_PLATFORM = Bytes.toBytes("pl");
    private static final byte[] COL_CUSTOM01 = Bytes.toBytes("cus01");
    private static final byte[] COL_CUSTOM02 = Bytes.toBytes("cus02");
    private static final byte[] COL_CUSTOM03 = Bytes.toBytes("cus03");
    private static final byte[] COL_SENTIMENT_SUM = Bytes.toBytes("ss");
    private static final byte[] COL_MESSAGE_COUNT = Bytes.toBytes("mc");

    static {
        try {
            final String KEY_ZOOKEEPER_QUORUM = "hbase.zookeeper.quorum";
            final String KEY_ZOOKEEPER_CLIENTPORT = "hbase.zookeeper.property.clientPort";
            final String KEY_HBASE_DISTRIBUTED_CLUSTERED = "hbase.cluster.distributed";
            final String KEY_ZOOKEEPER_ZNODE_PARENT = "zookeeper.znode.parent";

            final Configuration conf = HBaseConfiguration.create();
            conf.set(KEY_ZOOKEEPER_QUORUM, ConfigService.instance().getString(KEY_ZOOKEEPER_QUORUM));
            conf.set(KEY_ZOOKEEPER_CLIENTPORT, ConfigService.instance().getString(KEY_ZOOKEEPER_CLIENTPORT));
            conf.set(KEY_HBASE_DISTRIBUTED_CLUSTERED, ConfigService.instance().getString(KEY_HBASE_DISTRIBUTED_CLUSTERED));
            conf.set(KEY_ZOOKEEPER_ZNODE_PARENT, ConfigService.instance().getString(KEY_ZOOKEEPER_ZNODE_PARENT));
            connection = ConnectionFactory.createConnection(conf);
        }
        catch (IOException ex) {
            logger.fatal("Failed to establish connection with HBase: " + ex.getMessage());
            connection = null;
        }
    }

    public HBaseUserRepository() {
        if (Objects.isNull(connection)) {
            throw new IllegalStateException("hbase connection not found");
        }
    }

    @Override
    public User fetchWithId(final ApplicationId aAppId, final UserId aUserId) throws IOException {
        if (Objects.isNull(aAppId)) {
            throw new IllegalArgumentException("invalid application id specified.");
        }
        if (Objects.isNull(aUserId)) {
            throw new IllegalArgumentException("invalid user id specified.");
        }

        try(final Table table = connection.getTable(TABLE_NAME)) {
            final Get get = mkGet(aAppId, aUserId);
            final Result result = table.get(get);
            if (result.isEmpty()) {
                return null;
            }

            final Map<byte[],byte[]> info = result.getFamilyMap(CF_PROFILE);
            final long firstTimeActive = info.containsKey(COL_FIRST_SESSION_TIME) ? Bytes.toLong(info.get(COL_FIRST_SESSION_TIME)) : new Date().getTime();
            final long lastTimeActive = info.containsKey(COL_LAST_SESSION_TIME) ? Bytes.toLong(info.get(COL_LAST_SESSION_TIME)) : new Date().getTime();
            final long amountSpentInCents = info.containsKey(COL_AMOUNT_SPENT) ? Bytes.toLong(info.get(COL_AMOUNT_SPENT)) : 0;

            final String location = info.containsKey(COL_LOCATION) ? Bytes.toString(info.get(COL_LOCATION)) : null;
            final String build = info.containsKey(COL_BUILD) ? Bytes.toString(info.get(COL_BUILD)) : null;
            final String platform = info.containsKey(COL_PLATFORM) ? Bytes.toString(info.get(COL_PLATFORM)) : null;

            final String custom01 = info.containsKey(COL_CUSTOM01) ? Bytes.toString(info.get(COL_CUSTOM01)) : null;
            final String custom02 = info.containsKey(COL_CUSTOM02) ? Bytes.toString(info.get(COL_CUSTOM02)) : null;
            final String custom03 = info.containsKey(COL_CUSTOM03) ? Bytes.toString(info.get(COL_CUSTOM03)) : null;

            final double sentimentSum = info.containsKey(COL_SENTIMENT_SUM) ? Bytes.toDouble(info.get(COL_SENTIMENT_SUM)) : 0;
            final long messageCount = info.containsKey(COL_MESSAGE_COUNT) ? Bytes.toLong(info.get(COL_MESSAGE_COUNT)) : 0;

            try {
                return new User.Builder()
                        .setId(aUserId)
                        .setAppId(aAppId)
                        .setFirstTimeActiveTS(firstTimeActive)
                        .setLastTimeActiveTS(lastTimeActive)
                        .setAmountSpentInCents(amountSpentInCents)
                        .setLocation(location)
                        .setBuild(build)
                        .setPlatform(platform)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setSentimentSum(sentimentSum, messageCount)
                        .get();
            } catch (DomainErrorException e) {
                return null;
            }
        }
    }

    @Override
    public void save(final User aUser) throws IOException {
        if (Objects.isNull(aUser)) {
            throw new IllegalArgumentException("invalid user specified for persistence.");
        }

        try (final Table table = connection.getTable(TABLE_NAME)) {
            final Put put = mkPut(aUser);
            table.put(put);
        }
    }

    @Override
    public void save(List<User> aUsers) throws IOException {
        if (Objects.isNull(aUsers)) {
            throw new IllegalArgumentException("invalid user specified for persistence.");
        }

        if (aUsers.size() == 0) {
            throw new IllegalArgumentException("invalid user array specified for persistence.");
        }

        try (final Table table = connection.getTable(TABLE_NAME)) {
            final List<Put> put = mkPut(aUsers);
            table.put(put);
        }
    }

    private byte[] mkRowKey(final ApplicationId aAppId, final UserId aUserId) {
        return mkRowKey(aAppId.value(), aUserId.value());
    }

    private byte[] mkRowKey(final String aAppId, final String aUserId) {
        return Utils.md5Sum(aAppId + aUserId);
    }

    private Get mkGet(final ApplicationId aAppId, final UserId aUserId) {
        final byte[] key = mkRowKey(aAppId, aUserId);
        final Get get = new Get(key);
        get.addFamily(CF_PROFILE);
        return get;
    }

    private Put mkPut(final User aUser) {
        final byte[] key = mkRowKey(aUser.appId(), aUser.id());
        final Put put = new Put(key);

        put.addColumn(CF_PROFILE, COL_ID, Bytes.toBytes(aUser.id().value()));
        put.addColumn(CF_PROFILE, COL_APP_ID, Bytes.toBytes(aUser.appId().value()));

        put.addColumn(CF_PROFILE, COL_FIRST_SESSION_TIME, Bytes.toBytes(aUser.firstTimeActiveTS()));
        put.addColumn(CF_PROFILE, COL_LAST_SESSION_TIME, Bytes.toBytes(aUser.lastTimeActiveTS()));
        put.addColumn(CF_PROFILE, COL_AMOUNT_SPENT, Bytes.toBytes(aUser.amountSpentInCents()));

        put.addColumn(CF_PROFILE, COL_LOCATION, Bytes.toBytes(aUser.location()));
        put.addColumn(CF_PROFILE, COL_BUILD, Bytes.toBytes(aUser.build()));
        put.addColumn(CF_PROFILE, COL_PLATFORM, Bytes.toBytes(aUser.platform()));

        if (Objects.nonNull(aUser.custom01())) {
            put.addColumn(CF_PROFILE, COL_CUSTOM01, Bytes.toBytes(aUser.custom01()));
        }
        if (Objects.nonNull(aUser.custom02())) {
            put.addColumn(CF_PROFILE, COL_CUSTOM02, Bytes.toBytes(aUser.custom02()));
        }
        if (Objects.nonNull(aUser.custom03())) {
            put.addColumn(CF_PROFILE, COL_CUSTOM03, Bytes.toBytes(aUser.custom03()));
        }

        put.addColumn(CF_PROFILE, COL_SENTIMENT_SUM, Bytes.toBytes(aUser.sentimentSum()));
        put.addColumn(CF_PROFILE, COL_MESSAGE_COUNT, Bytes.toBytes(aUser.messagesCounts()));

        return put;
    }

    private List<Put> mkPut(final List<User> aUsers) {
        List<Put> puts = new ArrayList<>();
        for (User user: aUsers) {
            puts.add(mkPut(user));
        }
        return puts;
    }
}
