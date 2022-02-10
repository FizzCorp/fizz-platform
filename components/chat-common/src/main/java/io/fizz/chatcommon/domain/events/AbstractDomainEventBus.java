package io.fizz.chatcommon.domain.events;

import java.util.concurrent.CompletableFuture;

public interface AbstractDomainEventBus {
    void register(AbstractEventPublisher aPublisher);
    CompletableFuture<Void> publish(final AbstractDomainEvent aEvent);
    CompletableFuture<Void> addListener(final AbstractEventListener aListener);
    CompletableFuture<Void> removeListener(final AbstractEventListener aListener);
}
