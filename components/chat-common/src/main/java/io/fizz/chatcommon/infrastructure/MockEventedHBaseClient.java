package io.fizz.chatcommon.infrastructure;

import com.google.protobuf.ByteString;
import io.fizz.chatcommon.domain.events.AbstractDomainEventBus;
import io.fizz.chatcommon.infrastructure.serde.AbstractEventSerde;
import io.fizz.client.hbase.HBaseClientModels;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.fizz.common.Utils;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.concurrent.CompletableFuture;

public class MockEventedHBaseClient extends MockHBaseClient {
    private final AbstractDomainEventBus eventBus;
    private final AbstractEventSerde serde;

    public MockEventedHBaseClient(final AbstractDomainEventBus aEventBus,
                                  final AbstractEventSerde aSerde) {
        Utils.assertRequiredArgument(aEventBus, "invalid event bus");
        Utils.assertRequiredArgument(aSerde, "invalid serde");

        this.eventBus = aEventBus;
        this.serde = aSerde;
    }

    @Override
    public CompletableFuture<Boolean> put(HBaseClientModels.Put aPutModel) {
        return super.put(aPutModel)
                .thenApply(aStatus -> {
                    if (aStatus) {
                        for (int ci = 0; ci < aPutModel.getColumnsCount(); ci++) {
                            HBaseClientModels.ColumnValue col = aPutModel.getColumns(ci);
                            if (col.getKey().getFamily().equals(HBaseDomainAggregateRepository.CF_EVENTS)) {
                                ByteString value = col.getValue();
                                eventBus.publish(serde.deserialize(Bytes.toString(value.toByteArray())));
                            }
                        }
                    }

                    return aStatus;
                });
    }
}
