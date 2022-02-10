package io.fizz.chatcommon.infrastructure.serde;

import io.fizz.chatcommon.domain.events.AbstractDomainEvent;

public interface AbstractEventSerde {
    String serialize(final AbstractDomainEvent aEvent);
    AbstractDomainEvent deserialize(final String aEvent);
}
