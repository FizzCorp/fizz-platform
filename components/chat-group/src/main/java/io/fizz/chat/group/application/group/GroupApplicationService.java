package io.fizz.chat.group.application.group;

import io.fizz.chat.application.channel.*;
import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.chat.group.domain.group.AbstractGroupRepository;
import io.fizz.chat.group.domain.group.Group;
import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.application.user.UserRepository;
import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.chat.group.domain.user.User;
import io.fizz.chataccess.domain.AbstractAuthorizationService;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;
import io.fizz.command.bus.CommandHandler;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GroupApplicationService {
    protected static final LoggingService.Log logger = LoggingService.getLogger(GroupApplicationService.class);

    private final AbstractAuthorizationService authService;
    private final UserRepository userRepo;
    private final AbstractGroupRepository groupRepo;
    private final ChannelApplicationService channelService;
    private final RoleName muteRole;

    public GroupApplicationService(final AbstractAuthorizationService aAuthService,
                                   final UserRepository aUserRepo,
                                   final AbstractGroupRepository aGroupRepo,
                                   final ChannelApplicationService aChannelService,
                                   final RoleName aMuteRole) {
        Utils.assertRequiredArgument(aAuthService, "invalid auth service");
        Utils.assertRequiredArgument(aUserRepo, "invalid_user_repo");
        Utils.assertRequiredArgument(aGroupRepo, "invalid_group_repo");
        Utils.assertRequiredArgument(aChannelService, "invalid_channel_service");
        Utils.assertRequiredArgument(aMuteRole, "invalid mute role specified");

        this.authService = aAuthService;
        this.userRepo = aUserRepo;
        this.groupRepo = aGroupRepo;
        this.channelService = aChannelService;
        this.muteRole = aMuteRole;
    }

    public AbstractGroupRepository groupRepo() {
        return groupRepo;
    }

    @CommandHandler
    public CompletableFuture<GroupDTO> handle(final CreateGroupCommand aCmd) {
        Utils.assertRequiredArgument(aCmd, "invalid command");

        CompletableFuture<User> creatorFetched = userRepo.get(aCmd.groupId().appId(), aCmd.profile().createdBy());

        CompletableFuture<Group> groupCreated = creatorFetched.thenApply(aCreator ->
            aCreator.create(aCmd.groupId(), aCmd.profile(), aCmd.members())
        );

        CompletableFuture<Group> saved = groupCreated.thenCompose(this::saveGroup);

        return saved.thenApply(GroupDTO::new);
    }

    public CompletableFuture<GroupDTO> fetch(final GroupId aGroupId) {
        Utils.assertRequiredArgument(aGroupId, "invalid_group_id");

        return groupRepo.get(aGroupId)
                .thenApply(GroupDTO::new);
    }

    @CommandHandler
    public CompletableFuture<Boolean> handle(final UpdateGroupProfileCommand aCmd) {
        Utils.assertRequiredArgument(aCmd, "invalid command");

        CompletableFuture<Group> groupFetched = fetchGroup(aCmd.groupId());

        CompletableFuture<User> operatorFetched = userRepo.get(aCmd.operatorId(), aCmd.groupId());

        CompletableFuture<Group> profileUpdated = groupFetched.thenCombine(
                operatorFetched,
                (aGroup, aOperator) -> {
                    aGroup.updateProfile(
                        aOperator, aCmd.newTitle(), aCmd.newImageURL(), aCmd.newDescription(), aCmd.newType()
                    );

                    return aGroup;
                }
        );

        return profileUpdated.thenCompose(groupRepo::put);
    }

    @CommandHandler
    public CompletableFuture<Boolean> handle(final AddGroupMembersCommand aCmd) {
        Utils.assertRequiredArgument(aCmd, "invalid command");

        CompletableFuture<Group> groupFetched = fetchGroup(aCmd.groupId());

        CompletableFuture<User> operatorFetched = userRepo.get(aCmd.operatorId(), aCmd.groupId());

        CompletableFuture<Group> added = groupFetched.thenCombine(
                operatorFetched,
                (aGroup, aOperator) -> {
                   for (GroupMemberData info: aCmd.members()) {
                       aGroup.add(aOperator, info.id(), info.role(), info.state());
                   }

                   return aGroup;
               }
        );

        return added.thenCompose(groupRepo::put);
    }

    @CommandHandler
    public CompletableFuture<Boolean> handle(final RemoveGroupMembersCommand aCmd) {
        Utils.assertRequiredArgument(aCmd, "invalid command");

        CompletableFuture<Group> groupFetched = fetchGroup(aCmd.groupId());

        CompletableFuture<User> operatorFetched = userRepo.get(aCmd.operatorId(), aCmd.groupId());

        CompletableFuture<Group> removed = groupFetched.thenCombine(
                operatorFetched,
                (aGroup, aOperator) -> {
                    for (UserId memberId: aCmd.members()) {
                        aGroup.remove(aOperator, memberId);
                    }

                    return aGroup;
                }
        );

        return removed.thenCompose(groupRepo::put);
    }

    @CommandHandler
    public CompletableFuture<Boolean> handle(final UpdateGroupMemberCommand aCmd) {
        Utils.assertRequiredArgument(aCmd, "invalid_command");

        CompletableFuture<Group> fetched = fetchGroup(aCmd.groupId());

        CompletableFuture<User> operatorFetched = userRepo.get(aCmd.operatorId(), aCmd.groupId());

        CompletableFuture<Group> updated = fetched.thenCombine(
                operatorFetched,
                (aGroup, aOperator) -> {
                    if (Objects.nonNull(aCmd.newState())) {
                        aGroup.changeMemberState(aCmd.memberId(), aCmd.newState());
                    }

                    if (Objects.nonNull(aCmd.newRole())) {
                        aGroup.changeMemberRole(aOperator, aCmd.memberId(), aCmd.newRole());
                    }

                    if (Objects.nonNull(aCmd.lastReadMessageId())) {
                        aGroup.updateLastReadMessage(aCmd.memberId(), aCmd.lastReadMessageId());
                    }

                    return aGroup;
                }
        );

        return updated.thenCompose(groupRepo::put);
    }

    @CommandHandler
    public CompletableFuture<Void> handle(final PublishGroupMessageCommand aCmd) {
        Utils.assertRequiredArgument(aCmd, "invalid command");

        CompletableFuture<Group> groupFetched = fetchGroup(aCmd.groupId());

        CompletableFuture<User> authorFetched = userRepo.get(aCmd.authorId(), aCmd.groupId());

        CompletableFuture<Group> published = groupFetched.thenCombine(
                authorFetched,
                (aGroup, aAuthor) -> {
                    aGroup.publishMessage(aAuthor);

                    return aGroup;
                });

        return published.thenCompose(aGroup -> {
                Set<UserId> notifyList = new HashSet<>();
                for (GroupMember member: aGroup.members()) {
                    if (member.state() == GroupMember.State.JOINED) {
                        notifyList.add(member.userId());
                    }
                }

                final ChannelMessagePublishCommand cmd = new ChannelMessagePublishCommand(
                        aGroup.channelId(),
                        aCmd.authorId().value(),
                        aCmd.nick(),
                        aCmd.body(),
                        aCmd.data(),
                        aCmd.locale(),
                        aCmd.isTranslate(),
                        aCmd.isFilter(),
                        aCmd.isPersist(),
                        notifyList,
                        aCmd.isInternal()
                );

                return channelService.publish(cmd);
        });
    }

    public CompletableFuture<List<ChannelMessage>> queryMessages(final GroupMessagesQuery aQuery) {
        Utils.assertRequiredArgument(aQuery, "invalid messages query");

        CompletableFuture<Group> groupFetched = fetchGroup(aQuery.groupId());

        CompletableFuture<User> requesterFetched = userRepo.get(aQuery.requesterId(), aQuery.groupId());

        CompletableFuture<Group> queried = groupFetched.thenCombine(
                requesterFetched,
                (aGroup, aRequester) -> {
                   aGroup.queryMessages(aRequester);

                   return aGroup;
                }
        );

        return queried.thenCompose(aGroup -> {
            ChannelHistoryQuery query = new ChannelHistoryQuery(
                    aGroup.channelId(),
                    aQuery.requesterId().value(),
                    aQuery.count(),
                    aQuery.before(),
                    aQuery.after()
            );

            return channelService.queryMessages(query);
        });
    }

    @CommandHandler
    public CompletableFuture<Boolean> handle(final RemoveUserGroupsCommand aCmd) {
        Utils.assertRequiredArgument(aCmd, "invalid command");

        final CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
        final List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (GroupId groupId : aCmd.groupIds()) {
            futures.add(removeUserGroup(groupId, aCmd.operatorId(), aCmd.userId()));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .handle((v, error) -> {
                    try {
                        if (Objects.nonNull(error)) {
                            logger.error(error.getMessage());
                            resultFuture.complete(false);
                            return null;
                        }
                        boolean result = true;
                        for (CompletableFuture<Boolean> future : futures) {
                            result &= !future.isCompletedExceptionally();
                        }

                        resultFuture.complete(result);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage());
                        resultFuture.complete(false);
                    }
                    return null;
                });
        return resultFuture;
    }

    private CompletableFuture<Boolean> removeUserGroup(final GroupId aGroupId,
                                                       final UserId aOperatorId,
                                                       final UserId aUserId) {
        Utils.assertRequiredArgument(aGroupId, "invalid groupId");
        Utils.assertRequiredArgument(aOperatorId, "invalid operatorId");
        Utils.assertRequiredArgument(aUserId, "invalid userId");

        CompletableFuture<Group> groupFetched = fetchGroup(aGroupId);

        CompletableFuture<User> operatorFetched = userRepo.get(aOperatorId, aGroupId);

        CompletableFuture<Group> removed = groupFetched.thenCombine(
                operatorFetched,
                (aGroup, aOperator) -> {
                    aGroup.remove(aOperator, aUserId);

                    return aGroup;
                }
        );

        return removed.thenCompose(groupRepo::put);
    }

    public CompletableFuture<Void> muteUser(final GroupId aGroupId,
                                            final UserId aModerator,
                                            final UserId aMutedUser,
                                            final Date aEnds) {
        try {
            CompletableFuture<Group> groupFetched = fetchGroup(aGroupId);
            CompletableFuture<User> moderatorFetched = userRepo.get(aModerator, aGroupId);
            CompletableFuture<User> mutedUserFetched = userRepo.get(aMutedUser, aGroupId);

            CompletableFuture<Group> userMuted = groupFetched.thenCompose(
                    aGroup ->
                            moderatorFetched.thenCombine(mutedUserFetched, (moderatorUser, mutedUser) -> {
                                aGroup.canManageMutes(moderatorUser, mutedUser);
                                return aGroup;
                            })
            );

            return userMuted.thenCompose(
                    aGroup -> authService.assignContextRole(aGroupId.authContextId(), aMutedUser, muteRole, aEnds)
            );
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<Void> unmuteUser(final GroupId aGroupId,
                                              final UserId aModerator,
                                              final UserId aMutedUser) {
        try {
            CompletableFuture<Group> groupFetched = fetchGroup(aGroupId);
            CompletableFuture<User> moderatorFetched = userRepo.get(aModerator, aGroupId);
            CompletableFuture<User> mutedUserFetched = userRepo.get(aMutedUser, aGroupId);

            CompletableFuture<Group> userUnMuted = groupFetched.thenCompose(
                    aGroup ->
                            moderatorFetched.thenCombine(mutedUserFetched, (moderatorUser, mutedUser) -> {
                                aGroup.canManageMutes(moderatorUser, mutedUser);
                                return aGroup;
                            })
            );

            return userUnMuted.thenCompose(
                    aGroup ->authService.removeContextRole(aGroupId.authContextId(), aMutedUser, muteRole)
            );
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    private CompletableFuture<Group> saveGroup(final Group aGroup) {
        return groupRepo.put(aGroup).thenApply(aCreated -> {
            if (!aCreated) {
                throw new IllegalStateException("already_exists");
            }

            return aGroup;
        });
    }

    private CompletableFuture<Group> fetchGroup(final GroupId aGroupId) {
        Utils.assertRequiredArgument(aGroupId, "invalid_group_id");

        return groupRepo.get(aGroupId).thenCompose(aGroup -> {
            if (Objects.isNull(aGroup)) {
                return Utils.failedFuture(new IllegalStateException("missing_group"));
            }

            return CompletableFuture.completedFuture(aGroup);
        });
    }
}
