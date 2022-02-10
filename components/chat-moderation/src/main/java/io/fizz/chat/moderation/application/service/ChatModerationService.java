package io.fizz.chat.moderation.application.service;

import io.fizz.chat.moderation.application.repository.AbstractChatModerationRepository;
import io.fizz.chat.moderation.domain.*;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.QueryRange;
import io.fizz.common.domain.UserId;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ChatModerationService {
    private final AbstractChatModerationRepository chatModerationRepo;

    public ChatModerationService(final AbstractChatModerationRepository aChatModerationRepo) {
        Utils.assertRequiredArgument(aChatModerationRepo, "invalid content moderation repository specified");
        chatModerationRepo = aChatModerationRepo;
    }

    public CompletableFuture<String> reportMessage(final String aAppId,
                                                    final String aReporterUserId,
                                                    final String aReportedUserId,
                                                    final String aChannelId,
                                                    final String aMessage,
                                                    final String aMessageId,
                                                    final String aLanguage,
                                                    final String aOffense,
                                                    final String aDescription,
                                                    final Long aTime) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final UserId reporterUserId = new UserId(aReporterUserId);
            final UserId reportedUserId = new UserId(aReportedUserId);
            final ChannelId channelId = new ChannelId(appId, aChannelId);
            final ReportOffense offense = new ReportOffense(aOffense);
            final LanguageCode lang = LanguageCode.fromValue(aLanguage);
            final String id = generateReportedMessageId(appId, reporterUserId, reportedUserId, aMessageId, lang);

            final ReportedMessage reportedMessage = new ReportedMessage(
                    id,
                    appId,
                    reporterUserId,
                    reportedUserId,
                    aMessage,
                    aMessageId,
                    channelId,
                    lang,
                    offense,
                    aDescription,
                    aTime);
            return chatModerationRepo.save(reportedMessage);
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<ReportedMessageSearchResult> searchMessages(final String aAppId,
                                                                         final String aUserId,
                                                                         final String aChannelId,
                                                                         final String aLanguage,
                                                                         final Integer aCursor,
                                                                         final Integer aPageSize,
                                                                         final String aSort,
                                                                         final Long aStartTs,
                                                                         final Long aEndTs) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final UserId userId = Objects.nonNull(aUserId) ? new UserId(aUserId) : null;
            final ChannelId channelId = Objects.nonNull(aChannelId) ? new ChannelId(appId, aChannelId) : null;
            final LanguageCode lang = Objects.nonNull(aLanguage) ? LanguageCode.fromValue(aLanguage) : null;
            final QueryRange range = new QueryRange(aStartTs, aEndTs);

            return chatModerationRepo.searchMessages(appId, userId, channelId, lang, range, aCursor, aPageSize, aSort);
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<List<ReportedUser>> searchUsers(final String aAppId,
                                                             final String aChannelId,
                                                             final String aLanguage,
                                                             final Integer aResultLimit,
                                                             final Long aStartTs,
                                                             final Long aEndTs) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final ChannelId channelId = Objects.nonNull(aChannelId) ? new ChannelId(appId, aChannelId) : null;
            final LanguageCode lang = Objects.nonNull(aLanguage) ? LanguageCode.fromValue(aLanguage) : null;
            final QueryRange range = new QueryRange(aStartTs, aEndTs);

            return chatModerationRepo.searchUsers(appId, channelId, lang, aResultLimit, range);
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<List<ReportedChannel>> searchChannels(final String aAppId,
                                                                   final String aLanguage,
                                                                   final Integer aResultLimit,
                                                                   final Long aStartTs,
                                                                   final Long aEndTs) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final LanguageCode lang = Objects.nonNull(aLanguage) ? LanguageCode.fromValue(aLanguage) : null;
            final QueryRange range = new QueryRange(aStartTs, aEndTs);

            return chatModerationRepo.searchChannels(appId, lang, aResultLimit, range);
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    private String generateReportedMessageId(final ApplicationId aAppId, final UserId aReporterUserId, final UserId aReportedUserId, final String aMessageId, final LanguageCode aLang) {
        return Base64.getEncoder().encodeToString(
                Utils.md5Sum(
                        aAppId.value() +
                        aReporterUserId.value() +
                        aReportedUserId.value() +
                        aMessageId +
                        aLang.value()
                ));
    }
}
