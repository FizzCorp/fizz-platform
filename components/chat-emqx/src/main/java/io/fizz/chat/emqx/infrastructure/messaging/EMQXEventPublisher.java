package io.fizz.chat.emqx.infrastructure.messaging;

import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.AbstractEventListener;
import io.fizz.chatcommon.domain.events.AbstractEventPublisher;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.serde.AbstractEventSerde;
import io.fizz.common.Utils;
import io.fizz.common.domain.DomainErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class EMQXEventPublisher implements AbstractEventPublisher {
    private static final DomainErrorException ERROR_INVALID_EVENT = new DomainErrorException("invalid_event");

    private final HashMap<DomainEventType, ArrayList<AbstractEventListener>> listenersMap = new HashMap<>();
    private final DomainEventType[] events;
    private final AbstractEventSerde serde;

    public EMQXEventPublisher(final AbstractEventSerde aSerde, final DomainEventType[] aListenedEvents) {
        Utils.assertRequiredArgument(aSerde, "invalid event serde instance specified");
        Utils.assertRequiredArgument(aListenedEvents, "invalid listened events specified");

        serde = aSerde;
        events = aListenedEvents;
    }

    @Override
    public CompletableFuture<Void> publish(AbstractDomainEvent aEvent) {
        if (Objects.isNull(aEvent)) {
            return Utils.failedFuture(ERROR_INVALID_EVENT);
        }

        // important to keep consistent with other event publishers
        final AbstractDomainEvent event = serde.deserialize(serde.serialize(aEvent));

        final List<AbstractEventListener> listeners = listenersMap.get(event.type());
        if (Objects.nonNull(listeners)) {
            for (AbstractEventListener listener: listeners) {
                listener.handleEvent(event);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> addListener(final DomainEventType aEventType, final AbstractEventListener aListener) {
        Utils.assertRequiredArgument(aEventType, "invalid_event_typ");
        Utils.assertRequiredArgument(aListener, "invalid listener specified");

        removeListener(aEventType, aListener);
        ArrayList<AbstractEventListener> listeners = listenersMap.get(aEventType);
        if (Objects.isNull(listeners)){
            listeners = new ArrayList<>();
            listenersMap.put(aEventType, listeners);
        }
        listeners.add(aListener);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> removeListener(final DomainEventType aEventType, final AbstractEventListener aListener) {
        Utils.assertRequiredArgument(aEventType, "invalid_event_type");
        Utils.assertRequiredArgument(aListener, "invalid_listener");

        ArrayList<AbstractEventListener> listeners = listenersMap.get(aEventType);
        if (listeners != null) {
            listeners.remove(aListener);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public DomainEventType[] publishes() {
        return events;
    }
}
