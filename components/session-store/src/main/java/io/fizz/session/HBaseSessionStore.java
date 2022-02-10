package io.fizz.session;

import com.google.common.primitives.Longs;
import com.google.protobuf.ByteString;
import io.fizz.client.hbase.HBaseClientModels;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HBaseSessionStore implements SessionStore {
    private static final LoggingService.Log logger = LoggingService.getLogger(HBaseSessionStore.class);

    private static final String NAMESPACE = ConfigService.config().getString("hbase.sessions.namespace");
    private static final Boolean TTL_ENABLED = ConfigService.config().getBoolean("hbase.sessions.enable.ttl");
    // this should match the timeout set on the columnfamily
    private static final long MAX_SESSION_TIMEOUT = 30 * 60 * 1000;
    private static final long DEFAULT_RETRY_TIMEOUT = 5 * 1000;
    private static final int DEFAULT_SESSIONID_LENGTH = 16;

    private static final ByteString NS_NAME = ByteString.copyFrom(Bytes.toBytes(NAMESPACE));
    private static final ByteString TABLE_NAME = ByteString.copyFrom(Bytes.toBytes("tbl_session"));
    private static final ByteString CF_CONTENT = ByteString.copyFrom(Bytes.toBytes("c"));
    private static final ByteString COL_DATA = ByteString.copyFrom(Bytes.toBytes("d"));
    private static final ByteString COL_CREATED = ByteString.copyFrom(Bytes.toBytes("cd"));

    private static final HBaseClientModels.Table TABLE = HBaseClientModels.Table.newBuilder()
            .setNamespace(NS_NAME)
            .setName(TABLE_NAME)
            .build();

    private final AbstractHBaseClient client;
    private final PRNG random;

    public HBaseSessionStore(final Vertx aVertx, final AbstractHBaseClient aClient) {
        if (Objects.isNull(aVertx)) {
            throw new IllegalArgumentException("invalid vertx instance specified.");
        }
        if (Objects.isNull(aClient)) {
            throw new IllegalArgumentException("invalid hbase client specified.");
        }

        client = aClient;
        random = new PRNG(aVertx);
    }

    @Override
    public SessionStore init(Vertx vertx, JsonObject options) {
        return this;
    }

    @Override
    public long retryTimeout() {
        return DEFAULT_RETRY_TIMEOUT;
    }

    @Override
    public Session createSession(long aTimeoutMS) {
        return new FizzSessionImpl( random, Math.min(aTimeoutMS, MAX_SESSION_TIMEOUT), DEFAULT_SESSIONID_LENGTH);
    }

    @Override
    public Session createSession(long aTimeoutMS, int aIdLength) {
        return new FizzSessionImpl( random, Math.min(aTimeoutMS, MAX_SESSION_TIMEOUT), aIdLength);
    }

    @Override
    public void get(String aSessionId, Handler<AsyncResult<Session>> handler) {
        if (Objects.isNull(aSessionId)) {
            handler.handle(Future.succeededFuture(null));
        }
        else {
            HBaseClientModels.Get get = mkGet(aSessionId);
            client.get(get)
            .handle((aResult, aError) -> {
                if (Objects.isNull(aError)) {
                    if (aResult.getColumnsCount() == 0) {
                        handler.handle(Future.succeededFuture(null));
                    }
                    else {
                        Date creationDate = new Date();
                        Buffer sessionBuffer = null;
                        for (int ci = 0; ci < aResult.getColumnsCount(); ci++) {
                            final HBaseClientModels.ColumnValue col = aResult.getColumns(ci);
                            final ByteString qualifier = col.getKey().getQualifier();

                            if (qualifier.equals(COL_DATA)) {
                                sessionBuffer = Buffer.buffer(col.getValue().toByteArray());
                            }
                            else
                            if (qualifier.equals(COL_CREATED)) {
                                creationDate = new Date(Longs.fromByteArray(col.getValue().toByteArray()));
                            }
                        }
                        FizzSessionImpl session = new FizzSessionImpl(creationDate);
                        session.readFromBuffer(0, sessionBuffer);

                        if (Utils.now() - session.createdOn().getTime() > session.timeout()) {
                            session = null;
                        }
                        handler.handle(Future.succeededFuture(session));
                    }
                }
                else {
                    handler.handle(Future.failedFuture(aError));
                }

                return CompletableFuture.completedFuture(null);
            });
        }
    }

    @Override
    public void delete(String aSessionId, Handler<AsyncResult<Void>> handler) {
        logger.warn("Session deletion not supported in HBase session store");
    }

    @Override
    public void put(Session aSession, Handler<AsyncResult<Void>> handler) {
        final HBaseClientModels.Put put = mkPut(aSession);
        client.put(put)
        .handle((v, aError) -> {
            if (Objects.isNull(aError)) {
                handler.handle(Future.succeededFuture());
            }
            else {
                handler.handle(Future.failedFuture(aError));
            }

            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        logger.warn("Clear the HBase session store not supported");
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        logger.warn("Can not get size of the HBase session store");
    }

    @Override
    public void close() {

    }

    private byte[] mkRowKey(String id) {
        return Bytes.toBytes(id);
    }

    private HBaseClientModels.Get mkGet(String aId) {
        final HBaseClientModels.Get.Builder get = HBaseClientModels.Get.newBuilder();

        get.setTable(TABLE);
        get.setRowKey(ByteString.copyFrom(mkRowKey(aId)));
        get.addFamilies(CF_CONTENT);

        return get.build();
    }

    private HBaseClientModels.Put mkPut(Session aSession) {
        final HBaseClientModels.Put.Builder put = HBaseClientModels.Put.newBuilder();
        final FizzSessionImpl session = (FizzSessionImpl) aSession;
        final Buffer buffer = Buffer.buffer();
        session.writeToBuffer(buffer);

        put.setTable(TABLE);
        put.setRowKey(ByteString.copyFrom(mkRowKey(aSession.id())));
        put.addColumns(HBaseClientModels.ColumnValue.newBuilder()
                .setKey(HBaseClientModels.ColumnCoord.newBuilder()
                        .setFamily(CF_CONTENT).setQualifier(COL_DATA)
                        .build())
                .setValue(ByteString.copyFrom(buffer.getBytes()))
                .build());
        put.addColumns(HBaseClientModels.ColumnValue.newBuilder()
                .setKey(HBaseClientModels.ColumnCoord.newBuilder()
                        .setFamily(CF_CONTENT).setQualifier(COL_CREATED)
                        .build())
                .setValue(ByteString.copyFrom(Longs.toByteArray(session.createdOn().getTime())))
                .build());

        if (TTL_ENABLED) {
            put.setTtl(session.timeout());
        }

        return put.build();
    }
}
