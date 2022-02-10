package io.fizz.chatcommon.domain.events;

import java.util.Date;

public interface AbstractDomainEvent {
    Date occurredOn();

    DomainEventType type();

    default String streamId() {
        return null;
    }
}
