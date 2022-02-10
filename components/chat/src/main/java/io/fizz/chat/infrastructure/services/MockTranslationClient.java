package io.fizz.chat.infrastructure.services;

import io.fizz.chat.application.channel.AbstractTranslationClient;
import io.fizz.chatcommon.domain.LanguageCode;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MockTranslationClient implements AbstractTranslationClient {
    private final List<TranslationContainer> translations = new ArrayList<TranslationContainer>() {{
        add(new TranslationContainer(LanguageCode.ENGLISH, LanguageCode.ENGLISH, "Hello1", "Hello1_en_en"));
        add(new TranslationContainer(LanguageCode.ENGLISH, LanguageCode.FRENCH, "Hello1", "Hello1_en_fr"));
        add(new TranslationContainer(LanguageCode.ENGLISH, LanguageCode.SPANISH, "Hello1", "Hello1_en_es"));
        add(new TranslationContainer(LanguageCode.ENGLISH, LanguageCode.ARABIC, "Hello1", "Hello1_en_ar"));

        add(new TranslationContainer(LanguageCode.ENGLISH, LanguageCode.FRENCH, "Hello2", "Hello2_en_fr"));
        add(new TranslationContainer(LanguageCode.ENGLISH, LanguageCode.SPANISH, "Hello2", "Hello2_en_es"));
        add(new TranslationContainer(LanguageCode.ENGLISH, LanguageCode.ARABIC, "Hello2", "Hello2_en_ar"));

        add(new TranslationContainer(LanguageCode.ENGLISH, LanguageCode.FRENCH, "Hello3", "Hello3_en_fr"));
        add(new TranslationContainer(LanguageCode.ENGLISH, LanguageCode.SPANISH, "Hello3", "Hello3_en_es"));
        add(new TranslationContainer(LanguageCode.ENGLISH, LanguageCode.ARABIC, "Hello3", "Hello3_en_ar"));

        add(new TranslationContainer(LanguageCode.FRENCH, LanguageCode.FRENCH, "Hello1", "Hello1_fr_fr"));
        add(new TranslationContainer(LanguageCode.FRENCH, LanguageCode.ENGLISH, "Hello1", "Hello1_fr_en"));
        add(new TranslationContainer(LanguageCode.FRENCH, LanguageCode.SPANISH, "Hello1", "Hello1_fr_es"));
        add(new TranslationContainer(LanguageCode.FRENCH, LanguageCode.ARABIC, "Hello1", "Hello1_fr_ar"));
    }};

    @Override
    public CompletableFuture<Map<LanguageCode, String>> translate(final String aText,
                                                                  final LanguageCode aFrom,
                                                                  final LanguageCode[] aTo) {
        final Map<LanguageCode,String> translations = new HashMap<>();

        for (TranslationContainer translation: this.translations) {
            final LanguageCode from = Objects.isNull(aFrom) ? LanguageCode.ENGLISH : aFrom;

            if (from.equals(translation.source) &&
                    aText.equalsIgnoreCase(translation.text) &&
                    Arrays.asList(aTo).contains(translation.target)) {
                translations.put(translation.target, translation.translation);
            }
        }

        return CompletableFuture.completedFuture(translations);
    }

    private static class TranslationContainer {
        private final LanguageCode source;
        private final LanguageCode target;
        private final String text;
        private final String translation;

        public TranslationContainer(final LanguageCode source, final LanguageCode target,
                                    final String text, final String translation) {
            this.source = source;
            this.target = target;
            this.text = text;
            this.translation = translation;
        }
    }
}
