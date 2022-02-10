package io.fizz.chat.application.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.fizz.chat.application.AbstractApplicationRepository;
import io.fizz.chat.application.FCMConfiguration;
import io.fizz.chat.application.Preferences;
import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.chat.application.domain.AzureProviderConfig;
import io.fizz.chat.application.domain.CleanSpeakProviderConfig;
import io.fizz.client.hbase.HBaseClientModels;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import org.apache.hadoop.hbase.util.Bytes;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HBaseApplicationRepository implements AbstractApplicationRepository {
    private static final LoggingService.Log log = LoggingService.getLogger(HBaseApplicationRepository.class);

    private static final String NAMESPACE = ConfigService.config().getString("chat.application.hbase.namespace");
    private static final ByteString NS_NAME = ByteString.copyFrom(Bytes.toBytes(NAMESPACE));
    private static final ByteString TABLE_NAME = ByteString.copyFrom(Bytes.toBytes("tbl_app"));
    private static final ByteString CF_CONTENT = ByteString.copyFrom(Bytes.toBytes("c"));
    private static final ByteString COL_FCM_CONFIG = ByteString.copyFrom(Bytes.toBytes("fcm"));
    private static final ByteString COL_PREFERENCES = ByteString.copyFrom(Bytes.toBytes("prefs"));
    private static final ByteString COL_CONTENT_MODERATION = ByteString.copyFrom(Bytes.toBytes("cm"));

    private static final String AZURE_BASE_URL_KEY = "baseUrl";
    private static final String AZURE_SECRET_KEY = "secret";

    private static final String CS_BASE_URL_KEY = "baseUrl";
    private static final String CS_SECRET_KEY = "secret";
    private static final String CS_APP_ID_KEY = "appId";


    private static final HBaseClientModels.Table TABLE = HBaseClientModels.Table.newBuilder()
            .setNamespace(NS_NAME)
            .setName(TABLE_NAME)
            .build();

    private final AbstractHBaseClient client;

    public HBaseApplicationRepository(final AbstractHBaseClient aClient) {
        Utils.assertRequiredArgument(aClient, "invalid_hbase_client");

        this.client = aClient;
    }

    @Override
    public CompletableFuture<Preferences> getPreferences(final ApplicationId aId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aId, "invalid_app_id");

            final HBaseClientModels.Get get = makeGet(aId, COL_PREFERENCES);
            final CompletableFuture<HBaseClientModels.Result> executed = client.get(get);

            final CompletableFuture<ByteString> parsed =
                    executed.thenApply(aResult -> {
                        for (int i = 0; i < aResult.getColumnsCount(); i++) {
                            HBaseClientModels.ColumnValue col = aResult.getColumns(i);
                            if (col.getKey().getQualifier().equals(COL_PREFERENCES)) {
                                return col.getValue();
                            }
                        }
                        return null;
                    });

            return parsed.thenApply(this::deserializePreferences);
        });
    }

    @Override
    public CompletableFuture<Void> put(final ApplicationId aId, final Preferences aPrefs) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aId, "invalid_app_id");
            Utils.assertRequiredArgument(aPrefs, "invalid_config");

            final ByteString prefsByte = serializePreferences(aPrefs);
            final HBaseClientModels.ColumnValue prefsCV = buildColumnValue(CF_CONTENT, COL_PREFERENCES, prefsByte);

            return client.put(makePut(aId, prefsCV))
                    .thenApply(aSaved -> null);
        });
    }

    @Override
    public CompletableFuture<FCMConfiguration> getConfigFCM(final ApplicationId aId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aId, "invalid_app_id");

            final HBaseClientModels.Get get = makeGet(aId, COL_FCM_CONFIG);
            final CompletableFuture<HBaseClientModels.Result> executed = client.get(get);

            final CompletableFuture<ByteString> parsed =
                    executed.thenApply(aResult -> {
                        for (int i = 0; i < aResult.getColumnsCount(); i++) {
                            HBaseClientModels.ColumnValue col = aResult.getColumns(i);
                            if (col.getKey().getQualifier().equals(COL_FCM_CONFIG)) {
                                return col.getValue();
                            }
                        }
                        return null;
                    });

            return parsed.thenApply(this::deserializeFCMConfig);
        });
    }

    @Override
    public CompletableFuture<Void> put(final ApplicationId aId, final FCMConfiguration aConfig) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aId, "invalid_app_id");
            Utils.assertRequiredArgument(aConfig, "invalid_config");

            final ByteString configFCMBytes = serializeFCMConfig(aConfig);
            final HBaseClientModels.ColumnValue fcmConfigCV = buildColumnValue(CF_CONTENT, COL_FCM_CONFIG, configFCMBytes);

            return client.put(makePut(aId, fcmConfigCV))
                    .thenApply(aSaved -> null);
        });
    }

    @Override
    public CompletableFuture<Void> removeConfigFCM(final ApplicationId aId) {
        Utils.assertRequiredArgument(aId, "invalid_app_id");

        return Utils.async(() -> client.delete(makeDelete(aId, COL_FCM_CONFIG))
                .thenApply(aSaved -> null));
    }

    @Override
    public CompletableFuture<Boolean> saveContentModerationConfig(final ApplicationId aAppId,
                                           final AbstractProviderConfig aConfig) {
        final HBaseClientModels.ColumnValue contentConfigCV = buildColumnValue(CF_CONTENT, COL_CONTENT_MODERATION, serialize(aConfig));

        return client.put(makePut(aAppId, contentConfigCV));
    }

    @Override
    public CompletableFuture<AbstractProviderConfig> fetchContentModerationConfig(final ApplicationId aAppId) {

        return client.get(makeGet(aAppId, COL_CONTENT_MODERATION))
                .thenApply(aResult -> {
                    if (Objects.isNull(aResult) || aResult.getColumnsCount() <= 0) {
                        return null;
                    }
                    return deserialise(aResult.getColumns(0).getValue());
                });
    }

    @Override
    public CompletableFuture<Void> deleteContentModerationConfig(final ApplicationId aAppId) {
        return client.delete(makeDelete(aAppId, COL_CONTENT_MODERATION));
    }

    private HBaseClientModels.Put makePut(final ApplicationId aId, final HBaseClientModels.ColumnValue aValue) {
        final ByteString rowKey = makeRowKey(aId);

        final HBaseClientModels.Put.Builder builder = HBaseClientModels.Put.newBuilder();

        return builder.setTable(TABLE)
                .addColumns(aValue)
                .setRowKey(rowKey)
                .build();
    }

    private HBaseClientModels.Get makeGet(final ApplicationId aId, final ByteString aQualifier) {
        final ByteString rowKey = makeRowKey(aId);
        final HBaseClientModels.Get.Builder builder = HBaseClientModels.Get.newBuilder();

        return builder.setTable(TABLE)
                .addFamilies(CF_CONTENT)
                .addColumns(columnCoord(aQualifier))
                .setRowKey(rowKey)
                .build();
    }

    private HBaseClientModels.Delete makeDelete(final ApplicationId aId, final ByteString aQualifier) {
        final ByteString rowKey = makeRowKey(aId);
        return HBaseClientModels.Delete.newBuilder()
                .setTable(TABLE)
                .addColumns(columnCoord(aQualifier))
                .setRowKey(rowKey)
                .build();
    }

    private HBaseClientModels.ColumnCoord columnCoord(final ByteString aQualifier) {
        return HBaseClientModels.ColumnCoord.newBuilder()
                .setFamily(CF_CONTENT)
                .setQualifier(aQualifier)
                .build();
    }

    private ByteString serializePreferences(final Preferences aPrefs) {
        final Models.Prefs.Builder builder = Models.Prefs.newBuilder();
        builder.setForceCM(aPrefs.isForceContentModeration());

        return builder.build().toByteString();
    }

    private Preferences deserializePreferences(final ByteString aBuffer) {
        if (Objects.isNull(aBuffer)) {
            return null;
        }

        try {
            Models.Prefs prefs = Models.Prefs.parseFrom(aBuffer);

            Preferences preferences = new Preferences();
            preferences.setForceContentModeration(prefs.getForceCM());
            return preferences;
        }
        catch (InvalidProtocolBufferException ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    private ByteString serializeFCMConfig(final FCMConfiguration aConfig) {
        final Models.FCMConfig.Builder builder = Models.FCMConfig.newBuilder();
        builder.setTitle(aConfig.title());
        builder.setSecret(aConfig.secret());

        return builder.build().toByteString();
    }

    private FCMConfiguration deserializeFCMConfig(final ByteString aBuffer) {
        if (Objects.isNull(aBuffer)) {
            return null;
        }

        try {
            Models.FCMConfig configModel = Models.FCMConfig.parseFrom(aBuffer);
            return new FCMConfiguration(configModel.getTitle(), configModel.getSecret());
        }
        catch (InvalidProtocolBufferException ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    private HBaseClientModels.ColumnValue buildColumnValue(final ByteString aFamily,
                                                           final ByteString aQualifier,
                                                           final ByteString aValue) {
        return HBaseClientModels.ColumnValue.newBuilder()
                .setKey(
                        HBaseClientModels.ColumnCoord.newBuilder()
                                .setFamily(aFamily)
                                .setQualifier(aQualifier)
                                .build()
                )
                .setValue(aValue)
                .build();
    }

    private static ByteString makeRowKey(final ApplicationId aId) {
        return ByteString.copyFrom(aId.value(), StandardCharsets.UTF_8);
    }

    private ByteString serialize(final AbstractProviderConfig aConfig) {
        final Models.ProviderConfigModel.Builder modelBuilder = Models.ProviderConfigModel.newBuilder();
        modelBuilder.setType(Models.ProviderConfigModel.ProviderType.forNumber(aConfig.type().value()));

        switch (aConfig.type()) {
            case Azure:
                AzureProviderConfig azureConfig = (AzureProviderConfig) aConfig;
                modelBuilder.putParams(AZURE_BASE_URL_KEY, azureConfig.baseUrl());
                modelBuilder.putParams(AZURE_SECRET_KEY, azureConfig.secret());
                break;
            case CleanSpeak:
                CleanSpeakProviderConfig config = (CleanSpeakProviderConfig) aConfig;
                modelBuilder.putParams(CS_BASE_URL_KEY, config.baseUrl());
                modelBuilder.putParams(CS_SECRET_KEY, config.secret());
                modelBuilder.putParams(CS_APP_ID_KEY, config.appId());
                break;
        }

        return modelBuilder.build().toByteString();
    }

    private AbstractProviderConfig deserialise(final ByteString aBuffer) {
        try {
            final Models.ProviderConfigModel model = Models.ProviderConfigModel.parseFrom(aBuffer);
            AbstractProviderConfig config = null;
            switch (model.getType()) {
                case Azure: {
                    String aBaseUrl = model.getParamsOrDefault(AZURE_BASE_URL_KEY, "");
                    String aSecret = model.getParamsOrDefault(AZURE_SECRET_KEY, "");
                    config = new AzureProviderConfig(aBaseUrl, aSecret);
                }
                break;
                case CleanSpeak: {
                    String baseURL = model.getParamsOrDefault(CS_BASE_URL_KEY, "");
                    String secret = model.getParamsOrDefault(CS_SECRET_KEY, "");
                    String appId = model.getParamsOrDefault(CS_APP_ID_KEY, "");
                    config = new CleanSpeakProviderConfig(baseURL, secret, appId);
                }
                break;
            }
            return config;
        }
        catch (Exception ex) {
            log.error("Invalid application config format: " + ex.getMessage());
            return null;
        }
    }
}
