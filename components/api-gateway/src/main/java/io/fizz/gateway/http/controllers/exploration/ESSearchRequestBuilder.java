package io.fizz.gateway.http.controllers.exploration;

import io.fizz.chat.moderation.infrastructure.model.ReportedMessageESRequestBuilder;
import io.fizz.common.ConfigService;
import io.fizz.common.domain.*;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Objects;

public class ESSearchRequestBuilder {
    public enum ComparisonOp {
        LT,
        LTE,
        GT,
        GTE,
        EQ,
        BTW
    }

    private static DomainErrorException ERROR_INVALID_QUERY_RANGE = new DomainErrorException(new DomainError("invalid_query_range"));
    private static DomainErrorException ERROR_INVALID_SEARCH_TEXT = new DomainErrorException(new DomainError("invalid_search_text"));
    private static DomainErrorException ERROR_INVALID_SEARCH_PHRASE = new DomainErrorException(new DomainError("invalid_search_phrase"));
    private static DomainErrorException ERROR_INVALID_PAGE_SIZE = new DomainErrorException(new DomainError("invalid_page_size"));
    private static DomainErrorException ERROR_INVALID_CURSOR_OFFSET = new DomainErrorException(new DomainError("invalid_cursor_offset"));
    private static DomainErrorException ERROR_INVALID_SENTIMENT_SCORE_FORMAT = new DomainErrorException(new DomainError("invalid_sentiment_score_format"));
    private static int PAGE_SIZE_MAX = ConfigService.instance().getNumber("es.page.size.max").intValue();
    private static int PAGE_SIZE_MIN = ConfigService.instance().getNumber("es.page.size.min").intValue();
    private static String MESSAGES_INDEX = ConfigService.instance().getString("es.messages.index");
    private static long RESULT_SIZE_LIMIT = ConfigService.instance().getNumber("es.result.size.limit").longValue();
    private static int AGGREGATION_RESULT_SIZE_MAX_LIMIT = ConfigService.instance().getNumber("es.words.aggregation.result.size.limit").intValue();

    private ApplicationId appId;
    private QueryRange range;
    private int offset = 0;
    private int pageSize = PAGE_SIZE_MAX;
    private SortOrder sort;
    private String text;
    private String phrase;
    private String countryCode;
    private String channelId;
    private String senderId;
    private String senderNick;
    private Platform platform;
    private String build;
    private String custom01;
    private String custom02;
    private String custom03;
    private String age;
    private String spender;
    private String sentimentScore;
    private ComparisonOp op;
    private String aggregateColumn;
    private int aggResultSize = AGGREGATION_RESULT_SIZE_MAX_LIMIT;

    ESSearchRequestBuilder(final ApplicationId aAppId) throws DomainErrorException {
        if (Objects.isNull(aAppId)) {
            throw ApplicationId.ERROR_INVALID_APP_ID;
        }
        appId = aAppId;
    }

    ESSearchRequestBuilder setRange(QueryRange range) {
        this.range = range;
        return this;
    }

    ESSearchRequestBuilder setOffset(Integer offset) {
        if (offset != null) {
            this.offset = offset;
        }
        return this;
    }

    ESSearchRequestBuilder setPageSize(Integer pageSize) {
        if (pageSize != null) {
            this.pageSize = pageSize;
        }
        return this;
    }

    ESSearchRequestBuilder setText(String text) {
        this.text = text;
        return this;
    }

    ESSearchRequestBuilder setPhrase(String phrase) {
        this.phrase = phrase;
        return this;
    }

    ESSearchRequestBuilder setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    ESSearchRequestBuilder setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    ESSearchRequestBuilder setSenderId(String senderId) {
        this.senderId = senderId;
        return this;
    }

    ESSearchRequestBuilder setSenderNick(String senderNick) {
        this.senderNick = senderNick;
        return this;
    }

    ESSearchRequestBuilder setPlatform(Platform platform) {
        this.platform = platform;
        return this;
    }

    ESSearchRequestBuilder setBuild(String build) {
        this.build = build;
        return this;
    }

    ESSearchRequestBuilder setCustom01(String custom01) {
        this.custom01 = custom01;
        return this;
    }

    ESSearchRequestBuilder setCustom02(String custom02) {
        this.custom02 = custom02;
        return this;
    }

    ESSearchRequestBuilder setCustom03(String custom03) {
        this.custom03 = custom03;
        return this;
    }

    ESSearchRequestBuilder setAge(String age) {
        this.age = age;
        return this;
    }

    ESSearchRequestBuilder setSpender(String spender) {
        this.spender = spender;
        return this;
    }

    ESSearchRequestBuilder setSentimentScore(String sentimentScore) {
        this.sentimentScore = sentimentScore;
        return this;
    }

    ESSearchRequestBuilder setOp(ComparisonOp op) {
        this.op = op;
        return this;
    }

    ESSearchRequestBuilder setSort(SortOrder sort) {
        this.sort = sort;
        return this;
    }

    public ESSearchRequestBuilder setAggregateColumn(String aggregateColumn) {
        this.aggregateColumn = aggregateColumn;
        return this;
    }

    public ESSearchRequestBuilder setAggResultSize(Integer aggResultSize) {
        if (Objects.nonNull(aggResultSize)) {
            this.aggResultSize = aggResultSize;
        }
        return this;
    }

