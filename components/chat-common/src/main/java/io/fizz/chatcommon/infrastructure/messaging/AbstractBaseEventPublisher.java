package io.fizz.chatcommon.infrastructure.messaging;

import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.AbstractEventListener;
import io.fizz.chatcommon.domain.events.AbstractEventPublisher;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.common.Utils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractBaseEventPublisher implements AbstractEventPublisher {
    private final DomainEventType[] listenedEvents;
    private final Map<DomainEventType, List<AbstractEventListener>> listenersMap = new HashMap<>();

    public AbstractBaseEventPublisher(DomainEventType[] aListenedEvents) {
        Utils.assertRequiredArgument(aListenedEvents, "invalid listened events specified");

        this.listenedEvents = aListenedEvents;
    }

    @Override
    public CompletableFuture<Void> publish(AbstractDomainEvent aEvent) {
        return null;
    }

    @Override
    public CompletableFuture<Void> addListener(final DomainEventType aEventType, final AbstractEventListener aListener) {
        Utils.assertRequiredArgument(aEventType, "invalid event type specified");
        Utils.assertRequiredArgument(aListener, "invalid listener specified");

        removeListener(aEventType, aListener);
        createOrFindListeners(aEventType).add(aListener);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> removeListener(final DomainEventType aEventType, final AbstractEventListener aListener) {
        Utils.assertRequiredArgument(aEventType, "invalid event type specified");
        Utils.assertRequiredArgument(aListener, "invalid listener specified.");

        createOrFindListeners(aEventType).remove(aListener);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public DomainEventType[] publishes() {
        return listenedEvents;
    }

    protected List<AbstractEventListener> createOrFindListeners(final DomainEventType aEventType) {
        List<AbstractEventListener> listeners = listenersMap.get(aEventType);
        if (Objects.isNull(listeners)) {
            listeners = new ArrayList<>();
            listenersMap.put(aEventType, listeners);
        }
        return listeners;
    }
}
