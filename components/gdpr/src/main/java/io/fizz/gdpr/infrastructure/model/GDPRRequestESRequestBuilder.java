package io.fizz.gdpr.infrastructure.model;

import io.fizz.common.Utils;
import io.fizz.common.domain.*;
import io.fizz.gdpr.domain.GDPRRequestStatus;
import io.fizz.gdpr.infrastructure.ConfigService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.Objects;

public class GDPRRequestESRequestBuilder {
    private static DomainErrorException ERROR_INVALID_QUERY_RANGE = new DomainErrorException(new DomainError("invalid_query_range"));
    private static DomainErrorException ERROR_INVALID_PAGE_SIZE = new DomainErrorException(new DomainError("invalid_page_size"));
    private static DomainErrorException ERROR_INVALID_CURSOR_OFFSET = new DomainErrorException(new DomainError("invalid_cursor_offset"));

    private static String GDPR_REQUESTS_INDEX = ConfigService.config().getString("gdpr.es.index");
    private static int PAGE_SIZE_MAX = ConfigService.config().getNumber("gdpr.es.page.size.max").intValue();
    private static int PAGE_SIZE_MIN = ConfigService.config().getNumber("gdpr.es.page.size.min").intValue();
    private static long RESULT_SIZE_LIMIT = ConfigService.config().getNumber("gdpr.es.result.size.limit").longValue();

    private ApplicationId appId;
    private String requestId;
    private QueryRange range;
    private int offset = 0;
    private int pageSize = PAGE_SIZE_MAX;
    private UserId userId;
    private UserId requestedBy;
    private UserId cancelledBy;
    private GDPRRequestStatus status;

    public GDPRRequestESRequestBuilder(ApplicationId appId) {
        Utils.assertRequiredArgument(appId, "invalid_app_id");
        this.appId = appId;
    }

    public GDPRRequestESRequestBuilder setRange(QueryRange range) {
        this.range = range;
        return this;
    }

    public GDPRRequestESRequestBuilder setOffset(Integer offset) {
        if (Objects.nonNull(offset)) {
            this.offset = offset;
        }
        return this;
    }

    public GDPRRequestESRequestBuilder setPageSize(Integer pageSize) {
        if (Objects.nonNull(pageSize)) {
            this.pageSize = pageSize;
        }
        return this;
    }

    public GDPRRequestESRequestBuilder setUserId(UserId userId) {
        this.userId = userId;
        return this;
    }

    public GDPRRequestESRequestBuilder setRequestedBy(UserId requestedBy) {
        this.requestedBy = requestedBy;
        return this;
    }

    public GDPRRequestESRequestBuilder setCancelledBy(UserId cancelledBy) {
        this.cancelledBy = cancelledBy;
        return this;
    }

    public GDPRRequestESRequestBuilder setStatus(GDPRRequestStatus status) {
        this.status = status;
        return this;
    }

    public GDPRRequestESRequestBuilder setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public SearchRequest build() throws DomainErrorException {
        if (Objects.isNull(range)) {
            throw ERROR_INVALID_QUERY_RANGE;
        }

        if (pageSize < PAGE_SIZE_MIN || pageSize > PAGE_SIZE_MAX) {
            throw ERROR_INVALID_PAGE_SIZE;
        }

        if (offset > RESULT_SIZE_LIMIT - pageSize) {
            throw ERROR_INVALID_CURSOR_OFFSET;
        }

        final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        addFilterClause(queryBuilder);

        final SearchRequest request = new SearchRequest(GDPR_REQUESTS_INDEX);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(queryBuilder);
        builder.from(offset);
        builder.size(pageSize);
        request.source(builder);

        return request;
    }

    private void addFilterClause(final BoolQueryBuilder aBuilder) {
        aBuilder
                .filter(
                        QueryBuilders
                                .rangeQuery("created")
                                .from(range.getStart()*1000, true)
                                .to(range.getEnd()*1000, true)
                );

        aBuilder.filter(QueryBuilders.termQuery("appId", appId.value()));

        if (!Objects.isNull(userId)) {
            aBuilder.filter(QueryBuilders.matchQuery("userId", userId.value()));
        }
        if (!Objects.isNull(requestedBy)) {
            aBuilder.filter(QueryBuilders.matchQuery("requestedBy", requestedBy.value()));
        }
        if (!Objects.isNull(cancelledBy)) {
            aBuilder.filter(QueryBuilders.matchQuery("cancelledBy", cancelledBy.value()));
        }
        if (!Objects.isNull(status)) {
            aBuilder.filter(QueryBuilders.matchQuery("status", status.value()));
        }
        if (!Objects.isNull(requestId) && !requestId.isEmpty()) {
            aBuilder.filter(QueryBuilders.matchQuery("id", requestId));
        }
    }
}