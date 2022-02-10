package io.fizz.gateway.http.services.proxy.eventstream;

import io.fizz.gateway.persistence.AbstractEventStream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.Objects;

public class EventStreamClient implements AbstractEventStreamClient {
    private AbstractEventStream eventStream;

    public EventStreamClient(final AbstractEventStream aEventStream) {
        if (Objects.isNull(aEventStream)) {
            throw new IllegalArgumentException("invalid event stream specified.");
        }
        eventStream = aEventStream;
    }

    @Override
    public AbstractEventStreamClient put(final List<String> records, final Handler<AsyncResult<Integer>> handler) {
        eventStream.put(records, handler);
        return this;
    }
}
