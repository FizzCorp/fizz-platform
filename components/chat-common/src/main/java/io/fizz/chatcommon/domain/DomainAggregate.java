package io.fizz.chatcommon.domain;

import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.common.Utils;

import java.util.ArrayList;
import java.util.List;

public class DomainAggregate {
    private final List<AbstractDomainEvent> events = new ArrayList<>();
    private final EventsOffset eventsOffset;
    private final MutationId mutationId;

    public DomainAggregate(final MutationId aMutationId, final EventsOffset aEventsOffset) {
        Utils.assertRequiredArgument(aMutationId, "invalid_mutation_id");
        Utils.assertRequiredArgument(aEventsOffset, "invalid_events_offset");

        this.mutationId = aMutationId;
        this.eventsOffset = aEventsOffset;
    }

    public MutationId mutationId() {
        return mutationId;
    }

    public EventsOffset eventsOffset() {
        return eventsOffset;
    }

    public List<AbstractDomainEvent> events() {
        return events;
    }

    protected void emit(final AbstractDomainEvent aEvent) {
        events.add(aEvent);
    }
}
