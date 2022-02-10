package io.fizz.chat.infrastructure.persistence;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import io.fizz.chat.domain.channel.AbstractChannelMessageRepository;
import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.chat.domain.subscriber.AbstractSubscriptionService;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chat.infrastructure.ConfigService;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.HBaseClientModels;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.application.AbstractUIDService;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;
import org.apache.hadoop.hbase.util.Bytes;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class HBaseChannelMessageRepository implements AbstractChannelMessageRepository {
    private static class ValueObject {
        final String value;

        ValueObject(final String aValue) {
            value = aValue;
        }
    }

    private static class ChannelMessageSerde implements JsonSerializer<ChannelMessage>,
                                                        JsonDeserializer<ChannelMessage> {
        private static final String KEY_ID = "id";
        private static final String KEY_APP_ID = "appId";
        private static final String KEY_FROM = "from";
        private static final String KEY_NICK = "nick";
        private static final String KEY_TO = "to";
        private static final String KEY_TOPIC = "topic";
        private static final String KEY_BODY = "body";
        private static final String KEY_DATA = "data";
        private static final String KEY_CREATED = "created";
        private static final String KEY_UPDATED = "updated";
        private static final String KEY_DELETED = "deleted";
        private static final String KEY_TRANSLATIONS = "translations";

        private final ApplicationId appId;
        private final AbstractSubscriptionService subscriptionService;

        ChannelMessageSerde(final ApplicationId aAppId, final AbstractSubscriptionService aSubscriptionService) {
            appId = aAppId;
            subscriptionService = aSubscriptionService;
        }

        @Override
        public JsonElement serialize(final ChannelMessage aMessage,
                                     final Type aType,
                                     final JsonSerializationContext aContext) {
            final JsonObject json = new JsonObject();

            json.addProperty(KEY_ID, aMessage.id());
            json.add(KEY_FROM, aContext.serialize(aMessage.from()));
            json.addProperty(KEY_APP_ID, aMessage.to().appId().value());
            json.addProperty(KEY_NICK, aMessage.nick());
            json.add(KEY_TO, aContext.serialize(new ValueObject(aMessage.to().value())));
            json.addProperty(KEY_TOPIC, aMessage.topic().value());
            json.addProperty(KEY_BODY, aMessage.body());
            json.addProperty(KEY_DATA, aMessage.data());
            json.addProperty(KEY_CREATED, aMessage.created());
            json.addProperty(KEY_UPDATED, aMessage.updated());
            json.addProperty(KEY_DELETED, aMessage.deleted());
            json.add(KEY_TRANSLATIONS, aContext.serialize(aMessage.translations()));

            return json;
        }

        @Override
        public ChannelMessage deserialize(final JsonElement aJsonElement,
                                          final Type aType,
                                          final JsonDeserializationContext aContext) throws JsonParseException {
            try {
                final JsonObject json = aJsonElement.getAsJsonObject();
                final UserId from = aContext.deserialize(json.get(KEY_FROM), UserId.class);
                final ValueObject to = aContext.deserialize(json.get(KEY_TO), ValueObject.class);
                final ChannelId channelId = new ChannelId(appId(json), to.value);
                final Map<LanguageCode,String> translations =
                                aContext.deserialize(json.get(KEY_TRANSLATIONS), buildTranslationType());

                return new ChannelMessage(
                        json.get(KEY_ID).getAsLong(),
                        from,
                        child(json, KEY_NICK),
                        channelId,
                        topicId(json, channelId),
                        child(json, KEY_BODY),
                        child(json, KEY_DATA),
                        null,
                        new Date(json.get(KEY_CREATED).getAsLong()),
                        new Date(json.get(KEY_UPDATED).getAsLong()),
                        json.get(KEY_DELETED).getAsBoolean(),
                        translations
                );
            }
            catch (IllegalArgumentException ex) {
                throw new JsonParseException(ex.getMessage());
            }
        }

        private String child(final JsonObject aJson, final String aChildKey) {
            return aJson.has(aChildKey) ? aJson.get(aChildKey).getAsString() : null;
        }

        private TopicId topicId(final JsonObject aJson, final ChannelId aChannelId) {
            if (aJson.has(KEY_TOPIC)) {
                return new TopicId(aJson.get(KEY_TOPIC).getAsString());
            }

            return subscriptionService.defaultTopic(aChannelId);
        }

        private ApplicationId appId(final JsonObject aJson) {
            ApplicationId applicationId = appId;
            try {
                if (aJson.has(KEY_APP_ID)) {
                    applicationId = new ApplicationId(aJson.get(KEY_APP_ID).getAsString());
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
            return applicationId;
        }

        private static Type buildTranslationType() {
            return new TypeToken<Map<LanguageCode,Object>>() {}.getType();
        }
    }

    private static final DomainErrorException ERROR_INVALID_MESSAGE = new DomainErrorException("invalid_message");
    private static final DomainErrorException ERROR_INVALID_MESSAGE_ID = new DomainErrorException("invalid_message_id");
    private static final DomainErrorException ERROR_INVALID_MSG_COUNT = new DomainErrorException("invalid_count");

    private static final LoggingService.Log logger = LoggingService.getLogger(HBaseChannelMessageRepository.class);

    private static final String CHAT_NAMESPACE = ConfigService.config().getString("chat.hbase.namespace");
    private static final ByteString NS_NAME = ByteString.copyFrom(Bytes.toBytes(CHAT_NAMESPACE));
    private static final ByteString TABLE_NAME = ByteString.copyFrom(Bytes.toBytes("tbl_message"));
    private static final ByteString CF_CONTENT = ByteString.copyFrom(Bytes.toBytes("c"));
    private static final ByteString COL_ID = ByteString.copyFrom(Bytes.toBytes("id"));
    private static final ByteString COL_DATA = ByteString.copyFrom(Bytes.toBytes("data"));

    private static final HBaseClientModels.Table TABLE = HBaseClientModels.Table.newBuilder()
                                                         .setNamespace(NS_NAME)
                                                         .setName(TABLE_NAME)
                                                         .build();

    private final AbstractHBaseClient client;
    private final AbstractUIDService idService;
    private final AbstractSubscriptionService subscriptionService;

    public HBaseChannelMessageRepository(final AbstractHBaseClient aClient,
                                         final AbstractUIDService aIdService,
                                         final AbstractSubscriptionService aSubscriptionService) {
        Utils.assertRequiredArgument(aClient, "invalid hbase connection");
        Utils.assertRequiredArgument(aIdService, "invalid id service");
        Utils.assertRequiredArgument(aSubscriptionService, "invalid subscription service");

        client = aClient;
        idService = aIdService;
        subscriptionService = aSubscriptionService;
    }

    @Override
    public CompletableFuture<Long> nextMessageId(final ApplicationId aAppId, final TopicId aTopicId) {
        if (Objects.isNull(aAppId)) {
            return Utils.failedFuture(ApplicationId.ERROR_INVALID_APP_ID);
        }
        if (Objects.isNull(aTopicId)) {
            return Utils.failedFuture(TopicId.ERROR_INVALID_TOPIC_ID);
        }

        return mapChannelId(aAppId, aTopicId)
                .thenCompose(aNamespace -> idService.nextId(Bytes.toBytes(aNamespace)));
    }

    @Override
    public CompletableFuture<Boolean> save(final ApplicationId aAppId,
                                           final TopicId aTopicId,
                                           final ChannelMessage aMessage) {
        return saveMessage(aAppId, aTopicId, aMessage, false);
    }

    @Override
    public CompletableFuture<Boolean> update(final ApplicationId aAppId,
                                             final TopicId aTopicId,
                                             final ChannelMessage aMessage) {
        return saveMessage(aAppId, aTopicId, aMessage, true);
    }

    @Override
    public CompletableFuture<Void> remove(final ApplicationId aAppId, final TopicId aTopicId, long aMessageId) {
        if (Objects.isNull(aAppId)) {
            return Utils.failedFuture(ApplicationId.ERROR_INVALID_APP_ID);
        }
        if (Objects.isNull(aTopicId)) {
            return Utils.failedFuture(TopicId.ERROR_INVALID_TOPIC_ID);
        }

        return mapChannelId(aAppId, aTopicId)
            .thenCompose(aMappedTopicId -> client.delete(makeDelete(aMappedTopicId, aMessageId)));
    }

    @Override
    public CompletableFuture<ChannelMessage> messageOfId(final ApplicationId aAppId,
                                                         final TopicId aTopicId,
                                                         long aMessageId) {
        if (Objects.isNull(aAppId)) {
            return Utils.failedFuture(ApplicationId.ERROR_INVALID_APP_ID);
        }
        if (Objects.isNull(aTopicId)) {
            return Utils.failedFuture(TopicId.ERROR_INVALID_TOPIC_ID);
        }
        if (aMessageId < 0) {
            return Utils.failedFuture(ERROR_INVALID_MESSAGE_ID);
        }

        return mapChannelId(aAppId, aTopicId)
            .thenCompose(aChannelId -> client.get(makeGet(aChannelId, aMessageId)))
            .thenCompose(aResult -> {
                if (Objects.isNull(aResult) || aResult.getColumnsCount() <= 0) {
                    return CompletableFuture.completedFuture(null);
                }
                else {
                    return CompletableFuture.completedFuture(parseMessage(aAppId, aResult));
                }
            });
    }

    @Override
    public CompletableFuture<List<ChannelMessage>> queryLatest(final ApplicationId aAppId,
                                                               final TopicId aTopicId,
                                                               int aCount) {
        return query(aAppId, aTopicId, aCount, null, null);
    }

    public CompletableFuture<List<ChannelMessage>> query(final ApplicationId aAppId,
                                                         final TopicId aTopicId,
                                                         int aCount,
                                                         Long aBefore,
                                                         Long aAfter) {
        Utils.assertRequiredArgument(aAppId, ApplicationId.ERROR_INVALID_APP_ID.error().reason());
        Utils.assertRequiredArgument(aTopicId, TopicId.ERROR_INVALID_TOPIC_ID);

        if (aCount < 0) {
            return Utils.failedFuture(ERROR_INVALID_MSG_COUNT);
        }
        if (aCount == 0) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        final boolean isReversed = Objects.isNull(aBefore) && Objects.nonNull(aAfter);

        return mapChannelId(aAppId, aTopicId)
            .thenCompose(aId -> {
                HBaseClientModels.Scan scanModel;

                if (isReversed) {
                    scanModel = makeScan(aId, aCount, aAfter + 1, true);
                }
                else {
                    scanModel = makeScan(aId, aCount, Objects.nonNull(aBefore) ? aBefore - 1 : null, false);
                }

                return client.scan(scanModel);
            })
            .thenCompose(aScanner -> CompletableFuture.completedFuture(parseMessageList(aAppId, aScanner, isReversed)));
    }

    private List<ChannelMessage> parseMessageList(final ApplicationId aAppId,
                                                  final HBaseClientModels.Scanner aScanner,
                                                  boolean isReversed) {
        final List<ChannelMessage> messages = new ArrayList<>();

        for (int ri = 0; ri < aScanner.getRowsCount(); ri++) {
            final HBaseClientModels.Result row = aScanner.getRows(ri);
            final ChannelMessage message = parseMessage(aAppId, row);
            if (!Objects.isNull(message)) {
                messages.add(message);
            }
        }

        if (!isReversed) {
            Collections.reverse(messages);
        }

        return messages;
    }

    private ChannelMessage parseMessage(final ApplicationId aAppId, final HBaseClientModels.Result aRow) {
        byte[] data = null;

        for (int ci = 0; ci < aRow.getColumnsCount(); ci++) {
            final HBaseClientModels.ColumnValue col = aRow.getColumns(ci);
            final ByteString qualifier = col.getKey().getQualifier();

            if (qualifier.equals(COL_DATA)) {
                data = col.getValue().toByteArray();
            }
        }

        if (Objects.isNull(data)) {
            logger.error("No data field found while parsing message.");
            return null;
        }

        return deserialize(aAppId, data);
    }

    private ChannelMessage deserialize(final ApplicationId aAppId, final byte[] aBuffer) {
        final Gson serde = new GsonBuilder()
                .registerTypeAdapter(ChannelMessage.class, new ChannelMessageSerde(aAppId, subscriptionService))
                .create();

        return serde.fromJson(Bytes.toString(aBuffer), ChannelMessage.class);
    }

    private CompletableFuture<Boolean> saveMessage(final ApplicationId aAppId,
                                                   final TopicId aTopicId,
                                                   final ChannelMessage aMessage,
                                                   boolean aCheckForExistence) {
        if (Objects.isNull(aAppId)) {
            return Utils.failedFuture(ApplicationId.ERROR_INVALID_APP_ID);
        }
        if (Objects.isNull(aTopicId)) {
            return Utils.failedFuture(TopicId.ERROR_INVALID_TOPIC_ID);
        }
        if (Objects.isNull(aMessage)) {
            return Utils.failedFuture(ERROR_INVALID_MESSAGE);
        }

        return mapChannelId(aAppId, aTopicId)
                .thenCompose(aMappedTopicId -> client.put(makePut(aMappedTopicId, aMessage, aCheckForExistence)));
    }

    private HBaseClientModels.ColumnValue buildColumnValue(final ByteString aFamily,
                                                           final ByteString aQualifier,
                                                           final byte[] aValue) {
        return HBaseClientModels.ColumnValue.newBuilder()
            .setKey(
                HBaseClientModels.ColumnCoord.newBuilder()
                .setFamily(aFamily)
                .setQualifier(aQualifier)
                .build()
            )
            .setValue(ByteString.copyFrom(aValue))
            .build();
    }

    private HBaseClientModels.Put makePut(long aTopicId, final ChannelMessage aMessage, boolean aCheckExistence) {
        final HBaseClientModels.Put.Builder put = HBaseClientModels.Put.newBuilder();
        final byte[] rowKey = makeRowKey(aTopicId, aMessage.id());
        final byte[] payload = serialize(aMessage);
        final HBaseClientModels.ColumnValue colId = buildColumnValue(CF_CONTENT, COL_ID, Bytes.toBytes(aMessage.id()));

        put.setTable(TABLE);
        put.setRowKey(ByteString.copyFrom(rowKey));
        put.addColumns(colId);
        put.addColumns(buildColumnValue(CF_CONTENT, COL_DATA, payload));

        if (aCheckExistence) {
            put.setCondition(colId);
        }

        return put.build();
    }

    private byte[] serialize(final ChannelMessage aMessage) {
        final Gson serde = new GsonBuilder()
                .registerTypeAdapter(
                    ChannelMessage.class, new ChannelMessageSerde(aMessage.to().appId(), subscriptionService)
                )
                .create();

        return Bytes.toBytes(serde.toJson(aMessage));
    }

    private HBaseClientModels.Delete makeDelete(long aTopicId, long aMessageId) {
        byte[] rowKey = makeRowKey(aTopicId, aMessageId);
        return HBaseClientModels.Delete.newBuilder()
                .setTable(TABLE)
                .setRowKey(ByteString.copyFrom(rowKey))
                .build();
    }

    private HBaseClientModels.Get makeGet(long aTopicId, long aMessageId) {
        byte[] rowKey = makeRowKey(aTopicId, aMessageId);
        return HBaseClientModels.Get.newBuilder()
                .setTable(TABLE)
                .addFamilies(CF_CONTENT)
                .setRowKey(ByteString.copyFrom(rowKey))
                .build();
    }


    private HBaseClientModels.Scan makeScan(long aTopicId, int aCount, Long aOrigin, boolean aReversed) {
        byte[] startKey = Objects.isNull(aOrigin) ?
                           Bytes.padTail(Bytes.toBytes(aTopicId), Long.BYTES) :
                           makeRowKey(aTopicId, aOrigin);
        byte[] stopKey;

        if (aReversed) {
            stopKey = Bytes.padTail(Bytes.toBytes(aTopicId), Long.BYTES + 1);
        }
        else {
            stopKey = Bytes.padTail(Bytes.toBytes(++aTopicId), Long.BYTES);
        }

        return HBaseClientModels.Scan.newBuilder()
                .setTable(TABLE)
                .setStartRowKey(ByteString.copyFrom(startKey))
                .setEndRowKey(ByteString.copyFrom(stopKey))
                .addFamilies(CF_CONTENT)
                .setLimit(aCount)
                .setReversed(aReversed)
                .build();
    }

    private byte[] makeRowKey(long aTopicId, long aMessageId) {
        byte[] rowKey = new byte[Long.BYTES + Long.BYTES];

        int offset = 0;
        offset = Bytes.putBytes(rowKey, offset, Bytes.toBytes(aTopicId), 0, Long.BYTES);
        Bytes.putBytes(rowKey, offset, Bytes.toBytes(Long.MAX_VALUE - aMessageId), 0, Long.BYTES);

        return rowKey;
    }

    private CompletableFuture<Long> mapChannelId(final ApplicationId aAppId, final TopicId aTopicId) {
        final String name = aAppId.value() + aTopicId.value();
        return idService.createOrFindId(Bytes.toBytes(name), true);
    }
}
