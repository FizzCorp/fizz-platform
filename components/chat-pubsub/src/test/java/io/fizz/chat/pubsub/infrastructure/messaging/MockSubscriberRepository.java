package io.fizz.chat.pubsub.infrastructure.messaging;

import io.fizz.chat.pubsub.domain.subscriber.AbstractSubscriberRepository;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;

import java.util.UUID;

public class MockSubscriberRepository implements AbstractSubscriberRepository {
    @Override
    public SubscriberId nextIdentity() {
        try {
            return new SubscriberId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        catch (Exception ex) {
            return null;
        }
    }
}
