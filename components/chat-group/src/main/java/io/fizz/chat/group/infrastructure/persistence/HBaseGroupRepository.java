package io.fizz.chat.group.infrastructure.persistence;

import com.google.protobuf.ByteString;
import io.fizz.chat.group.domain.group.*;
import io.fizz.chat.group.infrastructure.ConfigService;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.domain.EventsOffset;
import io.fizz.chatcommon.domain.MutationId;
import io.fizz.chatcommon.infrastructure.HBaseDomainAggregateRepository;
import io.fizz.client.hbase.HBaseClientModels;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import org.apache.hadoop.hbase.util.Bytes;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class HBaseGroupRepository implements AbstractGroupRepository {
    private static final LoggingService.Log log = LoggingService.getLogger(HBaseGroupRepository.class);

    private static final String CHAT_GROUP_NAMESPACE = ConfigService.config().getString("chat.group.hbase.namespace");
    private static final ByteString NULL_VALUE = ByteString.copyFrom(new byte[]{});
    private static final ByteString NS_NAME = ByteString.copyFrom(Bytes.toBytes(CHAT_GROUP_NAMESPACE));
    private static final ByteString TABLE_NAME = ByteString.copyFrom(Bytes.toBytes("tbl_group"));
    private static final ByteString CF_CONTENT = ByteString.copyFrom(Bytes.toBytes("c"));
    private static final ByteString COL_GROUP = ByteString.copyFrom(Bytes.toBytes("g"));
    private static final ByteString COL_GROUP_TYPE = ByteString.copyFrom(Bytes.toBytes("gt"));
    private static final ByteString COL_EVENTS_OFFSET = ByteString.copyFrom(Bytes.toBytes("geo"));
    private static final ByteString COL_VERSION = ByteString.copyFrom(Bytes.toBytes("gv"));

    private static final HBaseClientModels.Table TABLE = HBaseClientModels.Table.newBuilder()
                                                        .setNamespace(NS_NAME)
                                                        .setName(TABLE_NAME)
                                                        .build();

    private final AbstractHBaseClient client;

    public HBaseGroupRepository(final AbstractHBaseClient aClient) {
        Utils.assertRequiredArgument(aClient, "invalid hbase client");

        this.client = aClient;
    }

    @Override
    public GroupId identity(final ApplicationId aAppId) {
        Utils.assertRequiredArgument(aAppId, "invalid_app_id");

        final String random = UUID.randomUUID().toString().replaceAll("-", "");

        return new GroupId(aAppId, random);
    }

    @Override
    public CompletableFuture<Boolean> put(final Group aGroup) {
        final HBaseClientModels.Put put = makePut(aGroup);

        return client.put(put);
    }

    @Override
    public CompletableFuture<Group> get(final GroupId aGroupId) {
        final HBaseClientModels.Get get = makeGet(aGroupId);

        return client.get(get)
                .thenApply(aResult -> {
                    ByteString groupBuffer = null;
                    MutationId mutationId = null;
                    EventsOffset eventsOffset = null;

                    for (int ci = 0; ci < aResult.getColumnsCount(); ci++) {
                        final HBaseClientModels.ColumnValue col = aResult.getColumns(ci);

                        if (col.getKey().getQualifier().equals(COL_GROUP)) {
                            groupBuffer = col.getValue();
                        }
                        else
                        if (col.getKey().getQualifier().equals(COL_VERSION)) {
                            mutationId = new MutationId(Bytes.toInt(col.getValue().toByteArray()));
                        }
                        else
                        if (col.getKey().getQualifier().equals(COL_EVENTS_OFFSET)) {
                            eventsOffset = new EventsOffset(Bytes.toLong(col.getValue().toByteArray()));
                        }
                    }

                    return deserialize(groupBuffer, mutationId, eventsOffset);
                });
    }

    private HBaseClientModels.Put makePut(final Group aGroup) {
        final HBaseClientModels.Put.Builder builder = HBaseClientModels.Put.newBuilder();
        final MutationId mutationId = aGroup.mutationId();
        final ByteString rowKey = makeRowKey(aGroup.id());
        final ByteString newValue = serialize(aGroup);

        builder.setTable(TABLE);
        builder.setRowKey(rowKey);
        builder.addColumns(buildColumnValue(COL_GROUP, newValue));
        if (Objects.nonNull(aGroup.type())) {
            final ByteString newType = ByteString.copyFrom(aGroup.type(), StandardCharsets.UTF_8);
            builder.addColumns(buildColumnValue(COL_GROUP_TYPE, newType));
        }

        ByteString newVersionBytes = ByteString.copyFrom(Bytes.toBytes(mutationId.next()));
        builder.addColumns(buildColumnValue(COL_VERSION, newVersionBytes));

        long newEventsOffset = HBaseDomainAggregateRepository.serialize(aGroup, builder, aGroup.eventsOffset());
        ByteString newEventsOffsetBytes = ByteString.copyFrom(Bytes.toBytes(newEventsOffset));
        builder.addColumns(buildColumnValue(COL_EVENTS_OFFSET, newEventsOffsetBytes));

        if (mutationId.isNull()) {
            builder.setCondition(buildColumnValue(COL_VERSION, NULL_VALUE));
        }
        else {
            final ByteString preVersion = ByteString.copyFrom(Bytes.toBytes(mutationId.value()));
            builder.setCondition(buildColumnValue(COL_VERSION, preVersion));
        }

        return builder.build();
    }

    private static HBaseClientModels.Get makeGet(final GroupId aGroupId) {
        final ByteString rowKey = makeRowKey(aGroupId);
        return HBaseClientModels.Get.newBuilder()
                .setTable(TABLE)
                .setRowKey(rowKey)
                .addColumns(buildColumnCoord(COL_GROUP))
                .addColumns(buildColumnCoord(COL_VERSION))
                .addColumns(buildColumnCoord(COL_EVENTS_OFFSET))
                .build();
    }

    private static ByteString makeRowKey(final GroupId aGroupId) {
        return ByteString.copyFrom(aGroupId.qualifiedValue(), StandardCharsets.UTF_8);
    }

    private static HBaseClientModels.ColumnValue buildColumnValue(final ByteString aQualifier, final ByteString aValue) {
        return HBaseClientModels.ColumnValue.newBuilder()
                .setKey(buildColumnCoord(aQualifier))
                .setValue(aValue)
                .build();
    }

    private static HBaseClientModels.ColumnCoord buildColumnCoord(final ByteString aQualifier) {
        return HBaseClientModels.ColumnCoord.newBuilder()
                .setFamily(CF_CONTENT)
                .setQualifier(aQualifier)
                .build();
    }

    private static ByteString serialize(final Group aGroup) {
        Models.GroupModel.Builder builder = Models.GroupModel.newBuilder();

        builder.setAppId(aGroup.id().appId().value());
        builder.setGroupId(aGroup.id().value());
        builder.setProfile(serialize(aGroup.profile()));

        for (final GroupMember member: aGroup.members()) {
            builder.addMembers(serialise(member));
        }

        builder.setCreatedOn(aGroup.createdOn().getTime());
        builder.setUpdatedOn(aGroup.updatedOn().getTime());

        return builder.build().toByteString();
    }

    private static Models.GroupProfileModel serialize(final GroupProfile aProfile) {
        Models.GroupProfileModel.Builder builder = Models.GroupProfileModel.newBuilder();

        if (Objects.nonNull(aProfile.title())) {
            builder.setTitle(aProfile.title());
        }

        if (Objects.nonNull(aProfile.imageURL())) {
            builder.setImageURL(aProfile.imageURL());
        }

        if (Objects.nonNull(aProfile.description())) {
            builder.setDescription(aProfile.description());
        }

        if (Objects.nonNull(aProfile.type())) {
            builder.setType(aProfile.type());
        }

        builder.setCreatedBy(aProfile.createdBy().value());

        return builder.build();
    }

    private static Models.GroupMemberModel serialise(final GroupMember aMember) {
        return Models.GroupMemberModel.newBuilder()
                .setUserId(aMember.userId().value())
                .setRole(aMember.role().value())
                .setState(aMember.state().value())
                .setCreatedOn(aMember.createdOn().getTime())
                .build();
    }

    private static Group deserialize(final ByteString aBytes,
                                     final MutationId aMutationId,
                                     final EventsOffset aOffset) {
        if (Objects.isNull(aBytes) || Objects.isNull(aMutationId)) {
            log.error("invalid group persistence encountered");
            return null;
        }

        try {
            final Models.GroupModel model = Models.GroupModel.parseFrom(aBytes);
            final ApplicationId appId = new ApplicationId(model.getAppId());
            final GroupId groupId = new GroupId(appId, model.getGroupId());
            final GroupProfile profile = deserialize(model.getProfile());

            final Set<GroupMember> members = new HashSet<>();
            for (int mi = 0; mi < model.getMembersCount(); mi++) {
                final GroupMember member = deserialize(model.getMembers(mi), groupId);
                members.add(member);
            }

            return new Group(
                    groupId,
                    profile,
                    members,
                    new Date(model.getCreatedOn()),
                    new Date(model.getUpdatedOn()),
                    aMutationId,
                    aOffset
            );
        }
        catch (Exception ex) {
            log.fatal(ex.getMessage());
            return null;
        }
    }

    private static GroupProfile deserialize(final Models.GroupProfileModel aModel) {
        return new GroupProfile(
                new UserId(aModel.getCreatedBy()),
                aModel.getTitle(),
                aModel.getImageURL(),
                aModel.getDescription(),
                aModel.getType()
        );
    }

    private static GroupMember deserialize(final Models.GroupMemberModel aModel, final GroupId aGroupId) {
        return new GroupMember(
                new UserId(aModel.getUserId()),
                aGroupId,
                GroupMember.State.fromValue(aModel.getState()),
                new RoleName(aModel.getRole()),
                new Date(aModel.getCreatedOn())
        );
    }
}
