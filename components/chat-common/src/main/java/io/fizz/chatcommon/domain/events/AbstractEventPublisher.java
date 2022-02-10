package io.fizz.chatcommon.domain.events;

import java.util.concurrent.CompletableFuture;

public interface AbstractEventPublisher {
    CompletableFuture<Void> publish(final AbstractDomainEvent aEvent);
    CompletableFuture<Void> addListener(final DomainEventType aEventType, final AbstractEventListener aListener);
    CompletableFuture<Void> removeListener(final DomainEventType aEventType, final AbstractEventListener aListener);
    DomainEventType[] publishes();
}
