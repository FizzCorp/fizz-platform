package io.fizz.gateway.http;

import io.fizz.chat.application.channel.AbstractTranslationClient;
import io.fizz.chat.infrastructure.services.MockTranslationClient;
import io.fizz.chat.moderation.infrastructure.bootstrap.ChatModerationComponent;
import io.fizz.chat.moderation.infrastructure.bootstrap.MockChatModerationComponent;
import io.fizz.gateway.http.auth.AuthenticatedUser;
import io.fizz.gateway.http.auth.SignatureAuthHandler;
import io.fizz.gateway.http.services.handler.eventstream.AbstractEventStreamClientHandler;
import io.fizz.gateway.http.verticles.HTTPVerticle;
import io.fizz.gateway.services.MockUSDConversionService;
import io.fizz.gateway.services.USDConversionService;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class InternalApplication extends Application {
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(InternalApplication.class.getName());
    }

    @Override
    protected HTTPVerticle buildHTTPService() {
        return new InternalHttpVerticle(API_PORT, API_PORT_INTERNAL, eventStreamHandler, rpcServiceHost, PROXY_PORT);
    }

    private static class InternalHttpVerticle extends HTTPVerticle {
        InternalHttpVerticle(int aPort,
                             int aPortInternal,
                             AbstractEventStreamClientHandler aEventStreamHandler,
                             String aRPCServiceHost,
                             int aRPCServicePort) {
            super(aPort, aPortInternal, aEventStreamHandler, aRPCServiceHost, aRPCServicePort);
        }

        @Override
        protected USDConversionService currencyConversionService() {
            return new MockUSDConversionService();
        }

        @Override
        protected Handler<RoutingContext> buildSignatureAuthHandler() {
            return new SignatureAuthHandler((jsonObject, handler) -> {
                String appId = jsonObject.getString("appId");
                handler.handle(Future.succeededFuture(new AuthenticatedUser(appId, "system")));
            });
        }

        @Override
        protected ChatModerationComponent buildChatModerationComponent() {
            return new MockChatModerationComponent();
        }

        @Override
        protected AbstractTranslationClient createTranslationClient() {
            return new MockTranslationClient();
        }
    }
}
