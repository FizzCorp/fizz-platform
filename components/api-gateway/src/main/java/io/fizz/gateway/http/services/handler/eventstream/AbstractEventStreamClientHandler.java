package io.fizz.gateway.http.services.handler.eventstream;

import io.fizz.common.domain.events.AbstractDomainEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AbstractEventStreamClientHandler {
    CompletableFuture<Integer> put(final List<AbstractDomainEvent> aEvents) throws IllegalArgumentException;
}
