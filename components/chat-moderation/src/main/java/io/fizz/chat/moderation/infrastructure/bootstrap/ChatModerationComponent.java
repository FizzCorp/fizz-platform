package io.fizz.chat.moderation.infrastructure.bootstrap;

import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.moderation.application.repository.AbstractChatModerationRepository;
import io.fizz.chat.moderation.application.service.ChatModerationService;
import io.fizz.chat.moderation.application.service.ContentModerationService;
import io.fizz.chat.moderation.infrastructure.repository.ESChatModerationRepository;
import io.fizz.common.Config;
import io.fizz.common.LoggingService;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;

public class ChatModerationComponent {
    private static final LoggingService.Log logger = LoggingService.getLogger(LoggingService.class);

    private static final Config config = new Config("chat-moderation");
    private static final String HBASE_NAMESPACE = config.getString("chat.moderation.hbase.namespace");

    private ChatModerationService chatModService;
    private ContentModerationService contentModService;
    private ApplicationService appService;

    public CompletableFuture<Void> open(final Vertx aVertx,
                                        final ApplicationService aAppService) {
        this.appService = aAppService;

        return createServices(aVertx);
    }

    public ChatModerationService chatModerationService() {
        return chatModService;
    }

    public ContentModerationService contentModerationService() {
        return contentModService;
    }

    private CompletableFuture<Void> createServices(final Vertx aVertx) {
        logger.info("creating moderation services");
        return createChatModService()
                .thenCompose(v -> createContentChatModService(aVertx));
    }

    private CompletableFuture<Void> createChatModService() {
        AbstractChatModerationRepository chatModRepo = chatModerationRepository();
        chatModService = new ChatModerationService(chatModRepo);
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> createContentChatModService(final Vertx aVertx) {
        contentModService = contentModerationService(appService, aVertx);
        return CompletableFuture.completedFuture(null);
    }

    AbstractChatModerationRepository chatModerationRepository() {
        return new ESChatModerationRepository();
    }

    // It will be removed from here and then will be created in ApplicationService in Chat Application comp
    ContentModerationService contentModerationService(final ApplicationService aAppService,
                                                      final Vertx aVertx) {
        return new ContentModerationService(aAppService, aVertx);
    }
}
