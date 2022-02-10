package io.fizz.chat.group.infrastructure.bootstrap;

import io.fizz.chat.application.channel.ChannelApplicationService;
import io.fizz.chat.group.domain.group.*;
import io.fizz.chataccess.domain.AbstractAuthorizationService;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.domain.events.AbstractEventPublisher;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.MockEventedHBaseClient;
import io.fizz.chatcommon.infrastructure.messaging.MockEventPublisher;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.vertx.core.Vertx;

public class MockChatGroupComponent extends ChatGroupComponent {
    @Override
    protected void createGroupServices(final AbstractAuthorizationService aAuthService,
                                       final ChannelApplicationService aChannelService,
                                       final RoleName aMutesRole,
                                       final AbstractHBaseClient aClient) {
        super.createGroupServices(aAuthService, aChannelService, aMutesRole, new MockEventedHBaseClient(eventBus, serde));
    }

    @Override
    protected AbstractEventPublisher createPublisher(Vertx aVertx, String aKafkaServers) {
        return new MockEventPublisher(
                serde,
                new DomainEventType[]{
                        GroupMemberUpdated.TYPE,
                        GroupMemberAdded.TYPE,
                        GroupMemberRemoved.TYPE,
                        GroupProfileUpdated.TYPE,
                        GroupMemberReadMessages.TYPE
                }
        );
    }
}
