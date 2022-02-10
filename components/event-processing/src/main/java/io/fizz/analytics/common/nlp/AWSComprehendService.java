package io.fizz.analytics.common.nlp;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.*;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import io.fizz.analytics.domain.Keyword;
import io.fizz.analytics.domain.Sentiment;
import io.fizz.analytics.domain.TextAnalysisResult;
import io.fizz.common.LoggingService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AWSComprehendService implements AbstractTextAnalysisService, Serializable {
    private final LoggingService.Log logger = LoggingService.getLogger(AWSComprehendService.class);

    private final String region;

    public AWSComprehendService(final String aRegion) {
        if (Objects.isNull(aRegion) || aRegion.length() <= 0) {
            throw new IllegalArgumentException("invalid Amazon Comprehend region");
        }

        region = aRegion;
    }

    @Override
    public CompletableFuture<TextAnalysisResult> analyze(String aText) {
        if (Objects.isNull(aText) || aText.length() < 5) {
            return CompletableFuture.completedFuture(null);
        }

        AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();
        final AmazonComprehend comprehendClient = AmazonComprehendClientBuilder.standard()
                .withCredentials(awsCreds)
                .withRegion(region)
                .build();

        // Call detectSentiment API
        DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest().withText(aText)
                .withLanguageCode("en");
        DetectSentimentResult detectSentimentResult = comprehendClient.detectSentiment(detectSentimentRequest);

        SentimentScore sentimentScore = detectSentimentResult.getSentimentScore();
        float positiveScore = sentimentScore.getPositive();
        float negativeScore = sentimentScore.getNegative();
        float neutralScore = sentimentScore.getNeutral();

        // Sentiment Score Formula
        // Negative Range -1.0>= x <= -0.75
        // Neutral Range -.75> x <0.75
        // Positive Range 0.75>= x <=1.0

        float score;

        if (positiveScore == Math.max(positiveScore, Math.max(negativeScore, neutralScore))) {
            score = 0.75f + 0.25f * positiveScore;
        } else if (negativeScore == Math.max(positiveScore, Math.max(negativeScore, neutralScore))) {
            score = -0.75f + -0.25f * negativeScore;
        } else if (positiveScore > negativeScore) {
            score = 0.75f * neutralScore;
        } else {
            score = -0.75f * neutralScore;
        }

        Sentiment textSentiment = new Sentiment(Double.valueOf(String.valueOf(score > 1.0f ? 1.0f : score)));

        // Call detectKeyPhrases API
        DetectKeyPhrasesRequest detectKeyPhrasesRequest = new DetectKeyPhrasesRequest().withText(aText)
                .withLanguageCode("en");
        DetectKeyPhrasesResult detectKeyPhrasesResult = comprehendClient.detectKeyPhrases(detectKeyPhrasesRequest);

        List<Keyword> keywords = new ArrayList<>();
        if (!Objects.isNull(detectKeyPhrasesResult.getKeyPhrases())) {
            for (final KeyPhrase phrase : detectKeyPhrasesResult.getKeyPhrases()) {
                final String keywordText = phrase.getText();
                final float keywordScore = phrase.getScore();
                final Sentiment keywordSentiment = new Sentiment(Double.valueOf(String.valueOf(keywordScore > 1.0f ? 1.0f : keywordScore)));
                Keyword keyword = new Keyword(keywordText, keywordSentiment);
                keywords.add(keyword);
            }
        }

        TextAnalysisResult textAnalysis = new TextAnalysisResult(textSentiment, keywords);
        return CompletableFuture.completedFuture(textAnalysis);
    }
}
