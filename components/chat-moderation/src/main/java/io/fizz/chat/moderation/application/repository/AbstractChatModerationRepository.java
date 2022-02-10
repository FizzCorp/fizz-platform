package io.fizz.chat.moderation.application.repository;

import io.fizz.chat.moderation.domain.ReportedChannel;
import io.fizz.chat.moderation.domain.ReportedMessageSearchResult;
import io.fizz.chat.moderation.domain.ReportedUser;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.QueryRange;
import io.fizz.common.domain.UserId;
import io.fizz.chat.moderation.domain.ReportedMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AbstractChatModerationRepository {
    CompletableFuture<String> save(final ReportedMessage aMessage);
    CompletableFuture<ReportedMessageSearchResult> searchMessages(final ApplicationId aAppId,
                                                                  final UserId aUserId,
                                                                  final ChannelId aChannelId,
                                                                  final LanguageCode aLanguage,
                                                                  final QueryRange aRange,
                                                                  final Integer aCursor,
                                                                  final Integer aPageSize,
                                                                  final String aSort);
    CompletableFuture<List<ReportedUser>> searchUsers(final ApplicationId aAppId,
                                                      final ChannelId aChannelId,
                                                      final LanguageCode aLanguage,
                                                      final Integer aResultLimit,
                                                      final QueryRange aRange);
    CompletableFuture<List<ReportedChannel>> searchChannels(final ApplicationId aAppId,
                                                            final LanguageCode aLang,
                                                            final Integer aResultLimit,
                                                            final QueryRange aRange);
}
