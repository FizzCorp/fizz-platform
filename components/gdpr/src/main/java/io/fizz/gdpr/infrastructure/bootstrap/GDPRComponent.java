package io.fizz.gdpr.infrastructure.bootstrap;

import io.fizz.common.Config;
import io.fizz.common.LoggingService;
import io.fizz.gdpr.application.repository.AbstractGDPRRequestRepository;
import io.fizz.gdpr.application.service.GDPRService;
import io.fizz.gdpr.infrastructure.repository.ESGDPRRequestRepository;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;

public class GDPRComponent {
    private static final LoggingService.Log logger = LoggingService.getLogger(GDPRComponent.class);

    private static final Config config = new Config("chat-moderation");

    private GDPRService gdprService;

    public CompletableFuture<Void> open() {
        return createServices();
    }

    public GDPRService GDPRService() {
        return gdprService;
    }

    private CompletableFuture<Void> createServices() {
        logger.info("creating gdpr service");
        return createGDPRService();
    }

    private CompletableFuture<Void> createGDPRService() {
        AbstractGDPRRequestRepository gdprRepo = gdprRequestRepository();
        gdprService = new GDPRService(gdprRepo);
        return CompletableFuture.completedFuture(null);
    }

    AbstractGDPRRequestRepository gdprRequestRepository() {
        return new ESGDPRRequestRepository();
    }
}
