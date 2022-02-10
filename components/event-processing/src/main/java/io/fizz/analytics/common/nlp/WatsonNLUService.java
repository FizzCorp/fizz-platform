package io.fizz.analytics.common.nlp;

import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import io.fizz.analytics.domain.Keyword;
import io.fizz.analytics.domain.Sentiment;
import io.fizz.analytics.domain.TextAnalysisResult;
import io.fizz.common.LoggingService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class WatsonNLUService implements AbstractTextAnalysisService, Serializable {
    private final LoggingService.Log logger = LoggingService.getLogger(WatsonNLUService.class);

    private final String apiKey;

    public WatsonNLUService(final String aApiKey) {
        if (Objects.isNull(aApiKey) || aApiKey.length() <= 0) {
            throw new IllegalArgumentException("invalid Watson NLU api key");
        }

        apiKey = aApiKey;
    }

    @Override
    public CompletableFuture<TextAnalysisResult> analyze(String aText) {
        if (Objects.isNull(aText) || aText.length() < 5) {
            return CompletableFuture.completedFuture(null);
        }

        IamOptions options = new IamOptions.Builder()
                .apiKey(apiKey)
                .build();

        final NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding("2017-11-07", options);

        final KeywordsOptions keywords = new KeywordsOptions.Builder()
        .sentiment(true)
        .emotion(true)
        .build();

        final SentimentOptions sentimentOpts = new SentimentOptions.Builder().document(true).build();

        final Features features = new Features.Builder()
        .keywords(keywords)
        .sentiment(sentimentOpts)
        .build();

        final AnalyzeOptions opts = new AnalyzeOptions.Builder()
        .text(aText)
        .features(features)
        .build();

        final CompletableFuture<TextAnalysisResult> future = new CompletableFuture<>();
        service.analyze(opts).enqueue(new ServiceCallback<AnalysisResults>() {
            @Override
            public void onResponse(AnalysisResults analysisResults) {
                Sentiment textSentiment = new Sentiment(analysisResults.getSentiment().getDocument().getScore());
                List<Keyword> keywords = new ArrayList<>();
                if (!Objects.isNull(analysisResults.getKeywords())) {
                    for (final KeywordsResult keywordRes : analysisResults.getKeywords()) {
                        final String keywordText = keywordRes.getText();
                        final double sentimentScore = Objects.isNull(keywordRes.getSentiment()) ? 0.0 : keywordRes.getSentiment().getScore();
                        final Sentiment keywordSentiment = new Sentiment(sentimentScore);
                        Keyword keyword = new Keyword(keywordText, keywordSentiment);
                        keywords.add(keyword);
                    }
                }

                TextAnalysisResult textAnalysis = new TextAnalysisResult(textSentiment, keywords);
                future.complete(textAnalysis);
            }

            @Override
            public void onFailure(Exception e) {
                logger.debug(e.getMessage() + " [" + aText + "]");
                future.complete(null);
            }
        });
        return future;
    }
}
