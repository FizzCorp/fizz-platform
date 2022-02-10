package io.fizz.analytics.common.nlp;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import io.fizz.analytics.domain.Sentiment;
import io.fizz.analytics.domain.TextAnalysisResult;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class MockTextAnalysisService implements AbstractTextAnalysisService {

    @Override
    public CompletableFuture<TextAnalysisResult> analyze(String aText) {
        return CompletableFuture.completedFuture(new TextAnalysisResult(new Sentiment(0d), new ArrayList<>()));
    }
}