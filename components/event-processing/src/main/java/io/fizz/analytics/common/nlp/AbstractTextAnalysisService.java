package io.fizz.analytics.common.nlp;

import io.fizz.analytics.domain.TextAnalysisResult;

import java.util.concurrent.CompletableFuture;

public interface AbstractTextAnalysisService {
    CompletableFuture<TextAnalysisResult> analyze(final String aText);
}
