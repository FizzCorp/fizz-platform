package io.fizz.analytics.jobs.hive2hbase;

import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.adapter.UserRowAdapter;
import io.fizz.analytics.domain.User;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

public class ProfileUpdateEventToUserTest extends AbstractSparkTest {
    @Test
    @DisplayName("it should transform update profile event to User")
    void transformUpdateProfileEvents() throws Exception {
        final MockProfileUpdateStore store = new MockProfileUpdateStore(spark);
        final Dataset<Row> updateProfileDS = store.scan();
        final ProfileUpdateEventToUser transformer = new ProfileUpdateEventToUser();
        final Dataset<Row> messagesDS = transformer.transform(updateProfileDS, null);
        final List<Row> userRows = messagesDS.collectAsList();

        for (final Row row: userRows) {
            User user = UserRowAdapter.toUser(row);
            assert (Objects.nonNull(user.id()));
            assert (Objects.nonNull(user.appId()));
            switch (user.id().value()) {
                case "userA":
                    assert (user.appId().value().equals("appA"));
                    assert (user.firstTimeActiveTS() == 1516095632);
                    assert (user.lastTimeActiveTS() == 1539682832);
                    assert (user.platform().equals("ios"));
                    assert (user.build().equals("build_1"));
                    assert (user.location().equals("PK"));
                    assert (user.amountSpentInCents() == 99);
                    assert (user.custom01().equals("A"));
                    assert (user.custom02().equals("B"));
                    assert (user.custom03().equals("C"));
                    assert (user.sentimentSum() == 20);
                    assert (user.messagesCounts() == 200);
                    break;
                case "userB":
                    assert (user.appId().value().equals("appA"));
                    assert (user.firstTimeActiveTS() == 1516095632);
                    assert (user.lastTimeActiveTS() == 1539682832);
                    assert (user.platform().equals("android"));
                    assert (user.build().equals("build_2"));
                    assert (user.location().equals("PK"));
                    assert (user.custom01().equals("D"));
                    assert (user.custom02().equals("E"));
                    assert (user.custom03().equals("F"));
                    assert (user.amountSpentInCents() == 9999);
                    assert (user.sentimentSum() == -0.5);
                    assert (user.messagesCounts() == 2000);
                    break;
                default:
                    assert (false);
            }
        }
    }
}
