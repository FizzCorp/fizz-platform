package io.fizz.chat.user.application.service;

import io.fizz.chat.presence.AbstractUserPresenceService;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.Url;
import io.fizz.common.domain.UserId;
import io.fizz.chat.user.application.repository.AbstractUserRepository;
import io.fizz.chat.user.domain.Nick;
import io.fizz.chat.user.domain.User;
import io.fizz.chat.user.domain.StatusMessage;

import java.util.concurrent.CompletableFuture;

public class MockUserService extends UserService {
    private static final String APP_ID = "appA";
    private static final UserId USER_A = new UserId("userA");
    private static final UserId USER_B = new UserId("user_offline");
    private static final Nick NICK_A = new Nick("nickA");
    private static final Nick NICK_B = new Nick("nickB");
    private static final StatusMessage STATUS_MESSAGE_A = new StatusMessage("statusMessageA");
    private static final StatusMessage STATUS_MESSAGE_B = new StatusMessage( "statusMessageB");
    private static final Url PROFILE_URL_A = new Url("http://www.google.com");
    private static final Url PROFILE_URL_B = new Url( "http://www.gmail.com");

    public MockUserService(final AbstractTopicMessagePublisher aMessagePublisher,
                           final AbstractUserRepository aProfileRepository,
                           final AbstractUserPresenceService aPresenceService) {
        super(aMessagePublisher, aProfileRepository, aPresenceService);
    }

    @Override
    public CompletableFuture<Void> open() {
        try {
            final User profileA = new User.Builder()
                    .setAppId(new ApplicationId(APP_ID))
                    .setUserId(USER_A)
                    .setNick(NICK_A)
                    .setStatusMessage(STATUS_MESSAGE_A)
                    .setProfileUrl(PROFILE_URL_A)
                    .build();
            final User profileB = new User.Builder()
                    .setAppId(new ApplicationId(APP_ID))
                    .setUserId(USER_B)
                    .setNick(NICK_B)
                    .setStatusMessage(STATUS_MESSAGE_B)
                    .setProfileUrl(PROFILE_URL_B)
                    .build();

            return saveUser(profileA)
                    .thenCompose(v -> saveUser(profileB))
                    .thenApply(v -> null);
        } catch (DomainErrorException ignored) { }

        return super.open();
    }
}
