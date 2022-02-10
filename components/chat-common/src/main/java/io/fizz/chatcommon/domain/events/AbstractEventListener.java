package io.fizz.chatcommon.domain.events;

import java.util.concurrent.CompletableFuture;

public interface AbstractEventListener {
    CompletableFuture<Void> handleEvent(final AbstractDomainEvent aEvent);
    DomainEventType[] listensTo();
}
