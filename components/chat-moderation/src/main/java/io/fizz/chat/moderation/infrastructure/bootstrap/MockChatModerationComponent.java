package io.fizz.chat.moderation.infrastructure.bootstrap;

import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.moderation.application.repository.AbstractChatModerationRepository;
import io.fizz.chat.moderation.application.service.MockContentModerationService;
import io.fizz.chat.moderation.infrastructure.repository.MockChatModerationRepository;
import io.fizz.chat.moderation.application.service.ContentModerationService;
import io.vertx.core.Vertx;

public class MockChatModerationComponent extends ChatModerationComponent {
    @Override
    AbstractChatModerationRepository chatModerationRepository() {
        return new MockChatModerationRepository();
    }

    @Override
    ContentModerationService contentModerationService(final ApplicationService aAppService,
                                                      final Vertx aVertx) {
        return new MockContentModerationService(aAppService, aVertx);
    }
}
