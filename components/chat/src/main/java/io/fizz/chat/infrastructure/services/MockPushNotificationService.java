package io.fizz.chat.infrastructure.services;

import io.fizz.chat.application.services.AbstractPushNotificationService;
import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MockPushNotificationService implements AbstractPushNotificationService
{
    private List<UserId> notifyList = new ArrayList<>();

    @Override
    public CompletableFuture<Void> send(final ApplicationId aAppId,
                                        final ChannelMessage aMessage,
                                        final Set<UserId> aNotifyList) {
        notifyList.addAll(aNotifyList);
        return CompletableFuture.completedFuture(null);
    }

    public List<UserId> notifyList() {
        return notifyList;
    }
}
