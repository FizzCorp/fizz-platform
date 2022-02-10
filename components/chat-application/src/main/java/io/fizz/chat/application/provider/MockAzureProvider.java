package io.fizz.chat.application.provider;

import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.common.domain.ApplicationId;
import io.vertx.ext.web.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MockAzureProvider extends AzureProvider {
    private static final List<String> FILTERED_WORDS = new ArrayList<String>() {{
        add("Filter 1");
        add("Filter 3");
        add("Filter 5");
    }};

    public MockAzureProvider(final AbstractProviderConfig aConfig) {
        super(aConfig);
    }

    @Override
    public CompletableFuture<String> filter(final ApplicationId aAppId,
                                            final WebClient aClient,
                                            final String aText) {

        return CompletableFuture.completedFuture(filteredText(FILTERED_WORDS, aText));
    }
}
