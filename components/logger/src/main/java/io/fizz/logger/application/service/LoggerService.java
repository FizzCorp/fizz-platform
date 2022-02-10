package io.fizz.logger.application.service;

import io.fizz.chat.application.channel.ChannelApplicationService;
import io.fizz.chat.application.channel.ChannelMessagePublishCommand;
import io.fizz.chat.application.channel.ChannelHistoryQuery;
import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.common.domain.ApplicationId;
import io.fizz.logger.domain.LogCount;
import io.fizz.logger.domain.LogId;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LoggerService {
    private static final String USER_ID = "FizzLoggerService";

    private final ChannelApplicationService channelService;

    public LoggerService(final ChannelApplicationService aChannelService) {
        channelService = aChannelService;
    }

    public CompletableFuture<Void> write(final String aAppId,
                                         final String aLogId,
                                         final String aLogItem) {
        final ApplicationId appId = appId(aAppId);
        final ChannelId channelId = new ChannelId(appId, new LogId(aLogId).value());

        final ChannelMessagePublishCommand cmd = new ChannelMessagePublishCommand.Builder()
                .setChannelId(channelId)
                .setAuthorId(USER_ID)
                .setNick(null)
                .setBody(aLogItem)
                .setData(null)
                .setTranslate(false)
                .setFilter(false)
                .setPersist(true)
                .setNotifyList(Collections.emptySet())
                .setInternal(true)
                .build();

        return channelService.publish(cmd);
    }
    public CompletableFuture<List<ChannelMessage>> read(final String aAppId,
                                                        final String aLogId,
                                                        final Integer aCount) {
        final ChannelHistoryQuery query = new ChannelHistoryQuery(
                new ChannelId(appId(aAppId),
                new LogId(aLogId).value()),
                USER_ID,
                new LogCount(aCount).value(),
                null,
                null
        );

        return channelService.queryMessages(query);
    }

    private ApplicationId appId(final String aValue) {
        try {
            return new ApplicationId(aValue);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
}
