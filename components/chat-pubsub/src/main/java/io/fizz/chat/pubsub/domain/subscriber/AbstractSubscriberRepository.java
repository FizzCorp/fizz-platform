package io.fizz.chat.pubsub.domain.subscriber;

public interface AbstractSubscriberRepository {
    SubscriberId nextIdentity();
}
