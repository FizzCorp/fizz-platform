package io.fizz.chat.moderation.application.service;

import io.fizz.chat.application.impl.ApplicationService;
import io.vertx.core.Vertx;

public class MockContentModerationService extends ContentModerationService {

    public MockContentModerationService(final ApplicationService aAppService,
                                        final Vertx aVertx) {

        super(aAppService, aVertx);
    }


}
