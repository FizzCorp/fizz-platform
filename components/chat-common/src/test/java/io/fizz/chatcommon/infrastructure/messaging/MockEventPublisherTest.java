package io.fizz.chatcommon.infrastructure.messaging;

import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.AbstractEventListener;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.SampleEvent;
import io.fizz.chatcommon.infrastructure.serde.JsonEventSerde;
import io.fizz.common.domain.UserId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class MockEventPublisherTest {
    @Test
    @DisplayName("it should publish event correctly")
    void basicPublishTest() {
        final UserId user = new UserId("userA");
        final SampleEvent event = new SampleEvent("event data", new Date().getTime());
        final JsonEventSerde serde = new JsonEventSerde();
        final MockEventPublisher publisher = new MockEventPublisher(serde, new DomainEventType[]{SampleEvent.TYPE});

        final DomainEventType[] listens = publisher.publishes();
        Assertions.assertEquals(listens.length, 1);
        Assertions.assertEquals(listens[0], SampleEvent.TYPE);

        publisher.addListener(SampleEvent.TYPE, new AbstractEventListener() {
            @Override
            public CompletableFuture<Void> handleEvent(AbstractDomainEvent aEvent) {
                Assertions.assertEquals(event.type(), aEvent.type());

                SampleEvent publishedEvent = (SampleEvent)aEvent;
                Assertions.assertEquals(publishedEvent, event);

                return CompletableFuture.completedFuture(null);
            }

            @Override
            public DomainEventType[] listensTo() {
                return new DomainEventType[]{SampleEvent.TYPE};
            }
        });

        publisher.publish(event);
    }

    @Test
    @DisplayName("it should not create publisher for invalid input")
    void invalidInputCreationTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new MockEventPublisher(null, new DomainEventType[]{})
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new MockEventPublisher(new JsonEventSerde(), null)
        );
    }

    @Test
    @DisplayName("it should handle invalid input properly ")
    void invalidInputTest() {
        final MockEventPublisher publisher = new MockEventPublisher(new JsonEventSerde(), new DomainEventType[]{});

        Assertions.assertThrows(
                ExecutionException.class,
                () -> publisher.publish(null).get()
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> publisher.addListener(SampleEvent.TYPE, null)
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> publisher.addListener(null, new AbstractEventListener() {
                    @Override
                    public CompletableFuture<Void> handleEvent(AbstractDomainEvent aEvent) {
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public DomainEventType[] listensTo() {
                        return new DomainEventType[0];
                    }
                })
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> publisher.removeListener(SampleEvent.TYPE, null)
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> publisher.removeListener(null, new AbstractEventListener() {
                    @Override
                    public CompletableFuture<Void> handleEvent(AbstractDomainEvent aEvent) {
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public DomainEventType[] listensTo() {
                        return new DomainEventType[0];
                    }
                })
        );
    }
}
