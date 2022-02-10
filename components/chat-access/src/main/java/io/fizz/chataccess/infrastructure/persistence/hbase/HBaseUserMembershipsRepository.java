package io.fizz.chataccess.infrastructure.persistence.hbase;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.fizz.chataccess.domain.context.AbstractUserMembershipsRepository;
import io.fizz.chataccess.domain.context.AuthContextId;
import io.fizz.chataccess.domain.context.UserMemberships;
import io.fizz.chataccess.domain.role.GroupMembership;
import io.fizz.chataccess.domain.role.GroupName;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chataccess.infrastructure.persistence.Models;
import io.fizz.chatcommon.domain.MutationId;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.HBaseClientModels;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;
import org.apache.hadoop.hbase.util.Bytes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HBaseUserMembershipsRepository implements AbstractUserMembershipsRepository {
    private static final LoggingService.Log logger = LoggingService.getLogger(HBaseUserMembershipsRepository.class);
    private static final ByteString NULL_VALUE = ByteString.copyFrom(new byte[]{});
    private static final ByteString TABLE_NAME = ByteString.copyFromUtf8("tbl_member");
    private static final ByteString CF_MEMBERSHIPS =  ByteString.copyFromUtf8("c");
    private static final ByteString COL_ENTITY = ByteString.copyFromUtf8("ent");
    private static final ByteString COL_VERSION = ByteString.copyFromUtf8("v");

    private final HBaseClientModels.Table table;
    private final AbstractHBaseClient client;

    public HBaseUserMembershipsRepository(final AbstractHBaseClient aClient,
                                          final String aNamespace) {
        Utils.assertRequiredArgument(aClient, "invalid hbase client specified");
        Utils.assertRequiredArgument(aNamespace, "invalid repo namespace specified");

        table = HBaseClientModels.Table.newBuilder()
                .setNamespace(ByteString.copyFromUtf8(aNamespace))
                .setName(TABLE_NAME)
                .build();

        client = aClient;
    }

    @Override
    public CompletableFuture<UserMemberships> fetch(final AuthContextId aContextId, final UserId aUserId) {
        final ByteString rowKey = makeRowKey(aContextId, aUserId);

        return client.get(makeGet(rowKey))
                .thenApply(aResult -> {
                    ByteString contentCell = null;
                    Integer version = null;

                    for (int ci = 0; ci < aResult.getColumnsCount(); ci++) {
                        final HBaseClientModels.ColumnValue col = aResult.getColumns(ci);

                        if (col.getKey().getQualifier().equals(COL_ENTITY)) {
                            contentCell = col.getValue();
                        }
                        else
                        if (col.getKey().getQualifier().equals(COL_VERSION)) {
                            version = Bytes.toInt(col.getValue().toByteArray());
                        }
                    }

                    if (Objects.isNull(contentCell)) {
                        return null;
                    }

                    return deserialize(aContextId, aUserId, contentCell, version);
                });
    }

    @Override
    public CompletableFuture<Boolean> save(final UserMemberships aMemberships) {
        final ByteString rowKey = makeRowKey(aMemberships.contextId(), aMemberships.userId());

        return client.put(makePut(rowKey, aMemberships));
    }

    private ByteString makeRowKey(final AuthContextId aContextId, final UserId aUserId) {
        try {
            final String key = aContextId.qualifiedValue() + aUserId.value();
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] keyBuffer = key.getBytes();
            final byte[] hashBuffer = md.digest(keyBuffer);
            final byte[] buffer = new byte[2 + keyBuffer.length];

            System.arraycopy(keyBuffer, 0, buffer, 2, keyBuffer.length);
            buffer[0] = hashBuffer[0];
            buffer[1] = hashBuffer[1];

            return ByteString.copyFrom(buffer);
        }
        catch (NoSuchAlgorithmException ex) {
            logger.fatal("no such digest alog");
            return null;
        }
    }

    private HBaseClientModels.Get makeGet(final ByteString aRowKey) {
        return HBaseClientModels.Get.newBuilder()
                .setTable(table)
                .setRowKey(aRowKey)
                .addColumns(buildColumnCoord(COL_ENTITY))
                .addColumns(buildColumnCoord(COL_VERSION))
                .build();
    }

    private HBaseClientModels.Put makePut(final ByteString aRowKey,
                                          final UserMemberships aMemberships) {
        final MutationId mutationId = aMemberships.mutationId();
        final HBaseClientModels.Put.Builder builder = HBaseClientModels.Put.newBuilder();
        final ByteString newValue = serialize(aMemberships);
        final ByteString newVersion = ByteString.copyFrom(Bytes.toBytes(mutationId.value()+1));

        builder.setTable(table);
        builder.setRowKey(aRowKey);
        builder.addColumns(buildColumnValue(COL_ENTITY, newValue));
        builder.addColumns(buildColumnValue(COL_VERSION, newVersion));

        if (aMemberships.mutationId().isNull()) {
            builder.setCondition(buildColumnValue(COL_VERSION, NULL_VALUE));
        }
        else {
            final ByteString preVersion = ByteString.copyFrom(Bytes.toBytes(mutationId.value()));
            builder.setCondition(buildColumnValue(COL_VERSION, preVersion));
        }

        return builder.build();
    }

    private static HBaseClientModels.ColumnValue buildColumnValue(final ByteString aQualifier, final ByteString aValue) {
        return HBaseClientModels.ColumnValue.newBuilder()
                .setKey(buildColumnCoord(aQualifier))
                .setValue(aValue)
                .build();
    }

    private static HBaseClientModels.ColumnCoord buildColumnCoord(final ByteString aQualifier) {
        return HBaseClientModels.ColumnCoord.newBuilder()
                .setFamily(CF_MEMBERSHIPS)
                .setQualifier(aQualifier)
                .build();
    }

    private static ByteString serialize(final UserMemberships aMemberships) {
        final Models.UserContextMemberships.Builder builder = Models.UserContextMemberships.newBuilder();

        for (final Map.Entry<GroupName, GroupMembership> entry: aMemberships.memberships().entrySet()) {
            final GroupMembership member = entry.getValue();

            builder.addMemberships(
                Models.GroupMemberModel.newBuilder()
                    .setMemberId(member.memberId().value())
                    .setGroupName(member.groupName().roleName().value())
                    .setEnds(
                        Models.Time.newBuilder()
                            .setValue(member.ends().getTime())
                        .build()
                    )
                .build()
            );
        }

        return builder.build().toByteString();
    }

    private static UserMemberships deserialize(final AuthContextId aContextId,
                                               final UserId aUserId,
                                               final ByteString aBuffer,
                                               final Integer version) {
        try {
            final Models.UserContextMemberships model = Models.UserContextMemberships.parseFrom(aBuffer);
            UserMemberships memberships;

            if (Objects.nonNull(version)) {
                memberships = new UserMemberships(aContextId, aUserId, new MutationId(version));
            }
            else {
                memberships = new UserMemberships(aContextId, aUserId);
            }

            deserializeMemberships(memberships, model);

            return memberships;
        }
        catch (IllegalArgumentException | InvalidProtocolBufferException ex) {
            logger.fatal(ex.getMessage());
            return null;
        }
    }

    private static void deserializeMemberships(final UserMemberships aMemberships,
                                               final Models.UserContextMemberships aMembershipsModel) {
        for (int mi = 0; mi < aMembershipsModel.getMembershipsCount(); mi++) {
            final Models.GroupMemberModel memberModel = aMembershipsModel.getMemberships(mi);

            final RoleName name = new RoleName(memberModel.getGroupName());

            if (memberModel.hasEnds()) {
                final Date ends = new Date(memberModel.getEnds().getValue());

                aMemberships.add(name, ends);
            }
            else {
                aMemberships.add(name);
            }
        }
    }
}
