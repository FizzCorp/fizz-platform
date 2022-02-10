package io.fizz.gateway.event.listener;

import io.fizz.chat.domain.channel.ChannelMessageTranslated;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.AbstractEventListener;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.CountryCode;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.events.TextMessageTranslated;
import io.fizz.gateway.http.services.handler.eventstream.AbstractEventStreamClientHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MessageTranslatedEventListener implements AbstractEventListener {
    private static final LoggingService.Log logger = LoggingService.getLogger(MessageTranslatedEventListener.class);
    private static final DomainEventType[] events = new DomainEventType[] {
            ChannelMessageTranslated.TYPE
    };

    private final AbstractEventStreamClientHandler eventStreamHandler;

    public MessageTranslatedEventListener(AbstractEventStreamClientHandler aEventStreamHandler) {
        eventStreamHandler = aEventStreamHandler;
    }

    @Override
    public DomainEventType[] listensTo() {
        return events;
    }

    @Override
    public CompletableFuture<Void> handleEvent(AbstractDomainEvent aEvent) {
        try {
            if (ChannelMessageTranslated.TYPE.equals(aEvent.type())) {
                return onMessageTranslated((ChannelMessageTranslated) aEvent);
            } else {
                logger.warn("Unrecognized message encountered in listener: " + aEvent.type());
            }
        }
        catch (ClassCastException ex) {
            logger.error(ex.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> onMessageTranslated(ChannelMessageTranslated aEvent) {
        CompletableFuture<Void> resultFuture = new CompletableFuture<>();
        try {
            TextMessageTranslated translatedMsg = adaptTo(aEvent);
            eventStreamHandler.put(Collections.singletonList(translatedMsg))
            .whenComplete((aResult, aError) -> {
                if (Objects.nonNull(aError)) {
                    logger.warn(aError);
                    resultFuture.completeExceptionally(aError);
                } else {
                    resultFuture.complete(null);
                }
            });
        } catch (DomainErrorException | IllegalArgumentException ex) {
            logger.error(ex.getMessage());
            resultFuture.completeExceptionally(ex);
        }
        return resultFuture;
    }

    private TextMessageTranslated adaptTo(ChannelMessageTranslated aEvent) throws DomainErrorException {
        LanguageCode[] toLocalCodes = aEvent.to();
        List<String> toLocalsStr = new ArrayList<>(toLocalCodes.length);
        for (LanguageCode local : toLocalCodes) {
            toLocalsStr.add(local.value());
        }

        return new TextMessageTranslated.Builder()
                .setVersion(io.fizz.common.domain.events.AbstractDomainEvent.VERSION)
                .setId(aEvent.id())
                .setAppId(aEvent.appId())
                .setUserId(aEvent.userId())
                .setCountryCode(CountryCode.unknown())
                .setSessionId("TranslatedMessage001")
                .setLength(aEvent.length())
                .setFrom(LanguageCode.ENGLISH.value())
                .setTo(toLocalsStr.toArray(new String[0]))
                .setChannelId(String.valueOf(aEvent.channelId()))
                .setMessageId(String.valueOf(aEvent.messageId()))
                .setOccurredOn(aEvent.occurredOn().getTime())
                .get();
    }

}