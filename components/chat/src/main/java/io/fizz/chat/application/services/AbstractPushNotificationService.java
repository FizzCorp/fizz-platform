package io.fizz.chat.application.services;

import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface AbstractPushNotificationService {
    CompletableFuture<Void> send(final ApplicationId aAppId,
                                 final ChannelMessage aMessage,
                                 final Set<UserId> aNotifyList);
}
