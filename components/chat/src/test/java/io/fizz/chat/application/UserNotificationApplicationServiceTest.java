package io.fizz.chat.application;

import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.topic.TopicName;
import io.fizz.chat.pubsub.infrastructure.messaging.MockTopicMessagePublisher;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

public class UserNotificationApplicationServiceTest {
    @Test
    @DisplayName("it should not create an invalid service instance")
    void invalidCreationTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new UserNotificationApplicationService(null)
        );
    }

    @Test
    @DisplayName("it should not build a topic with invalid parameters")
    void invalidTopicBuildTest() throws Exception {
        final ApplicationId appId = new ApplicationId("appA");
        final UserId userId = new UserId("userA");
        final AbstractTopicMessagePublisher publisher = new MockTopicMessagePublisher();
        final UserNotificationApplicationService service = new UserNotificationApplicationService(publisher);

        Assertions.assertThrows(IllegalArgumentException.class, () -> service.build(null, userId));
        Assertions.assertThrows(IllegalArgumentException.class, () -> service.build(appId, (UserId)null));
    }

    @Test
    @DisplayName("it should build a valid topic name")
    void validTopicBuildTest() throws Exception {
        final ApplicationId appId = new ApplicationId("appA");
        final UserId userId = new UserId("userA");
        final AbstractTopicMessagePublisher publisher = new MockTopicMessagePublisher();
        final UserNotificationApplicationService service = new UserNotificationApplicationService(publisher);
        final TopicName topic = service.build(appId, userId);

        Assertions.assertEquals(
                topic.value('/'),
                UserNotificationApplicationService.NAMESPACE + "/" + appId.value() + "/" + userId.value()
        );
    }

    @Test
    @DisplayName("it should not build a topic list with invalid parameters")
    void invalidTopicListBuildTest() throws Exception {
        final ApplicationId appId = new ApplicationId("appA");
        final UserId userId = new UserId("userA");
        final Set<UserId> userIds = Collections.singleton(userId);
        final Set<UserId> emptyUserIds = Collections.singleton(null);
        final AbstractTopicMessagePublisher publisher = new MockTopicMessagePublisher();
        final UserNotificationApplicationService service = new UserNotificationApplicationService(publisher);

        Assertions.assertThrows(IllegalArgumentException.class, () -> service.build(null, userIds));
        Assertions.assertThrows(IllegalArgumentException.class, () -> service.build(appId, (Set<UserId>)null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> service.build(appId, emptyUserIds));
    }
    @Test
    @DisplayName("it should build a valid list of topics")
    void topicListBuildTest() throws Exception {
        final ApplicationId appId = new ApplicationId("appA");
        final UserId userA = new UserId("userA");
        final UserId userB = new UserId("userB");
        final Set<UserId> userIds = new HashSet<>(Arrays.asList(userA, userB));
        final AbstractTopicMessagePublisher publisher = new MockTopicMessagePublisher();
        final UserNotificationApplicationService service = new UserNotificationApplicationService(publisher);
        final Set<TopicName> topics = service.build(appId, userIds);

        Assertions.assertEquals(topics.size(), userIds.size());
    }
}
