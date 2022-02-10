package io.fizz.chat.infrastructure.services;

import io.fizz.chat.application.AbstractApplicationRepository;
import io.fizz.chat.application.FCMConfiguration;
import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.application.impl.HBaseApplicationRepository;
import io.fizz.chat.application.impl.MockApplicationService;
import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chat.user.application.UserDTO;
import io.fizz.chat.user.application.repository.AbstractUserRepository;
import io.fizz.chat.user.application.service.AbstractUserApplicationService;
import io.fizz.chat.user.domain.User;
import io.fizz.chat.user.infrastructure.persistence.HBaseUserRepository;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;

@Disabled
public class FCMPushNotificationServiceTest {
    @Test
    void driver() throws Throwable {
        final ApplicationId appId = new ApplicationId("appA");
        final AbstractHBaseClient client = new MockHBaseClient();
        final AbstractUserRepository userRepo = new HBaseUserRepository(client);
        final AbstractApplicationRepository appRepo = new HBaseApplicationRepository(client);

        FCMConfiguration fcmConfig = new FCMConfiguration("appA", "AAAAQAPsSYM:APA91bEZtTfMcdKnht5-Wc8G6vWXv5LsOEUiRMSh6OQg0UzgbnscqXJRVNeqTOHM8-zisr1eBnFD_rxQRf82Ws1HnwuotrfKUVyxK_7V1wWHPpoEXMD7rRhptdzekEcmfv3sE0IDbSH6");

        appRepo.put(appId, fcmConfig).get();

        final User user = new User.Builder()
                .setAppId(appId)
                .setUserId(new UserId("userA"))
                .build();

        user.setToken("cs");
        user.setToken("dDwsC9TBRBGUhmDkfkoX6E:APA91bF4Sril1KjqV7CrumVuT04Zuoln_FSiCZ-6CIsYyCEQDWw5ZJij6cd_eKRSvQp1ruV4ge0loJZoOYYeCe7mYoOvRXy6VdNVcvqw2pPzqIl-f5-YhAmRWZLYfxEPR9gEbaneFAtn");
        userRepo.put(user).get();

        final AbstractUserApplicationService userService =
                (aAppId, aUserId) ->
                        userRepo.get(aAppId, aUserId)
                        .thenApply(
                                aUser -> new UserDTO(
                                        aUser.userId(),
                                        aUser.nick(),
                                        aUser.statusMessage(),
                                        aUser.profileUrl(),
                                        false,
                                        aUser.token()
                                )
                        );

        final ApplicationService appService = new MockApplicationService(appRepo);
        final FCMPushNotificationService service = new FCMPushNotificationService(Vertx.vertx(), appService, userService);
        final ChannelMessage message = new ChannelMessage(
                0,
                new UserId("jim"),
                "jimbo",
                new ChannelId(appId, "guild"),
                new TopicId("guild"),
                "come back man!!!",
                null,
                null,
                new Date(),
                new Date(),
                false,
                null
        );

        service.send(new ApplicationId("appA"), message, Collections.singleton(user.userId())).get();
    }
}
