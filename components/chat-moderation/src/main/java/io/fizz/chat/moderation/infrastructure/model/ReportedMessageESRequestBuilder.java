package io.fizz.chat.moderation.infrastructure.model;

import io.fizz.chat.moderation.infrastructure.ConfigService;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.Utils;
import io.fizz.common.domain.*;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Objects;

public class ReportedMessageESRequestBuilder {
    private static DomainErrorException ERROR_INVALID_QUERY_RANGE = new DomainErrorException(new DomainError("invalid_query_range"));
    private static DomainErrorException ERROR_INVALID_PAGE_SIZE = new DomainErrorException(new DomainError("invalid_page_size"));
    private static DomainErrorException ERROR_INVALID_RESULT_LIMIT = new DomainErrorException(new DomainError("invalid_result_limit"));
    private static DomainErrorException ERROR_INVALID_CURSOR_OFFSET = new DomainErrorException(new DomainError("invalid_cursor_offset"));

    private static String MESSAGES_INDEX = ConfigService.config().getString("chat.content.es.index");
    private static int PAGE_SIZE_MAX = ConfigService.config().getNumber("chat.content.es.page.size.max").intValue();
    private static int PAGE_SIZE_MIN = ConfigService.config().getNumber("chat.content.es.page.size.min").intValue();
    private static long RESULT_SIZE_LIMIT = ConfigService.config().getNumber("chat.content.es.result.size.limit").longValue();
    private static int AGGREGATION_RESULT_SIZE_MAX_LIMIT = ConfigService.config().getNumber("chat.content.es.aggregation.result.size.max.limit").intValue();

    private ApplicationId appId;
    private QueryRange range;
    private int offset = 0;
    private int pageSize = PAGE_SIZE_MAX;
    private int aggResultSize = AGGREGATION_RESULT_SIZE_MAX_LIMIT;
    private SortOrder sort = SortOrder.DESC;
    private ChannelId channelId;
    private UserId reportedUserId;
    private UserId reporterUserId;
    private LanguageCode lang;
    private String aggregateColumn;

    public ReportedMessageESRequestBuilder(ApplicationId appId) {
        Utils.assertRequiredArgument(appId, "invalid_app_id");
        this.appId = appId;
    }

    public ReportedMessageESRequestBuilder setRange(QueryRange range) {
        this.range = range;
        return this;
    }

    public ReportedMessageESRequestBuilder setOffset(Integer offset) {
        if (Objects.nonNull(offset)) {
            this.offset = offset;
        }
        return this;
    }

    public ReportedMessageESRequestBuilder setPageSize(Integer pageSize) {
        if (Objects.nonNull(pageSize)) {
            this.pageSize = pageSize;
        }
        return this;
    }

    public ReportedMessageESRequestBuilder setAggResultSize(Integer aggResultSize) {
        if (Objects.nonNull(aggResultSize)) {
            this.aggResultSize = aggResultSize;
        }
        return this;
    }

    public ReportedMessageESRequestBuilder setSort(SortOrder sort) {
        this.sort = sort;
        return this;
    }

    public ReportedMessageESRequestBuilder setChannelId(ChannelId channelId) {
        this.channelId = channelId;
        return this;
    }

    public ReportedMessageESRequestBuilder setReportedUserId(UserId reportedUserId) {
        this.reportedUserId = reportedUserId;
        return this;
    }

    public ReportedMessageESRequestBuilder setReporterUserId(UserId reporterUserId) {
        this.reporterUserId = reporterUserId;
        return this;
    }

    public ReportedMessageESRequestBuilder setLang(LanguageCode lang) {
        this.lang = lang;
        return this;
    }

    public ReportedMessageESRequestBuilder setAggregateColumn(String aggregateColumn) {
        this.aggregateColumn = aggregateColumn;
        return this;
    }

    public SearchRequest build() throws DomainErrorException {
        if (Objects.isNull(range)) {
            throw ERROR_INVALID_QUERY_RANGE;
        }

        if (pageSize < PAGE_SIZE_MIN || pageSize > PAGE_SIZE_MAX) {
            throw ERROR_INVALID_PAGE_SIZE;
        }

        if (aggResultSize < 0 || aggResultSize > AGGREGATION_RESULT_SIZE_MAX_LIMIT) {
            throw ERROR_INVALID_RESULT_LIMIT;
        }

        if (offset > RESULT_SIZE_LIMIT - pageSize) {
            throw ERROR_INVALID_CURSOR_OFFSET;
        }

        final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
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

    private void addFilterClause(final BoolQueryBuilder aBuilder) {
        aBuilder
                .filter(
                        QueryBuilders
                                .rangeQuery("timestamp")
                                .from(range.getStart()*1000, true)
                                .to(range.getEnd()*1000, true)
                );

        aBuilder.filter(QueryBuilders.termQuery("appId", appId.value()));

        if (!Objects.isNull(reportedUserId)) {
            aBuilder.filter(QueryBuilders.matchQuery("reportedUserId", reportedUserId.value()));
        }
        if (!Objects.isNull(reporterUserId)) {
            aBuilder.filter(QueryBuilders.matchQuery("reporterUserId", reporterUserId.value()));
        }
        if (!Objects.isNull(channelId)) {
            aBuilder.filter(QueryBuilders.matchQuery("channelId", channelId.value()));
        }
        if (!Objects.isNull(lang)) {
            aBuilder.filter(QueryBuilders.matchQuery("language", lang.value()));
        }
    }

    private void bindAggregateQuery(SearchSourceBuilder aBuilder) {
        if (Objects.nonNull(aggregateColumn)) {
            aBuilder
            .aggregation(AggregationBuilders
                        .terms("reports")
                        .field(aggregateColumn)
                        .size(aggResultSize));
        }
    }
}
