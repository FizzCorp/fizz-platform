package io.fizz.chat.application.channel;

import io.fizz.chatcommon.domain.LanguageCode;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface AbstractTranslationClient {
    CompletableFuture<Map<LanguageCode,String>> translate(final String aText, final LanguageCode from, LanguageCode[] aTo);
}
