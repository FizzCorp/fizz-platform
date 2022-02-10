package io.fizz.chat.group.infrastructure.bootstrap;

import io.fizz.chat.application.UserNotificationApplicationService;
import io.fizz.chat.application.channel.ChannelApplicationService;
import io.fizz.chat.group.application.group.GroupApplicationService;
import io.fizz.chat.group.application.projection.UserGroupProjection;
import io.fizz.chat.group.application.query.AbstractUserGroupQueryService;
import io.fizz.chat.group.application.user.UserRepository;
import io.fizz.chat.group.domain.group.*;
import io.fizz.chat.group.infrastructure.ConfigService;
import io.fizz.chat.group.infrastructure.persistence.HBaseGroupRepository;
import io.fizz.chat.group.infrastructure.persistence.HBaseUserGroupService;
import io.fizz.chat.group.infrastructure.serde.GsonGroupNotificationSerde;
import io.fizz.chataccess.application.AuthorizationService;
import io.fizz.chataccess.domain.AbstractAuthorizationService;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.domain.events.AbstractDomainEventBus;
import io.fizz.chatcommon.domain.events.AbstractEventPublisher;
import io.fizz.chatcommon.domain.events.DomainEventBus;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.messaging.kafka.KafkaEventPublisher;
import io.fizz.chatcommon.infrastructure.serde.AbstractEventSerde;
import io.fizz.chatcommon.infrastructure.serde.JsonEventSerde;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;

public class ChatGroupComponent {
    private static final String KAFKA_TOPIC = ConfigService.config().getString("chat.group.kafka.topic");
    private static final String KAFKA_GROUP = ConfigService.config().getString("chat.group.kafka.group");

    private GroupApplicationService groupService;
    private AbstractUserGroupQueryService userGroupQueryService;
    protected AbstractDomainEventBus eventBus;
    protected final AbstractEventSerde serde = new JsonEventSerde();

    public CompletableFuture<Void> open(final Vertx aVertx,
                                        final AuthorizationService aAuthService,
                                        final ChannelApplicationService aChannelService,
                                        final UserNotificationApplicationService aNotificationService,
                                        final RoleName aMutesRole,
                                        final AbstractHBaseClient aClient,
                                        final String aKafkaServers) {
        createEventBus(aVertx, aKafkaServers);

        createGroupServices(aAuthService, aChannelService, aMutesRole, aClient);

        eventBus.addListener(
            new UserGroupProjection(userGroupQueryService, aNotificationService, new GsonGroupNotificationSerde())
        );

        return CompletableFuture.completedFuture(null);
    }

    public GroupApplicationService groupService() {
        return groupService;
    }

    public AbstractUserGroupQueryService userGroupQueryService() {
        return userGroupQueryService;
    }

    private void createEventBus(final Vertx aVertx, final String aKafkaServers) {
        eventBus = new DomainEventBus();

        eventBus.register(createPublisher(aVertx, aKafkaServers));
    }

    protected AbstractEventPublisher createPublisher(final Vertx aVertx, final String aKafkaServers) {
        return new KafkaEventPublisher(
                aVertx,
                new JsonEventSerde(),
                new DomainEventType[]{
                        GroupMemberUpdated.TYPE,
                        GroupMemberAdded.TYPE,
                        GroupMemberRemoved.TYPE,
                        GroupProfileUpdated.TYPE,
                        GroupMemberReadMessages.TYPE
                },
                aKafkaServers,
                KAFKA_TOPIC,
                KAFKA_GROUP
        );
    }

    protected void createGroupServices(final AbstractAuthorizationService aAuthService,
                                     final ChannelApplicationService aChannelService,
                                     final RoleName aMutesRole,
                                     final AbstractHBaseClient aClient) {
        groupService = new GroupApplicationService(
                aAuthService,
                new UserRepository(aAuthService),
                new HBaseGroupRepository(aClient),
                aChannelService,
                aMutesRole
        );

        userGroupQueryService = new HBaseUserGroupService(aClient);
    }
}
