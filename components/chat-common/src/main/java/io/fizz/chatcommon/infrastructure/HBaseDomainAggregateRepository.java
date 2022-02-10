package io.fizz.chatcommon.infrastructure;

import com.google.protobuf.ByteString;
import io.fizz.chatcommon.domain.DomainAggregate;
import io.fizz.chatcommon.domain.EventsOffset;
import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.infrastructure.serde.AbstractEventSerde;
import io.fizz.chatcommon.infrastructure.serde.JsonEventSerde;
import io.fizz.client.hbase.HBaseClientModels;
import org.apache.hadoop.hbase.util.Bytes;

import java.nio.charset.StandardCharsets;

public class HBaseDomainAggregateRepository {
    public static final ByteString CF_EVENTS = ByteString.copyFrom(Bytes.toBytes("es"));
    private static final AbstractEventSerde eventSerde = new JsonEventSerde();

    public static long serialize(final DomainAggregate aAggregate,
                                 final HBaseClientModels.Put.Builder aPutBuilder,
                                 EventsOffset aEventsOffset) {
        long eventId = aEventsOffset.value();

        for (final AbstractDomainEvent event: aAggregate.events()) {
            ByteString qualifier = ByteString.copyFrom(Bytes.toBytes(eventId++));
            ByteString value = ByteString.copyFrom(eventSerde.serialize(event), StandardCharsets.UTF_8);
            aPutBuilder.addColumns(buildColumnValue(qualifier, value));
        }

        return eventId;
    }

    private static HBaseClientModels.ColumnValue buildColumnValue(final ByteString aQualifier, final ByteString aValue) {
        return HBaseClientModels.ColumnValue.newBuilder()
                .setKey(buildColumnCoord(aQualifier))
                .setValue(aValue)
                .build();
    }

    private static HBaseClientModels.ColumnCoord buildColumnCoord(final ByteString aQualifier) {
        return HBaseClientModels.ColumnCoord.newBuilder()
                .setFamily(CF_EVENTS)
                .setQualifier(aQualifier)
                .build();
    }
}
