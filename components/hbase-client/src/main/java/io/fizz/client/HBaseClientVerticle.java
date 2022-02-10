package io.fizz.client;

import io.fizz.client.hbase.HBaseClientHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.util.concurrent.ExecutionException;

public class HBaseClientVerticle extends AbstractVerticle {
    private HBaseClientHandler hBaseClientHandler;

    @Override
    public void start(Future<Void> startFuture) {
        try {
            final String KEY_ZOOKEEPER_QUORUM = "hbase.client.quorum";
            final String KEY_ZOOKEEPER_CLIENTPORT = "hbase.client.clientport";
            final String KEY_HBASE_DISTRIBUTED_CLUSTERED = "hbase.client.cluster.distributed";
            final String KEY_ZOOKEEPER_ZNODE_PARENT = "hbase.client.zookeeper.znode.parent";
            final String KEY_HBASE_RPC_TIMEOUT = "hbase.client.rpc.timeout";

            final Configuration conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.quorum", ConfigService.config().getString(KEY_ZOOKEEPER_QUORUM));
            conf.set("hbase.zookeeper.property.clientPort", ConfigService.config().getString(KEY_ZOOKEEPER_CLIENTPORT));
            conf.set("hbase.cluster.distributed", ConfigService.config().getString(KEY_HBASE_DISTRIBUTED_CLUSTERED));
            conf.set("zookeeper.znode.parent", ConfigService.config().getString(KEY_ZOOKEEPER_ZNODE_PARENT));
            conf.set("hbase.client.retries.number", "2");
            conf.set("hbase.rpc.timeout", ConfigService.config().getString(KEY_HBASE_RPC_TIMEOUT));
            conf.set("hbase.client.pause", "1000");
            conf.set("timeout", "1000");

            final AsyncConnection connection = ConnectionFactory.createAsyncConnection(conf).get();

            hBaseClientHandler = new HBaseClientHandler(connection, vertx);
            hBaseClientHandler.open();
            startFuture.complete();
        }
        catch (InterruptedException | ExecutionException ex) {
            startFuture.fail(ex);
        }
    }

    @Override
    public void stop() throws Exception {
        hBaseClientHandler.close();
        super.stop();
    }
}
