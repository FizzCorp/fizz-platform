package io.fizz.chatcommon.domain.events;

import io.fizz.common.LoggingService;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DomainEventBus implements AbstractDomainEventBus {
    private final LoggingService.Log logger = LoggingService.getLogger(DomainEventBus.class);
    private final Map<DomainEventType, AbstractEventPublisher> publishers = new HashMap<>();

    @Override
    public void register(AbstractEventPublisher aPublisher) {
        if (Objects.isNull(aPublisher)) {
            throw new IllegalArgumentException("invalid publisher specified.");
        }

        final DomainEventType[] eventTypes = aPublisher.publishes();
        for (DomainEventType eventType: eventTypes) {
            if (!Objects.isNull(publishers.get(eventType))) {
                logger.warn("Replacing publisher for event type: " + eventType);
            }
            publishers.put(eventType, aPublisher);
        }
    }

    @Override
    public CompletableFuture<Void> publish(final AbstractDomainEvent aEvent) {
        if (Objects.isNull(aEvent)) {
            throw new IllegalArgumentException("invalid event specified for publishing.");
        }

        final DomainEventType evenType = aEvent.type();
        final AbstractEventPublisher publisher = publishers.get(evenType);
        if (Objects.isNull(publisher)) {
            logger.warn("publisher not registered for event type: " + evenType);
            return CompletableFuture.completedFuture(null);
        }
        else {
            return publisher.publish(aEvent);
        }
    }

    @Override
    public CompletableFuture<Void> addListener(final AbstractEventListener aListener) {
        if (Objects.isNull(aListener)) {
            throw new IllegalArgumentException("invalid subscriber specified.");
        }

        final List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (final DomainEventType eventType: aListener.listensTo()) {
            final AbstractEventPublisher publisher = publishers.get(eventType);
            if (Objects.isNull(publisher)) {
                logger.warn("publisher not registered for event type: " + eventType);
                continue;
            }

            futures.add(publisher.addListener(eventType, aListener));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public CompletableFuture<Void> removeListener(final AbstractEventListener aListener) {
        if (Objects.isNull(aListener)) {
            throw new IllegalArgumentException("invalid subscriber specified.");
        }

        final List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (final DomainEventType eventType: aListener.listensTo()) {
            final AbstractEventPublisher publisher = publishers.get(eventType);
            if (Objects.isNull(publisher)) {
                logger.warn("publisher not registered for event type: " + eventType);
                continue;
            }

            futures.add(publisher.removeListener(eventType, aListener));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }
}
