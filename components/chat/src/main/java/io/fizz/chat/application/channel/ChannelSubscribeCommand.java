package io.fizz.chat.application.channel;

import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;

public class ChannelSubscribeCommand extends AbstractChannelCommand {
    private final UserId authorId;
    private final SubscriberId subscriberId;
    private final LanguageCode locale;

    public ChannelSubscribeCommand(final ChannelId aChannelId,
                                   final String aUserId,
                                   final String aSubscriberId,
                                   final String aLocale) {
        super(aChannelId);
        try {
            authorId = new UserId(aUserId);
            subscriberId = new SubscriberId(aSubscriberId);
            locale = LanguageCode.fromValue(aLocale);
        }
        catch (DomainErrorException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public UserId authorId() {
        return authorId;
    }

    public SubscriberId subscriberId() {
        return subscriberId;
    }

    public LanguageCode locale() {
        return locale;
    }
}