    public SearchRequest build() throws DomainErrorException {
        if (Objects.isNull(range)) {
            throw ERROR_INVALID_QUERY_RANGE;
        }
        if (!Objects.isNull(text) && text.length() > 2048) {
            throw ERROR_INVALID_SEARCH_TEXT;
        }
        if (!Objects.isNull(phrase) && phrase.length() > 2048) {
            throw ERROR_INVALID_SEARCH_PHRASE;
        }

        if (pageSize < PAGE_SIZE_MIN || pageSize > PAGE_SIZE_MAX) {
            throw ERROR_INVALID_PAGE_SIZE;
        }

        if (offset > RESULT_SIZE_LIMIT - pageSize) {
            throw ERROR_INVALID_CURSOR_OFFSET;
        }

        final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        addQueryClause(queryBuilder);
        addFilterClause(queryBuilder);

        final SearchRequest request = new SearchRequest(MESSAGES_INDEX);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(queryBuilder);
        builder.from(offset);
        builder.size(pageSize);
        if (!Objects.isNull(sort)) {
            builder.sort("timestamp", sort);
        }
        bindAggregateQuery(builder);
        request.source(builder);

        return request;
    }

    private void addQueryClause(final BoolQueryBuilder aBuilder) {
        if (!Objects.isNull(phrase)) {
            aBuilder.must(QueryBuilders.matchPhraseQuery("content", phrase));
        }
        if (!Objects.isNull(text)) {
            aBuilder.must(
                    QueryBuilders
                        .multiMatchQuery(text, "content", "body", "body.locale_*")
                        .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
            );
        }
        if (aBuilder.must().size() <= 0) {
            aBuilder.must(QueryBuilders.matchAllQuery());
        }
    }

    private void addFilterClause(final BoolQueryBuilder aBuilder) throws DomainErrorException {
        aBuilder
        .filter(
            QueryBuilders
            .rangeQuery("timestamp")
            .from(range.getStart()*1000, true)
            .to(range.getEnd()*1000, true)
        );

        aBuilder.filter(QueryBuilders.termQuery("appId", appId.value()));

        if (!Objects.isNull(countryCode)) {
            aBuilder.filter(QueryBuilders.matchQuery("countryCode", countryCode));
        }
        if (!Objects.isNull(channelId)) {
            aBuilder.filter(QueryBuilders.matchQuery("channel", channelId));
        }
        if (!Objects.isNull(senderId)) {
            aBuilder.filter(QueryBuilders.matchQuery("actorId", senderId));
        }
        if (!Objects.isNull(senderNick)) {
            aBuilder.filter(QueryBuilders.matchQuery("nick", senderNick));
        }
        if (!Objects.isNull(platform)) {
            aBuilder.filter(QueryBuilders.matchQuery("platform", platform.value()));
        }
        if (!Objects.isNull(build)) {
            aBuilder.filter(QueryBuilders.matchQuery("build", build));
        }
        if (!Objects.isNull(custom01)) {
            aBuilder.filter(QueryBuilders.matchQuery("custom01", custom01));
        }
        if (!Objects.isNull(custom02)) {
            aBuilder.filter(QueryBuilders.matchQuery("custom02", custom02));
        }
        if (!Objects.isNull(custom03)) {
            aBuilder.filter(QueryBuilders.matchQuery("custom03", custom03));
        }
        if (!Objects.isNull(age)) {
            aBuilder.filter(QueryBuilders.matchQuery("age", age));
        }
        if (!Objects.isNull(spender)) {
            aBuilder.filter(QueryBuilders.matchQuery("spender", spender));
        }
        if (!Objects.isNull(sentimentScore) && !Objects.isNull(op)) {
            addSentimentFilter(aBuilder);
        }
    }

    private void addSentimentFilter(BoolQueryBuilder aBuilder) throws DomainErrorException {
        if (op == ComparisonOp.BTW) {
            try {
                JsonObject sentimentScore = new JsonObject(this.sentimentScore);

                if (!sentimentScore.containsKey("from") && !sentimentScore.containsKey("to")) {
                    throw ERROR_INVALID_SENTIMENT_SCORE_FORMAT;
                }

                Double sentimentScoreFrom = sentimentScore.getDouble("from", -1.0);
                Double sentimentScoreTo = sentimentScore.getDouble("to", 1.0);

                final RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("sentimentScore");

                rangeQuery.from(sentimentScoreFrom);
                rangeQuery.to(sentimentScoreTo);

                aBuilder.filter(rangeQuery);
            } catch (DecodeException ex) {
                throw ERROR_INVALID_SENTIMENT_SCORE_FORMAT;
            }
        }
        else {
            try {
                double sentimentScore = Double.parseDouble(this.sentimentScore);
                if (op == ComparisonOp.EQ) {
                    aBuilder.filter(QueryBuilders.termQuery("sentimentScore", sentimentScore));
                } else {
                    final RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("sentimentScore");
                    switch (op) {
                        case LT:
                            rangeQuery.lt(sentimentScore);
                            break;
                        case LTE:
                            rangeQuery.lte(sentimentScore);
                            break;
                        case GT:
                            rangeQuery.gt(sentimentScore);
                            break;
                        case GTE:
                            rangeQuery.gte(sentimentScore);
                            break;
                    }
                    aBuilder.filter(rangeQuery);
                }
            }
            catch(NumberFormatException ex) {
                throw ERROR_INVALID_SENTIMENT_SCORE_FORMAT;
            }
        }
    }

    private void bindAggregateQuery(SearchSourceBuilder aBuilder) {
        if (Objects.nonNull(aggregateColumn)) {
            aBuilder
                    .aggregation(AggregationBuilders
                            .terms("keywords")
                            .field(aggregateColumn)
                            .size(aggResultSize));
        }
    }
}
