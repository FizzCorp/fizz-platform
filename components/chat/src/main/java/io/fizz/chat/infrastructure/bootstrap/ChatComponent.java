package io.fizz.chat.infrastructure.bootstrap;

import io.fizz.chat.application.UserNotificationApplicationService;
import io.fizz.chat.application.channel.AbstractTranslationClient;
import io.fizz.chat.application.channel.ChannelApplicationService;
import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.application.services.AbstractPushNotificationService;
import io.fizz.chat.application.services.TranslationService;
import io.fizz.chat.domain.channel.*;
import io.fizz.chat.domain.subscriber.AbstractSubscriptionService;
import io.fizz.chat.infrastructure.ConfigService;
import io.fizz.chat.infrastructure.persistence.HBaseChannelMessageRepository;
import io.fizz.chat.infrastructure.services.HBaseUIDService;
import io.fizz.chat.infrastructure.services.RedisSubscriptionService;
import io.fizz.chat.moderation.application.service.ContentModerationService;
import io.fizz.chat.moderation.infrastructure.bootstrap.ChatModerationComponent;
import io.fizz.chat.presence.AbstractUserPresenceService;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.AbstractSubscriberRepository;
import io.fizz.chataccess.domain.AbstractAuthorizationService;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.domain.RedisNamespace;
import io.fizz.chatcommon.domain.events.AbstractEventPublisher;
import io.fizz.chatcommon.domain.events.DomainEventBus;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.VertxRedisClientProxy;
import io.fizz.chatcommon.infrastructure.messaging.kafka.KafkaEventPublisher;
import io.fizz.chatcommon.infrastructure.serde.JsonEventSerde;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.Config;
import io.fizz.common.LoggingService;
import io.fizz.common.application.AbstractUIDService;
import io.vertx.core.Vertx;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ChatComponent {
    private static final LoggingService.Log logger = LoggingService.getLogger(ChatComponent.class);

    private static final Config config = ConfigService.config();
    private static final int CHAT_TOPIC_SIZE = config.getNumber("chat.topic.size").intValue();

    private static final String KAFKA_TOPIC = config.getString("chat.kafka.topic");
    private static final String KAFKA_GROUP = config.getString("chat.kafka.consumer.group");

    private Vertx vertx;

    DomainEventBus eventBus;
    AbstractEventPublisher eventPublisher;
    AbstractSubscriberRepository subscriberRepo;
    AbstractSubscriptionService subscriptionService;

    private AbstractUIDService idService;
    private ChannelApplicationService channelService;
    private UserNotificationApplicationService userNotificationService;
    private AbstractChannelMessageRepository messageRepo;
    private TranslationService translationService;

    public CompletableFuture<Void> open(final Vertx aVertx,
                                        final VertxRedisClientProxy aRedisClient,
                                        final RedisNamespace aRedisNamespace,
                                        final AbstractHBaseClient aClient,
                                        final AbstractTopicMessagePublisher aMessagePublisher,
                                        final ApplicationService aAppService,
                                        final ChatModerationComponent aChatModComponent,
                                        final AbstractTranslationClient aTranslationClient,
                                        final AbstractAuthorizationService aAuthService,
                                        final AbstractUserPresenceService aPresenceService,
                                        final AbstractPushNotificationService aPushNotificationService,
                                        final RoleName aMutesRole,
                                        final RoleName aBansRole,
                                        final int aSubscriberTTL,
                                        final String aKafkaServers) {
        vertx = aVertx;
        return createUIDService(aClient)
                .thenCompose(v -> createDomainEventBus())
                .thenCompose(v -> createSubscriptionService(aRedisClient, aRedisNamespace, aSubscriberTTL))
                .thenCompose(v -> createRepos(aClient))
                .thenCompose(v -> createEventPublisher(aVertx, aKafkaServers))
                .thenCompose(v -> createUserNotificationService(aMessagePublisher))
                .thenCompose(v -> {
                    translationService = new TranslationService(aTranslationClient);

                    final ContentModerationService contentModerationService = aChatModComponent.contentModerationService();
                    return createChannelService(
                            eventBus,
                            aMessagePublisher,
                            messageRepo,
                            aAppService,
                            translationService,
                            contentModerationService,
                            aAuthService,
                            aPresenceService,
                            aPushNotificationService,
                            aMutesRole,
                            aBansRole
                    );
                });
    }

    public DomainEventBus eventBus() {
        return eventBus;
    }

    public ChannelApplicationService channelService() {
        return channelService;
    }

    public AbstractSubscriberRepository subscriberRepo() {
        return subscriberRepo;
    }

    public TranslationService translationService() {
        return translationService;
    }

    public UserNotificationApplicationService userNotificationService() {
        return userNotificationService;
    }

    protected CompletableFuture<Void> createEventPublisher(final Vertx aVertx, final String aKafkaServers) {
        eventPublisher = new KafkaEventPublisher(
                aVertx,
                new JsonEventSerde(),
                new DomainEventType[]{
                        ChannelMessageReceivedForPublish.TYPE,
                        ChannelMessageReceivedForUpdate.TYPE,
                        ChannelMessageReceivedForDeletion.TYPE,
                        ChannelMessageTranslated.TYPE
                },
                aKafkaServers,
                KAFKA_TOPIC,
                KAFKA_GROUP
        );

        eventBus.register(eventPublisher);
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> createUserNotificationService(final AbstractTopicMessagePublisher aMessagePublisher) {
        userNotificationService = new UserNotificationApplicationService(aMessagePublisher);
        return CompletableFuture.completedFuture(null);
    }

    protected CompletableFuture<Void> createSubscriptionService(final VertxRedisClientProxy aRedisClient,
                                                                final RedisNamespace aRedisNamespace,
                                                                final int aSubscriberTTL) {
        final CompletableFuture<Void> created = new CompletableFuture<>();

        final RedisSubscriptionService service =
                new RedisSubscriptionService(aRedisClient, aRedisNamespace, CHAT_TOPIC_SIZE, aSubscriberTTL);
        service.open()
                .handle((aVoid, aError) -> {
                    if (Objects.isNull(aError)) {
                        subscriberRepo = service;
                        subscriptionService = service;

                        created.complete(null);
                    } else {
                        created.completeExceptionally(aError);
                    }

                    return CompletableFuture.completedFuture(null);
                });
        return created;
    }

    private CompletableFuture<Void> createDomainEventBus() {
        logger.info("creating domain event bus...");

        eventBus = new DomainEventBus();

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> createUIDService(final AbstractHBaseClient aClient) {
        logger.info("creating chat id service");

        idService = new HBaseUIDService(aClient);

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> createRepos(final AbstractHBaseClient aClient) {
        logger.info("creating chat repositories");

        messageRepo = new HBaseChannelMessageRepository(aClient, idService, subscriptionService);

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> createChannelService(final DomainEventBus aEventBus,
                                                         final AbstractTopicMessagePublisher aMessagePublisher,
                                                         final AbstractChannelMessageRepository aMessageRepo,
                                                         final ApplicationService aAppService,
                                                         final TranslationService aTranslationService,
                                                         final ContentModerationService aContentModerationService,
                                                         final AbstractAuthorizationService aAuthService,
                                                         final AbstractUserPresenceService aPresenceService,
                                                         final AbstractPushNotificationService aPushNotificationService,
                                                         final RoleName aMutesRole,
                                                         final RoleName aBansRole) {
        logger.info("creating channel application service");

        channelService = new ChannelApplicationService(
                aEventBus,
                aMessagePublisher,
                aMessageRepo,
                aAppService,
                aTranslationService,
                aContentModerationService,
                aAuthService,
                subscriptionService,
                aPresenceService,
                userNotificationService,
                aPushNotificationService,
                aMutesRole,
                aBansRole);

        return channelService.open();
    }
}
