package io.fizz.chat.application.provider;

import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.chatcommon.infrastructure.ConfigService;
import io.fizz.common.Config;
import io.fizz.common.domain.ApplicationId;
import io.vertx.ext.web.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class MockCleanSpeakProvider extends CleanSpeakProvider {

    private static final Config CONFIG = ConfigService.config();
    private static final String CONTENT_REPLACE_STRING = CONFIG.getString("content.moderation.replacement.string");

    private static final List<String> FILTERED_WORDS = new ArrayList<String>() {{
        add("Filter 1");
        add("Filter 3");
        add("Filter 5");
    }};

    public MockCleanSpeakProvider(final AbstractProviderConfig aConfig) {
        super(aConfig);
    }

    @Override
    public CompletableFuture<String> filter(final ApplicationId aAppId,
                                            final WebClient aClient,
                                            final String aText) {

        return CompletableFuture.completedFuture(filteredText(FILTERED_WORDS, aText));
    }

    private String filteredText(final List<String> aTerms,
                        final String aText) {

        String filteredText = aText;
        for (int i = aTerms.size() - 1; i >= 0; i--) {
            String term = aTerms.get(i);
            if (Objects.nonNull(term)) {
                String caseInsensitiveTerm = String.format("(?i)%s", Pattern.quote(term));
                filteredText = filteredText.replaceAll(caseInsensitiveTerm, CONTENT_REPLACE_STRING);
            }
        }
        return filteredText;
    }
}

