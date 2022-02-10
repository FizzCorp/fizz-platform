package io.fizz.chat.domain.channel;

import io.fizz.chat.domain.Permissions;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.Utils;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Channel {
    private static final int MAX_MSG_QUERY_COUNT = 50;

    private static DomainErrorException ERROR_INVALID_QUERY_COUNT = new DomainErrorException("invalid_query_count");
    private static IllegalStateException ERROR_MESSAGE_NOT_FOUND = new IllegalStateException("message_not_found");

    private final ChannelId id;

    public Channel(final ChannelId aId) {
        Utils.assertRequiredArgument(aId, "invalid_channel_id");

        id = aId;
    }

    public CompletableFuture<ChannelMessage> publishMessage(final AbstractChannelAuthorizationService aAuthService,
                                                            final AbstractChannelMessageRepository aMessageRepo,
                                                            final TopicId aTopicId,
                                                            final UserId aAuthorId,
                                                            final String aNick,
                                                            final String aBody,
                                                            final String aData,
                                                            final LanguageCode aLocale) {
        return aAuthService.assertOperateOwned(id, aAuthorId, aAuthorId, Permissions.PUBLISH_MESSAGES)
                .thenCompose(v -> aMessageRepo.nextMessageId(id.appId(), aTopicId))
                .thenApply(aMessageId -> new ChannelMessage(
                            aMessageId,
                            aAuthorId,
                            aNick,
                            id,
                            aTopicId,
                            aBody,
                            aData,
                            aLocale,
                            new Date(),
                            null
                ));
    }

    public CompletableFuture<ChannelMessage> editMessage(final AbstractChannelAuthorizationService aAuthService,
                                                         final ChannelMessage aMessage,
                                                         final UserId aAuthorId,
                                                         final String aNick,
                                                         final String aBody,
                                                         final String aData,
                                                         final LanguageCode aLocale) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aMessage, ERROR_MESSAGE_NOT_FOUND);

            return aAuthService.assertOperateOwned(id, aMessage.from(), aAuthorId, Permissions.EDIT_OWN_MESSAGE)
                    .thenApply(v -> {
                        if (Objects.nonNull(aNick)) {
                            aMessage.updateNick(aNick);
                        }
                        if (Objects.nonNull(aBody)) {
                            aMessage.updateBody(aBody, aLocale);
                        }
                        if (Objects.nonNull(aData)) {
                            aMessage.updateData(aData);
                        }
                        return aMessage;
                    });
        });
    }

    public CompletableFuture<ChannelMessage> deleteMessage(final AbstractChannelAuthorizationService aAuthService,
                                                           final ChannelMessage aMessage,
                                                           final UserId aAuthorId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aMessage, ERROR_MESSAGE_NOT_FOUND);
            Utils.assertRequiredArgument(aAuthorId, "invalid_author_id");

            return aAuthService
                    .buildAuthorization(id, aMessage.from(), aAuthorId)
                    .thenApply(aAuth -> {
                        aAuth.assertOperateNotOwned(Permissions.DELETE_ANY_MESSAGE.value())
                                .assertOperateOwned(Permissions.DELETE_OWN_MESSAGE.value())
                                .validate();

                        aMessage.markAsDeleted();

                        return aMessage;
                    });
        });
    }

    public CompletableFuture<List<ChannelMessage>> listMessages(final AbstractChannelAuthorizationService aAuthService,
                                                                final AbstractChannelMessageRepository aMessageRepo,
                                                                final UserId aRequsterId,
                                                                final TopicId aTopicId,
                                                                final int aCount,
                                                                final Long aBefore,
                                                                final Long aAfter) {
        if (aCount < 0 || aCount > MAX_MSG_QUERY_COUNT) {
            return Utils.failedFuture(ERROR_INVALID_QUERY_COUNT);
        }

        return aAuthService
                .assertOperateOwned(id, aRequsterId, aRequsterId, Permissions.READ_MESSAGES)
                .thenCompose(v -> aMessageRepo.query(id.appId(), aTopicId, aCount, aBefore, aAfter));
    }

    public CompletableFuture<Void> muteUser(final AbstractChannelAuthorizationService aAuthService,
                                            final UserId aModeratorId,
                                            final UserId aUserToMuteId,
                                            final Date aEnds) {
        return aAuthService
                .assertOperateNotOwned(id, aUserToMuteId, aModeratorId, Permissions.MANAGE_MUTES)
                .thenCompose(v -> aAuthService.muteUser(id, aUserToMuteId, aEnds));
    }

    public CompletableFuture<Void> unmuteUser(final AbstractChannelAuthorizationService aAuthService,
                                              final UserId aModeratorId,
                                              final UserId aMutedUserId) {
        return aAuthService
                .assertOperateNotOwned(id, aMutedUserId, aModeratorId, Permissions.MANAGE_MUTES)
                .thenCompose(v -> aAuthService.unmuteUser(id, aMutedUserId));
    }

    public CompletableFuture<Void> addUserBan(final AbstractChannelAuthorizationService aAuthService,
                                              final UserId aModeratorId,
                                              final UserId aUserToMuteId,
                                              final Date aEnds) {
        return aAuthService
                .assertOperateNotOwned(id, aUserToMuteId, aModeratorId, Permissions.MANAGE_MUTES)
                .thenCompose(v -> aAuthService.addUserBan(id, aUserToMuteId, aEnds));
    }

    public CompletableFuture<Void> removeUserBan(final AbstractChannelAuthorizationService aAuthService,
                                                 final UserId aModeratorId,
                                                 final UserId aMutedUserId) {
        return aAuthService
                .assertOperateNotOwned(id, aMutedUserId, aModeratorId, Permissions.MANAGE_MUTES)
                .thenCompose(v -> aAuthService.removeUserBan(id, aMutedUserId));
    }
}
