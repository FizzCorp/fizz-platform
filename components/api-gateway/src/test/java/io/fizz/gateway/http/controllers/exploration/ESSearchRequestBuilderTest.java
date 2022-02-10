package io.fizz.gateway.http.controllers.exploration;

import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.Platform;
import io.fizz.common.domain.QueryRange;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ESSearchRequestBuilderTest {
    private static final String appId = "appA";
    private static final long startTs = 1525794895L;
    private static final long endTs = 1525794897L;
    private static final int offset = 0;
    private static final int pageSize = 10;
    private static final String text = "Hello";
    private static final String phrase = "Hello World";
    private static final String countryCode = "PK";
    private static final String channelId = "roomA";
    private static final String userId = "userA";
    private static final String userNick = "Demo";
    private static final String platform = "android";
    private static final String build = "1.0";
    private static final String custom01 = "custom01";
    private static final String custom02 = "custom02";
    private static final String custom03 = "custom03";
    private static final String age = "days_1_3";
    private static final String spend = "none";
    private static final double sentimentScoreDouble = 0.5;
    private static final String sentimentScoreJson = new JsonObject().put("from", -0.5).put("to", 0.5).toString();
    private static final String sentimentScoreJsonFrom = new JsonObject().put("from", -0.5).toString();
    private static final String sentimentScoreJsonTo = new JsonObject().put("to", 0.5).toString();

    @Test
    @DisplayName("it should build es search request for sentiment double value")
    void validQueryDoubleSentiment() throws DomainErrorException {
        final SearchRequest request = new ESSearchRequestBuilder(new ApplicationId(appId))
                .setRange(new QueryRange(startTs, endTs))
                .setOffset(offset)
                .setPageSize(pageSize)
                .setText(text)
                .setPhrase(phrase)
                .setCountryCode(countryCode)
                .setChannelId(channelId)
                .setSenderId(userId)
                .setSenderNick(userNick)
                .setPlatform(new Platform(platform))
                .setBuild(build)
                .setCustom01(custom01)
                .setCustom02(custom02)
                .setCustom03(custom03)
                .setAge(age)
                .setSpender(spend)
                .setSentimentScore(String.valueOf(sentimentScoreDouble))
                .setOp(ESSearchRequestBuilder.ComparisonOp.EQ)
                .setSort(SortOrder.ASC)
                .build();

        Assertions.assertNotNull(request);
    }

    @Test
    @DisplayName("it should build es search request for sentiment Json Value")
    void validQueryJsonSentiment() throws DomainErrorException {
        final SearchRequest request = new ESSearchRequestBuilder(new ApplicationId(appId))
                .setRange(new QueryRange(startTs, endTs))
                .setOffset(offset)
                .setPageSize(pageSize)
                .setText(text)
                .setPhrase(phrase)
                .setCountryCode(countryCode)
                .setChannelId(channelId)
                .setSenderId(userId)
                .setSenderNick(userNick)
                .setPlatform(new Platform(platform))
                .setBuild(build)
                .setCustom01(custom01)
                .setCustom02(custom02)
                .setCustom03(custom03)
                .setAge(age)
                .setSpender(spend)
                .setSentimentScore(String.valueOf(sentimentScoreJson))
                .setOp(ESSearchRequestBuilder.ComparisonOp.BTW)
                .setSort(SortOrder.ASC)
                .build();

        Assertions.assertNotNull(request);
    }

    @Test
    @DisplayName("it should not build es search request for invalid sentiment double value")
    void invalidQueryDoubleSentiment() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new ESSearchRequestBuilder(new ApplicationId(appId))
                        .setRange(new QueryRange(startTs, endTs))
                        .setOffset(offset)
                        .setPageSize(pageSize)
                        .setText(text)
                        .setPhrase(phrase)
                        .setCountryCode(countryCode)
                        .setChannelId(channelId)
                        .setSenderId(userId)
                        .setSenderNick(userNick)
                        .setPlatform(new Platform(platform))
                        .setBuild(build)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setAge(age)
                        .setSpender(spend)
                        .setSentimentScore(String.valueOf(sentimentScoreDouble))
                        .setOp(ESSearchRequestBuilder.ComparisonOp.BTW)
                        .setSort(SortOrder.ASC)
                        .build();
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_sentiment_score_format");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should build not es search request for invalid sentiment Json Value")
    void invalidQueryJsonSentiment() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new ESSearchRequestBuilder(new ApplicationId(appId))
                        .setRange(new QueryRange(startTs, endTs))
                        .setOffset(offset)
                        .setPageSize(pageSize)
                        .setText(text)
                        .setPhrase(phrase)
                        .setCountryCode(countryCode)
                        .setChannelId(channelId)
                        .setSenderId(userId)
                        .setSenderNick(userNick)
                        .setPlatform(new Platform(platform))
                        .setBuild(build)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setAge(age)
                        .setSpender(spend)
                        .setSentimentScore(String.valueOf(sentimentScoreJson))
                        .setOp(ESSearchRequestBuilder.ComparisonOp.EQ)
                        .setSort(SortOrder.ASC)
                        .build();

            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_sentiment_score_format");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should not build es search request for empty sentiment json")
    void invalidQueryJsonEmptySentiment() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new ESSearchRequestBuilder(new ApplicationId(appId))
                        .setRange(new QueryRange(startTs, endTs))
                        .setOffset(offset)
                        .setPageSize(pageSize)
                        .setText(text)
                        .setPhrase(phrase)
                        .setCountryCode(countryCode)
                        .setChannelId(channelId)
                        .setSenderId(userId)
                        .setSenderNick(userNick)
                        .setPlatform(new Platform(platform))
                        .setBuild(build)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setAge(age)
                        .setSpender(spend)
                        .setSentimentScore(new JsonObject().toString())
                        .setOp(ESSearchRequestBuilder.ComparisonOp.BTW)
                        .setSort(SortOrder.ASC)
                        .build();
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_sentiment_score_format");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should build es search request for sentiment Json from Value")
    void validQueryJsonSentimentFrom() throws DomainErrorException {
        final SearchRequest request = new ESSearchRequestBuilder(new ApplicationId(appId))
                .setRange(new QueryRange(startTs, endTs))
                .setOffset(offset)
                .setPageSize(pageSize)
                .setText(text)
                .setPhrase(phrase)
                .setCountryCode(countryCode)
                .setChannelId(channelId)
                .setSenderId(userId)
                .setSenderNick(userNick)
                .setPlatform(new Platform(platform))
                .setBuild(build)
                .setCustom01(custom01)
                .setCustom02(custom02)
                .setCustom03(custom03)
                .setAge(age)
                .setSpender(spend)
                .setSentimentScore(String.valueOf(sentimentScoreJsonFrom))
                .setOp(ESSearchRequestBuilder.ComparisonOp.BTW)
                .setSort(SortOrder.ASC)
                .build();

        Assertions.assertNotNull(request);

        RangeQueryBuilder sentimentRangeQueryBuilder = getSentimentRangeQueryBuilder(request);

        Assertions.assertNotNull(sentimentRangeQueryBuilder);

        Assertions.assertEquals(-0.5, sentimentRangeQueryBuilder.from());
        Assertions.assertEquals(1.0, sentimentRangeQueryBuilder.to());
    }

    @Test
    @DisplayName("it should build es search request for sentiment Json to Value")
    void validQueryJsonSentimentTo() throws DomainErrorException {
        final SearchRequest request = new ESSearchRequestBuilder(new ApplicationId(appId))
                .setRange(new QueryRange(startTs, endTs))
                .setOffset(offset)
                .setPageSize(pageSize)
                .setText(text)
                .setPhrase(phrase)
                .setCountryCode(countryCode)
                .setChannelId(channelId)
                .setSenderId(userId)
                .setSenderNick(userNick)
                .setPlatform(new Platform(platform))
                .setBuild(build)
                .setCustom01(custom01)
                .setCustom02(custom02)
                .setCustom03(custom03)
                .setAge(age)
                .setSpender(spend)
                .setSentimentScore(String.valueOf(sentimentScoreJsonTo))
                .setOp(ESSearchRequestBuilder.ComparisonOp.BTW)
                .setSort(SortOrder.ASC)
                .build();

        Assertions.assertNotNull(request);

        RangeQueryBuilder sentimentRangeQueryBuilder = getSentimentRangeQueryBuilder(request);

        Assertions.assertNotNull(sentimentRangeQueryBuilder);

        Assertions.assertEquals(-1.0, sentimentRangeQueryBuilder.from());
        Assertions.assertEquals(0.5, sentimentRangeQueryBuilder.to());
    }

    @Test
    @DisplayName("it should not build es search request for missing range")
    void invalidQueryNullApplicationId() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new ESSearchRequestBuilder(null)
                        .setOffset(offset)
                        .setPageSize(pageSize)
                        .setText(text)
                        .setPhrase(phrase)
                        .setCountryCode(countryCode)
                        .setChannelId(channelId)
                        .setSenderId(userId)
                        .setSenderNick(userNick)
                        .setPlatform(new Platform(platform))
                        .setBuild(build)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setAge(age)
                        .setSpender(spend)
                        .setSentimentScore(String.valueOf(sentimentScoreDouble))
                        .setOp(ESSearchRequestBuilder.ComparisonOp.EQ)
                        .setSort(SortOrder.ASC)
                        .build();
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_app_id");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should not build es search request for missing range")
    void invalidQueryMissingRange() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new ESSearchRequestBuilder(new ApplicationId(appId))
                        .setOffset(offset)
                        .setPageSize(pageSize)
                        .setText(text)
                        .setPhrase(phrase)
                        .setCountryCode(countryCode)
                        .setChannelId(channelId)
                        .setSenderId(userId)
                        .setSenderNick(userNick)
                        .setPlatform(new Platform(platform))
                        .setBuild(build)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setAge(age)
                        .setSpender(spend)
                        .setSentimentScore(String.valueOf(sentimentScoreDouble))
                        .setOp(ESSearchRequestBuilder.ComparisonOp.EQ)
                        .setSort(SortOrder.ASC)
                        .build();
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_query_range");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should build es search request for missing optional params")
    void validQueryMissingOptionals() throws DomainErrorException {
        final SearchRequest request = new ESSearchRequestBuilder(new ApplicationId(appId))
                .setRange(new QueryRange(startTs, endTs))
                .build();

        Assertions.assertNotNull(request);
    }

    @Test
    @DisplayName("it should build es search request for null optional params")
    void validQueryNullOptionals() throws DomainErrorException {
        final SearchRequest request = new ESSearchRequestBuilder(new ApplicationId(appId))
                .setRange(new QueryRange(startTs, endTs))
                .setOffset(null)
                .setPageSize(null)
                .setText(null)
                .setPhrase(null)
                .setCountryCode(null)
                .setChannelId(null)
                .setSenderId(null)
                .setSenderNick(null)
                .setPlatform(null)
                .setBuild(null)
                .setCustom01(null)
                .setCustom02(null)
                .setCustom03(null)
                .setAge(null)
                .setSpender(null)
                .setSentimentScore(null)
                .setOp(null)
                .setSort(null)
                .build();

        Assertions.assertNotNull(request);
    }

    @Test
    @DisplayName("it should build es search request for large text")
    void invalidQueryLargeText() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            final String LARGE_ID_65 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-+1";
            StringBuilder largeText = new StringBuilder(LARGE_ID_65);
            for (int i = 2048 / LARGE_ID_65.length(); i >= 0; i--) {
                largeText.append(LARGE_ID_65);
            }

            try {
                new ESSearchRequestBuilder(new ApplicationId(appId))
                        .setRange(new QueryRange(startTs, endTs))
                        .setOffset(offset)
                        .setPageSize(pageSize)
                        .setText(largeText.toString())
                        .setPhrase(phrase)
                        .setCountryCode(countryCode)
                        .setChannelId(channelId)
                        .setSenderId(userId)
                        .setSenderNick(userNick)
                        .setPlatform(new Platform(platform))
                        .setBuild(build)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setAge(age)
                        .setSpender(spend)
                        .setSentimentScore(String.valueOf(sentimentScoreDouble))
                        .setOp(ESSearchRequestBuilder.ComparisonOp.EQ)
                        .setSort(SortOrder.ASC)
                        .build();
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_search_text");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should build es search request for large phrase")
    void invalidQueryLargePhrase() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            final String LARGE_ID_65 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-+1";
            StringBuilder largePhrase = new StringBuilder(LARGE_ID_65);
            for (int i = 2048 / LARGE_ID_65.length(); i >= 0; i--) {
                largePhrase.append(LARGE_ID_65);
            }

            try {
                new ESSearchRequestBuilder(new ApplicationId(appId))
                        .setRange(new QueryRange(startTs, endTs))
                        .setOffset(offset)
                        .setPageSize(pageSize)
                        .setText(text)
                        .setPhrase(largePhrase.toString())
                        .setCountryCode(countryCode)
                        .setChannelId(channelId)
                        .setSenderId(userId)
                        .setSenderNick(userNick)
                        .setPlatform(new Platform(platform))
                        .setBuild(build)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setAge(age)
                        .setSpender(spend)
                        .setSentimentScore(String.valueOf(sentimentScoreDouble))
                        .setOp(ESSearchRequestBuilder.ComparisonOp.EQ)
                        .setSort(SortOrder.ASC)
                        .build();
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_search_phrase");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should not build es search request for invalid min page size")
    void invalidQueryMinPageSize() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new ESSearchRequestBuilder(new ApplicationId(appId))
                        .setRange(new QueryRange(startTs, endTs))
                        .setOffset(offset)
                        .setPageSize(-1)
                        .setText(text)
                        .setPhrase(phrase)
                        .setCountryCode(countryCode)
                        .setChannelId(channelId)
                        .setSenderId(userId)
                        .setSenderNick(userNick)
                        .setPlatform(new Platform(platform))
                        .setBuild(build)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setAge(age)
                        .setSpender(spend)
                        .setSentimentScore(String.valueOf(sentimentScoreDouble))
                        .setOp(ESSearchRequestBuilder.ComparisonOp.EQ)
                        .setSort(SortOrder.ASC)
                        .build();
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_page_size");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should not build es search request for invalid min page size")
    void invalidQueryMaxPageSize() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new ESSearchRequestBuilder(new ApplicationId(appId))
                        .setRange(new QueryRange(startTs, endTs))
                        .setOffset(offset)
                        .setPageSize(26)
                        .setText(text)
                        .setPhrase(phrase)
                        .setCountryCode(countryCode)
                        .setChannelId(channelId)
                        .setSenderId(userId)
                        .setSenderNick(userNick)
                        .setPlatform(new Platform(platform))
                        .setBuild(build)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setAge(age)
                        .setSpender(spend)
                        .setSentimentScore(String.valueOf(sentimentScoreDouble))
                        .setOp(ESSearchRequestBuilder.ComparisonOp.EQ)
                        .setSort(SortOrder.ASC)
                        .build();
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_page_size");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should not build es search request for invalid offset")
    void invalidQueryOffset() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new ESSearchRequestBuilder(new ApplicationId(appId))
                        .setRange(new QueryRange(startTs, endTs))
                        .setOffset(9991)
                        .setPageSize(pageSize)
                        .setText(text)
                        .setPhrase(phrase)
                        .setCountryCode(countryCode)
                        .setChannelId(channelId)
                        .setSenderId(userId)
                        .setSenderNick(userNick)
                        .setPlatform(new Platform(platform))
                        .setBuild(build)
                        .setCustom01(custom01)
                        .setCustom02(custom02)
                        .setCustom03(custom03)
                        .setAge(age)
                        .setSpender(spend)
                        .setSentimentScore(String.valueOf(sentimentScoreDouble))
                        .setOp(ESSearchRequestBuilder.ComparisonOp.EQ)
                        .setSort(SortOrder.ASC)
                        .build();
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_cursor_offset");
                throw ex;
            }
        });
    }

    private RangeQueryBuilder getSentimentRangeQueryBuilder(SearchRequest request) {
        List<QueryBuilder> queryBuilders = ((BoolQueryBuilder) request.source().query()).filter();
        for (QueryBuilder queryBuilder: queryBuilders) {
            if (queryBuilder instanceof RangeQueryBuilder) {
                RangeQueryBuilder rangeQueryBuilder = (RangeQueryBuilder) queryBuilder;
                if (rangeQueryBuilder.fieldName().equals("sentimentScore")) {
                    return rangeQueryBuilder;
                }
            }
        }
        return null;
    }
}
