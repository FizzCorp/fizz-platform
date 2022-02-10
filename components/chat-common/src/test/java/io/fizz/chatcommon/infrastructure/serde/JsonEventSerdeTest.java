package io.fizz.chatcommon.infrastructure.serde;

import io.fizz.chatcommon.infrastructure.SampleEvent;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

class JsonEventSerdeTest {
    @Test
    @DisplayName("it should serialize and deserialize an event")
    void eventSerdeTest() throws DomainErrorException {
        final ApplicationId app = new ApplicationId("appA");
        final SampleEvent lhs = new SampleEvent(
                "event data",
                new Date().getTime()
            );

        final JsonEventSerde serde = new JsonEventSerde();
        final String buffer = serde.serialize(lhs);
        final SampleEvent rhs = (SampleEvent) serde.deserialize(buffer);

        Assertions.assertEquals(lhs.getData(), rhs.getData());
        Assertions.assertEquals(lhs.occurredOn(), rhs.occurredOn());
        Assertions.assertEquals(lhs.type(), rhs.type());
    }
}
