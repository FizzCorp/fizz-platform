package io.fizz.chat.application.services;

import io.fizz.chat.application.channel.AbstractTranslationClient;
import io.fizz.chat.infrastructure.translation.HystrixTranslationClient;
import io.fizz.chatcommon.domain.LanguageCode;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TranslationService {
    private final AbstractTranslationClient client;

    public TranslationService(final AbstractTranslationClient aClient) {
        client = aClient;
    }

    public CompletableFuture<Map<LanguageCode,String>> translate(final String aText, final LanguageCode aFrom, final LanguageCode[] aTo) {
        return new HystrixTranslationClient(client)
                .translate(aText, aFrom, aTo);
    }
}
