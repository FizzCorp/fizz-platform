package io.fizz.chat.user.infrastructure.persistence;

import com.google.protobuf.ByteString;
import io.fizz.chat.user.domain.PushPlatform;
import io.fizz.client.hbase.HBaseClientModels;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.Url;
import io.fizz.common.domain.UserId;
import io.fizz.chat.user.application.repository.AbstractUserRepository;
import io.fizz.chat.user.domain.Nick;
import io.fizz.chat.user.domain.User;
import io.fizz.chat.user.domain.StatusMessage;
import io.fizz.chat.user.infrastructure.ConfigService;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/*
* Note: This repo stores multiple tokens for multiple platforms for supporting multiple devices in the future.
*/
public class HBaseUserRepository implements AbstractUserRepository, Serializable {
    private static final LoggingService.Log log = LoggingService.getLogger(HBaseUserRepository.class);

    private static final String USER_PROFILE_NAMESPACE = ConfigService.config().getString("chat.user.hbase.namespace");
    private static final ByteString NS_NAME = ByteString.copyFrom(Bytes.toBytes(USER_PROFILE_NAMESPACE));
    private static final ByteString TABLE_NAME = ByteString.copyFrom(Bytes.toBytes("tbl_profile"));
    private static final ByteString CF_CONTENT = ByteString.copyFrom(Bytes.toBytes("c"));

    private static final ByteString COL_PROFILE = ByteString.copyFrom(Bytes.toBytes("profile"));

    private static final HBaseClientModels.Table TABLE = HBaseClientModels.Table.newBuilder()
            .setNamespace(NS_NAME)
            .setName(TABLE_NAME)
            .build();

    private final AbstractHBaseClient client;

    public HBaseUserRepository(final AbstractHBaseClient aClient) {
        Utils.assertRequiredArgument(aClient, "invalid hbase client");
        this.client = aClient;
    }

    @Override
    public CompletableFuture<User> get(final ApplicationId aAppId, final UserId aUserId) {
        Utils.assertRequiredArgument(aAppId, "invalid application id specified.");
        Utils.assertRequiredArgument(aUserId, "invalid user id specified.");

        final HBaseClientModels.Get get = makeGet(aAppId, aUserId);
        return client.get(get)
                .thenApply(aResult -> {
                    ByteString profileBuffer = null;

                    for (int ci = 0; ci < aResult.getColumnsCount(); ci++) {
                        final HBaseClientModels.ColumnValue col = aResult.getColumns(ci);

                        if (col.getKey().getQualifier().equals(COL_PROFILE)) {
                            profileBuffer = col.getValue();
                        }
                    }
                    return deserialize(profileBuffer);
                });
    }

    @Override
    public CompletableFuture<Boolean> put(final User aProfile) {
        final HBaseClientModels.Put put = makePut(aProfile);
        return client.put(put);
    }

    private static HBaseClientModels.Get makeGet(final ApplicationId aAppId, final UserId aUserId) {
        final ByteString rowKey = makeRowKey(aAppId, aUserId);
        return HBaseClientModels.Get.newBuilder()
                .setTable(TABLE)
                .addColumns(buildColumnCoord(COL_PROFILE))
                .setRowKey(rowKey)
                .build();
    }

    private HBaseClientModels.Put makePut(final User aProfile) {
        final HBaseClientModels.Put.Builder builder = HBaseClientModels.Put.newBuilder();
        final ByteString rowKey = makeRowKey(aProfile.appId(), aProfile.userId());
        final ByteString newValue = serialize(aProfile);

        builder.setTable(TABLE);
        builder.setRowKey(rowKey);
        builder.addColumns(buildColumnValue(COL_PROFILE, newValue));

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
                .setFamily(CF_CONTENT)
                .setQualifier(aQualifier)
                .build();
    }

    private static ByteString makeRowKey(final ApplicationId aAppId, final UserId aUserId) {
        return ByteString.copyFrom(aUserId.qualifiedValue(aAppId), StandardCharsets.UTF_8);
    }

    private static ByteString serialize(final User aProfile) {
        Models.ProfileModel.Builder builder = Models.ProfileModel.newBuilder();

        builder.setAppId(aProfile.appId().value());
        builder.setUserId(aProfile.userId().value());
        builder.setNick(Objects.isNull(aProfile.nick()) ? "" : aProfile.nick().value());
        builder.setStatusMessage(Objects.isNull(aProfile.statusMessage()) ? "" : aProfile.statusMessage().value());
        builder.setProfileUrl(Objects.isNull(aProfile.profileUrl()) ? "" : aProfile.profileUrl().value());

        if (Objects.nonNull(aProfile.token())) {
            final Deque<String> tokens = new ArrayDeque<>(Collections.singletonList(aProfile.token()));
            builder.putTokens(PushPlatform.FCM.value(), serialize(tokens));
        }

        return builder.build().toByteString();
    }

    private static Models.PlatformTokens serialize(final Deque<String> aTokens) {
        Models.PlatformTokens.Builder builder = Models.PlatformTokens.newBuilder();

        for (String token: aTokens) {
            builder.addToken(token);
        }

        return builder.build();
    }

    private static User deserialize(final ByteString aBytes) {
        if (Objects.isNull(aBytes)) {
            return null;
        }

        try {
            final Models.ProfileModel model = Models.ProfileModel.parseFrom(aBytes);
            final User user =  new User.Builder()
                    .setAppId(new ApplicationId(model.getAppId()))
                    .setUserId(new UserId(model.getUserId()))
                    .setNick(new Nick(model.getNick()))
                    .setStatusMessage(new StatusMessage(model.getStatusMessage()))
                    .setProfileUrl(model.getProfileUrl().isEmpty() ? null : new Url(model.getProfileUrl()))
                    .build();

            final Map<String, Models.PlatformTokens> tokensMap = model.getTokensMap();
            for (Map.Entry<String, Models.PlatformTokens> entry: tokensMap.entrySet()) {
                PushPlatform platform = PushPlatform.fromValue(entry.getKey());
                if (Objects.isNull(platform)) {
                    log.error("error parsing platform token: " + entry.getKey() + " : " + entry.getValue());
                    continue;
                }

                deserialize(entry.getValue(), user);
            }

            return user;
        } catch (Exception ex) {
            log.fatal(ex.getMessage());
            return null;
        }
    }

    private static void deserialize(final Models.PlatformTokens aInTokens, final User aUser) {
        if (Objects.isNull(aInTokens)) {
            return;
        }

        for (int ti = 0; ti < aInTokens.getTokenCount(); ti++) {
            aUser.setToken(aInTokens.getToken(ti));
        }
    }
}
