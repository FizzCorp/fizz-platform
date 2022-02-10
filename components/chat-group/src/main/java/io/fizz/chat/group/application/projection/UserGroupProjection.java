package io.fizz.chat.group.application.projection;

import io.fizz.chat.application.UserNotificationApplicationService;
import io.fizz.chat.group.application.notifications.AbstractGroupNotificationSerde;
import io.fizz.chat.group.application.notifications.GroupMemberNotification;
import io.fizz.chat.group.application.notifications.GroupNotification;
import io.fizz.chat.group.application.notifications.GroupProfileNotification;
import io.fizz.chat.group.application.query.AbstractUserGroupQueryService;
import io.fizz.chat.group.domain.group.*;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;
import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.AbstractEventListener;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UserGroupProjection implements AbstractEventListener {
    private static final LoggingService.Log log = LoggingService.getLogger(UserGroupProjection.class);

    private final AbstractUserGroupQueryService queryService;
    private final UserNotificationApplicationService notificationService;
    private final AbstractGroupNotificationSerde notificationSerde;

    public UserGroupProjection(final AbstractUserGroupQueryService aQueryService,
                               final UserNotificationApplicationService aNotificationService,
                               final AbstractGroupNotificationSerde aNotificationSerde) {
        Utils.assertRequiredArgument(aQueryService, "invalid query service");
        Utils.assertRequiredArgument(aNotificationService, "invalid notification service");
        Utils.assertRequiredArgument(aNotificationSerde, "invalid notification serde");

        this.queryService = aQueryService;
        this.notificationService = aNotificationService;
        this.notificationSerde = aNotificationSerde;
    }

    @Override
    public CompletableFuture<Void> handleEvent(AbstractDomainEvent aEvent) {
        if (aEvent.type().equals(GroupMemberAdded.TYPE)) {
            return on((GroupMemberAdded)aEvent);
        }
        else
        if (aEvent.type().equals(GroupMemberRemoved.TYPE)) {
            return on((GroupMemberRemoved)aEvent);
        }
        else
        if (aEvent.type().equals(GroupMemberUpdated.TYPE)) {
            return on((GroupMemberUpdated)aEvent);
        }
        else
        if (aEvent.type().equals(GroupProfileUpdated.TYPE)) {
            return on((GroupProfileUpdated)aEvent);
        }
        else
        if (aEvent.type().equals(GroupMemberReadMessages.TYPE)) {
            return on((GroupMemberReadMessages)aEvent);
        }

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> on(final GroupMemberAdded aEvent) {
        return queryService.put(aEvent.memberId(), aEvent.groupId(), aEvent.state(), aEvent.role(), aEvent.createdOn())
                .thenCompose(aStatus -> {
                    final GroupMemberNotification notification = new GroupMemberNotification(
                            aEvent.groupId(),
                            aEvent.memberId(),
                            aEvent.state(),
                            aEvent.role()
                    );

                    return notify(notification, GroupNotification.Type.GROUP_MEMBER_ADDED, aEvent.memberIds());
                });
    }

    private CompletableFuture<Void> on(final GroupMemberUpdated aEvent) {
        return queryService.put(aEvent.memberId(), aEvent.groupId(), aEvent.state(), aEvent.role(), aEvent.createdOn())
                .thenCompose(aStatus -> {
                    final GroupMemberNotification notification = new GroupMemberNotification(
                            aEvent.groupId(),
                            aEvent.memberId(),
                            aEvent.state(),
                            aEvent.role()
                    );

                    return notify(notification, GroupNotification.Type.GROUP_MEMBER_UPDATED, aEvent.memberIds());
                });
    }

    private CompletableFuture<Void> on(final GroupMemberReadMessages aEvent) {
        return queryService.update(aEvent.memberId(), aEvent.groupId(), aEvent.lastReadMessageId());
    }

    private CompletableFuture<Void> on(final GroupMemberRemoved aEvent) {
        return queryService.delete(aEvent.memberId(), aEvent.groupId())
                .thenCompose(aStatus -> {
                    final GroupMemberNotification notification = new GroupMemberNotification(
                            aEvent.groupId(),
                            aEvent.memberId(),
                            aEvent.state(),
                            aEvent.role()
                    );

                    return notify(notification, GroupNotification.Type.GROUP_MEMBER_REMOVED, aEvent.memberIds());
                });
    }

    private CompletableFuture<Void> on(final GroupProfileUpdated aEvent) {
        final GroupProfileNotification notification = new GroupProfileNotification(
                aEvent.groupId(), aEvent.title(), aEvent.imageURL(), aEvent.description(), aEvent.groupType()
        );

        return notify(notification, aEvent.members());
    }

    @Override
    public DomainEventType[] listensTo() {
        return new DomainEventType[] {
                GroupProfileUpdated.TYPE,
                GroupMemberAdded.TYPE,
                GroupMemberRemoved.TYPE,
                GroupMemberUpdated.TYPE,
                GroupMemberReadMessages.TYPE
        };
    }

    private CompletableFuture<Void> notify(final GroupMemberNotification aNotification,
                                           final GroupNotification.Type aType,
                                           final Set<UserId> aMemberIds) {
        final TopicMessage message = build(
            aType.value(), aNotification.groupId(), notificationSerde.serialize(aNotification)
        );

        return notificationService.publish(aNotification.appId(), new HashSet<>(aMemberIds), message)
                .whenComplete((aVoid, aError) -> {
                    if (Objects.nonNull(aError)) {
                        log.fatal(aError);
                    }
                });
    }

    private CompletableFuture<Void> notify(final GroupProfileNotification aNotification,
                                           final Set<UserId> aMemberIds) {
        final TopicMessage message = build(
                GroupNotification.Type.GROUP_UPDATED.value(),
                aNotification.groupId(),
                notificationSerde.serialize(aNotification)
        );

        return notificationService.publish(aNotification.appId(), new HashSet<>(aMemberIds), message)
                .whenComplete((aVoid, aError) -> {
                    if (Objects.nonNull(aError)) {
                        log.fatal(aError);
                    }
                });
    }

    private TopicMessage build(final String aEventType, final GroupId aGroupId, final String aPayload) {
        return new TopicMessage(0L, aEventType, aGroupId.value(), aPayload, new Date());
    }
}
