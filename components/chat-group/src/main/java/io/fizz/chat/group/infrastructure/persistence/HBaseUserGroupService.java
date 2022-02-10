package io.fizz.chat.group.infrastructure.persistence;

import com.google.protobuf.ByteString;
import io.fizz.chat.group.application.query.AbstractUserGroupQueryService;
import io.fizz.chat.group.application.query.QueryResult;
import io.fizz.chat.group.application.query.UserGroupDTO;
import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.chat.group.infrastructure.ConfigService;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.client.hbase.HBaseClientModels;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import org.apache.hadoop.hbase.util.Bytes;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HBaseUserGroupService implements AbstractUserGroupQueryService {
    private static final String CHAT_GROUP_NAMESPACE = ConfigService.config().getString("chat.group.hbase.namespace");
    private static final ByteString NS_NAME = ByteString.copyFrom(Bytes.toBytes(CHAT_GROUP_NAMESPACE));
    private static final ByteString TABLE_NAME = ByteString.copyFrom(Bytes.toBytes("tbl_user_group"));
    private static final ByteString CF_CONTENT = ByteString.copyFrom(Bytes.toBytes("c"));
    private static final ByteString COL_GROUP_ID = ByteString.copyFrom(Bytes.toBytes("gid"));
    private static final ByteString COL_USER_ID = ByteString.copyFrom(Bytes.toBytes("uid"));
    private static final ByteString COL_MEMBER_STATE = ByteString.copyFrom(Bytes.toBytes("ms"));
    private static final ByteString COL_MEMBER_ROLE = ByteString.copyFrom(Bytes.toBytes("mr"));
    private static final ByteString COL_MEMBER_CREATED = ByteString.copyFrom(Bytes.toBytes("mc"));
    private static final ByteString COL_LAST_READ_MESSAGE_ID = ByteString.copyFrom("lmi", StandardCharsets.UTF_8);
    private static final byte KEY_DELIMITER = (byte)124;
    private static final byte KEY_CAP = (byte)125;

    private static final HBaseClientModels.Table TABLE = HBaseClientModels.Table.newBuilder()
            .setNamespace(NS_NAME)
            .setName(TABLE_NAME)
            .build();

    private final AbstractHBaseClient client;

    public HBaseUserGroupService(final AbstractHBaseClient aClient) {
        Utils.assertRequiredArgument(aClient, "invalid hbase client");

        this.client = aClient;
    }

    @Override
    public CompletableFuture<Void> put(final UserId aUserId,
                                       final GroupId aGroupId,
                                       final GroupMember.State aState,
                                       final RoleName aRole,
                                       final Date aCreatedOn) {
        return client.put(makePut(aUserId, aGroupId, aState, aRole, aCreatedOn))
                .thenApply(aStatus -> null);
    }

    @Override
    public CompletableFuture<Void> update(final UserId aUserId, final GroupId aGroupId, final Long aLastReadMessageId) {
        final HBaseClientModels.Put.Builder builder = HBaseClientModels.Put.newBuilder();

        builder.setTable(TABLE);
        builder.setRowKey(makeRowKey(aUserId, aGroupId));
        builder.addColumns(
                columnValue(COL_LAST_READ_MESSAGE_ID, ByteString.copyFrom(Bytes.toBytes(aLastReadMessageId)))
        );

        return client.put(builder.build())
                .thenApply(aStatus -> null);
    }

    @Override
    public CompletableFuture<Void> delete(final UserId aUserId, final GroupId aGroupId) {
        return client.delete(
                HBaseClientModels.Delete.newBuilder()
                .setTable(TABLE)
                .setRowKey(makeRowKey(aUserId, aGroupId))
                .build()
        );
    }

    @Override
    public CompletableFuture<QueryResult<UserGroupDTO>> query(final ApplicationId aAppId,
                                                              final UserId aUserId,
                                                              final String aPageCursor,
                                                              final int aPageSize) {
        final ByteString startKey = Objects.nonNull(aPageCursor) ?
                                    makeRowKey(aUserId, new GroupId(aAppId, aPageCursor)) :
                                    makeStartRowKey(aAppId, aUserId);
        final ByteString endKey = makeEndRowKey(aAppId, aUserId);
        int limit = Math.min(aPageSize, 100);

        if (limit <= 0) {
            return CompletableFuture.completedFuture(new QueryResult<>());
        }

        final CompletableFuture<HBaseClientModels.Scanner> scanned = client.scan(HBaseClientModels.Scan.newBuilder()
                .setTable(TABLE)
                .setStartRowKey(startKey)
                .setEndRowKey(endKey)
                .addFamilies(CF_CONTENT)
                .setLimit(limit + 1)
                .build()
        );

        return scanned.thenApply(aResult -> {
            final QueryResult<UserGroupDTO> queryResult = new QueryResult<>();
            final int itemCount = Math.min(aResult.getRowsCount(), limit);
            for (int ri = 0; ri < itemCount; ri++) {
                final HBaseClientModels.Result row = aResult.getRows(ri);
                queryResult.add(parse(aAppId, row));
            }

            if (aResult.getRowsCount() > limit) {
                final HBaseClientModels.Result row = aResult.getRows(limit);
                final UserGroupDTO lastItem  = parse(aAppId, row);
                queryResult.setNextPageCursor(lastItem.groupId().value());
            }

            return queryResult;
        });
    }

    private UserGroupDTO parse(final ApplicationId aAppId, final HBaseClientModels.Result aRow) {
        UserId userId = null;
        GroupId groupId = null;
        GroupMember.State state = null;
        RoleName role = null;
        Long lastReadMessageId = null;
        Date created = null;

        for (int ci = 0; ci < aRow.getColumnsCount(); ci++) {
            HBaseClientModels.ColumnValue col = aRow.getColumns(ci);
            if (col.getKey().getQualifier().equals(COL_USER_ID)) {
                userId = new UserId(toString(col.getValue()));
            }
            else
            if (col.getKey().getQualifier().equals(COL_GROUP_ID)) {
                groupId = new GroupId(aAppId, toString(col.getValue()));
            }
            else
            if (col.getKey().getQualifier().equals(COL_MEMBER_STATE)) {
                state = GroupMember.State.fromValue(toString(col.getValue()));
            }
            else
            if (col.getKey().getQualifier().equals(COL_MEMBER_ROLE)) {
                role = new RoleName(toString(col.getValue()));
            }
            else
            if (col.getKey().getQualifier().equals(COL_MEMBER_CREATED)) {
                created = new Date(Bytes.toLong(col.getValue().toByteArray()));
            }
            else
            if (col.getKey().getQualifier().equals(COL_LAST_READ_MESSAGE_ID)) {
                lastReadMessageId = Bytes.toLong(col.getValue().toByteArray());
            }
        }

        return new UserGroupDTO(userId, groupId, state, role, lastReadMessageId, created);
    }

    private HBaseClientModels.Put makePut(final UserId aUserId,
                                          final GroupId aGroupId,
                                          final GroupMember.State aState,
                                          final RoleName aRole,
                                          final Date aCreatedOn) {
        final HBaseClientModels.Put.Builder builder = HBaseClientModels.Put.newBuilder();

        builder.setTable(TABLE);
        builder.setRowKey(makeRowKey(aUserId, aGroupId));
        builder.addColumns(columnValue(COL_USER_ID, adapt(aUserId.value())));
        builder.addColumns(columnValue(COL_GROUP_ID, adapt(aGroupId.value())));
        if (Objects.nonNull(aState)) {
            builder.addColumns(columnValue(COL_MEMBER_STATE, adapt(aState.value())));
        }
        if (Objects.nonNull(aRole)) {
            builder.addColumns(columnValue(COL_MEMBER_ROLE, adapt(aRole.value())));
        }
        if (Objects.nonNull(aCreatedOn)) {
            builder.addColumns(columnValue(COL_MEMBER_CREATED, adapt(aCreatedOn.getTime())));
        }

        return builder.build();
    }

    private HBaseClientModels.ColumnValue columnValue(ByteString aQualifier, ByteString aValue) {
        return HBaseClientModels.ColumnValue.newBuilder()
                .setKey(columnCoord(aQualifier))
                .setValue(aValue)
                .build();
    }

    private HBaseClientModels.ColumnCoord columnCoord(ByteString aQualifier) {
        return HBaseClientModels.ColumnCoord.newBuilder()
                .setFamily(CF_CONTENT)
                .setQualifier(aQualifier)
                .build();
    }

    private ByteString makeRowKey(final UserId aUserId, final GroupId aGroupId) {
        return makeRowKey(aGroupId.appId(), aUserId, KEY_DELIMITER, aGroupId);
    }

    private ByteString makeStartRowKey(final ApplicationId aAppId, final UserId aUserId) {
        return makeRowKey(aAppId, aUserId, KEY_DELIMITER, null);
    }

    private ByteString makeEndRowKey(final ApplicationId aAppId, final UserId aUserId) {
        return makeRowKey(aAppId, aUserId, KEY_CAP, null);
    }

    private ByteString makeRowKey(final ApplicationId aAppId,
                                  final UserId aUserId,
                                  final byte aUserDelimiter,
                                  final GroupId aGroupId) {
        final byte[] appId = Bytes.toBytes(aAppId.value());
        final byte[] userId = Bytes.toBytes(aUserId.value());
        final byte[] groupId = Objects.nonNull(aGroupId) ? Bytes.toBytes(aGroupId.value()) : null;

        int keyBufferSize = appId.length + 1 + userId.length + 1;
        if (Objects.nonNull(groupId)) {
            keyBufferSize += groupId.length;
        }

        final byte[] rowKey = new byte[keyBufferSize];

        int offset = Bytes.putBytes(rowKey, 0, appId, 0, appId.length);
        offset = Bytes.putByte(rowKey, offset, KEY_DELIMITER);
        offset = Bytes.putBytes(rowKey, offset, userId, 0, userId.length);
        offset = Bytes.putByte(rowKey, offset, aUserDelimiter);

        if (Objects.nonNull(groupId)) {
            Bytes.putBytes(rowKey, offset, groupId, 0, groupId.length);
        }

        return ByteString.copyFrom(rowKey);
    }

    private ByteString adapt(final String aValue) {
        return ByteString.copyFrom(aValue, StandardCharsets.UTF_8);
    }

    private ByteString adapt(final Long aValue) {
        return ByteString.copyFrom(Bytes.toBytes(aValue));
    }

    private String toString(final ByteString aBytes) {
        return Bytes.toString(aBytes.toByteArray());
    }
}
