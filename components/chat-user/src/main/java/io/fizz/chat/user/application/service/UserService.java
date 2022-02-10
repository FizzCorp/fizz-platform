package io.fizz.chat.user.application.service;

import io.fizz.chat.presence.AbstractUserPresenceService;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.user.application.ProfileNotificationService;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.user.application.UserDTO;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.Url;
import io.fizz.common.domain.UserId;
import io.fizz.chat.user.domain.Nick;
import io.fizz.chat.user.domain.User;
import io.fizz.chat.user.application.UpdateUserCommand;
import io.fizz.chat.user.application.repository.AbstractUserRepository;
import io.fizz.chat.user.domain.StatusMessage;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class UserService implements AbstractUserApplicationService {
    private static final LoggingService.Log log = LoggingService.getLogger(UserService.class);

    private final ProfileNotificationService profileNotificationService;
    private final AbstractUserRepository userRepository;
    private final AbstractUserPresenceService presenceService;

    public UserService(final AbstractTopicMessagePublisher aMessagePublisher,
                       final AbstractUserRepository aUserRepository,
                       final AbstractUserPresenceService aPresenceService) {
        Utils.assertRequiredArgument(aMessagePublisher, "invalid message publisher");
        Utils.assertRequiredArgument(aUserRepository, "invalid user repository");
        Utils.assertRequiredArgument(aUserRepository, "invalid presence service");

        profileNotificationService = new ProfileNotificationService(aMessagePublisher);
        userRepository = aUserRepository;
        presenceService = aPresenceService;
    }

    public CompletableFuture<Void> open() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<UserDTO> fetchUser(final ApplicationId aAppId, final UserId aUserId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aAppId, "invalid application id specified.");
            Utils.assertRequiredArgument(aUserId, "invalid user id specified.");

            CompletableFuture<User> userFetched = fetchUserProfile(aAppId, aUserId);
            CompletableFuture<Boolean> presenceFetched = presenceService.get(aAppId, aUserId);

            return userFetched.thenCombine(presenceFetched,
                    (aUser, aIsOnline) ->
                            new UserDTO(
                                    aUser.userId(),
                                    aUser.nick(),
                                    aUser.statusMessage(),
                                    aUser.profileUrl(),
                                    aIsOnline,
                                    aUser.token()
                            )
            );
        });
    }

    public CompletableFuture<Boolean> updateUser(final UpdateUserCommand aCmd) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aCmd, "invalid update command specified.");
            CompletableFuture<User> fetched = fetchUserProfile(aCmd.appId(), aCmd.userId());
            CompletableFuture<User> saved = fetched.thenCompose(aExistingProfile -> {
                User updatedProfile = adaptTo(aExistingProfile, aCmd);
                return saveUser(updatedProfile).thenApply(aSaved -> aSaved ? updatedProfile : null);
            });
            return saved.thenApply(aProfile -> {
                if (Objects.nonNull(aProfile)) {
                    profileNotificationService.publishUserUpdated(aProfile)
                            .whenComplete((aVoid, aError) -> {
                                if (Objects.nonNull(aError)) {
                                    log.error(aError.getMessage());
                                }
                            });
                }
                return Objects.nonNull(aProfile);
            });
        });
    }

    public CompletableFuture<Void> subscribeUser(final ApplicationId aAppId,
                                                 final UserId aUserId,
                                                 final SubscriberId aSubscriberId) {
        return profileNotificationService.subscribe(aAppId, Collections.singleton(aUserId), aSubscriberId);
    }

    public CompletableFuture<Void> unsubscribeUser(final ApplicationId aAppId,
                                                   final UserId aUserId,
                                                   final SubscriberId aSubscriberId) {
        return profileNotificationService.unsubscribe(aAppId, Collections.singleton(aUserId), aSubscriberId);
    }

    public CompletableFuture<Boolean> setDeviceToken(final ApplicationId aAppId,
                                                     final UserId aUserId,
                                                     final String aToken) {
        return Utils.async(() -> {
            final CompletableFuture<User> fetched = fetchUserProfile(aAppId, aUserId);

            final CompletableFuture<User> updated = fetched.thenApply(aUser -> {
                aUser.setToken(aToken);
                return aUser;
            });

            return updated.thenCompose(this::saveUser);
        });
    }

    public CompletableFuture<Boolean> clearDeviceToken(final ApplicationId aAppId, final UserId aUserId) {
        return Utils.async(() -> {
            final CompletableFuture<User> fetched = fetchUserProfile(aAppId, aUserId);

            final CompletableFuture<User> updated = fetched.thenApply(aUser -> {
                aUser.clearToken();
                return aUser;
            });

            return updated.thenCompose(this::saveUser);
        });
    }

    private CompletableFuture<User> fetchUserProfile(final ApplicationId aAppId,
                                                     final UserId aUserId) {
        return userRepository.get(aAppId, aUserId)
                .thenApply(user -> {
                    if (Objects.isNull(user)) {
                        return new User.Builder()
                                .setAppId(aAppId)
                                .setUserId(aUserId)
                                .build();
                    }
                    return user;
                });
    }

    protected CompletableFuture<Boolean> saveUser(User aUser) {
        return userRepository.put(aUser);
    }

    private User adaptTo(final User aProfile, final UpdateUserCommand aCmd) {
        final Nick nick = Objects.isNull(aCmd.nick()) ? aProfile.nick() : aCmd.nick();
        final StatusMessage statusMessage = Objects.isNull(aCmd.statusMessage())
                ? aProfile.statusMessage() : aCmd.statusMessage();
        final Url profileUrl = Objects.isNull(aCmd.profileUrl()) ? aProfile.profileUrl() : aCmd.profileUrl();
        return new User.Builder()
                .setAppId(aCmd.appId())
                .setUserId(aCmd.userId())
                .setNick(nick)
                .setStatusMessage(statusMessage)
                .setProfileUrl(profileUrl)
                .build();
    }
}