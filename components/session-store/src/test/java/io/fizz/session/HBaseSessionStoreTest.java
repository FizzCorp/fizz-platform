package io.fizz.session;

import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Session;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class HBaseSessionStoreTest {
    private static int TEST_TIMEOUT = 10;
    static Vertx vertx;

    @BeforeAll
    static void setUp() {
        vertx = Vertx.vertx();
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> aContext.completeNow());
    }

    @Test
    @DisplayName("it should persist session properly")
    void sessionPersistenceTest(VertxTestContext aContext) throws InterruptedException {
        final AbstractHBaseClient client = new MockHBaseClient();
        final HBaseSessionStore store = new HBaseSessionStore(vertx, client);
        final Session session = store.createSession(1000);

        session.put("key", "value");
        final Future<Void> persisted = Future.future();
        store.put(session, persisted);
        persisted
        .compose(v ->  {
            Future<Session> got = Future.future();
            store.get(session.id(), got);
            return got;
        })
        .setHandler(aResult -> {
            Assertions.assertTrue(aResult.succeeded());
            Assertions.assertEquals(session.id(), aResult.result().id());
            Assertions.assertEquals(session.<String>get("key"), aResult.result().<String>get("key"));
            aContext.completeNow();
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should remove session properly on TTL expiration")
    void sessionTTLTest(VertxTestContext aContext) throws InterruptedException {
        final AbstractHBaseClient client = new MockHBaseClient();
        final HBaseSessionStore store = new HBaseSessionStore(vertx, client);
        final Session session = store.createSession(1000);

        final Future<Void> persisted = Future.future();
        store.put(session, persisted);
        persisted
                .compose(v ->  {
                    Future<Long> timerFired = Future.future();
                    vertx.setTimer(1005, l -> timerFired.complete());
                    return timerFired;
                })
                .compose(v -> {
                    Future<Session> got = Future.future();
                    store.get(session.id(), got);
                    return got;
                })
                .setHandler(aResult -> {
                    Assertions.assertTrue(aResult.succeeded());
                    Assertions.assertNull(aResult.result());
                    aContext.completeNow();
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should not create store for invalid argument")
    void invalidCreationTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new HBaseSessionStore(null, new MockHBaseClient()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new HBaseSessionStore(vertx, null));
    }
}
