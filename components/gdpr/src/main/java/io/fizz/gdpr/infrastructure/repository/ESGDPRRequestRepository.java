package io.fizz.gdpr.infrastructure.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.QueryRange;
import io.fizz.common.domain.UserId;
import io.fizz.gdpr.application.repository.AbstractGDPRRequestRepository;
import io.fizz.gdpr.domain.GDPRRequest;
import io.fizz.gdpr.domain.GDPRRequestSearchResult;
import io.fizz.gdpr.domain.GDPRRequestStatus;
import io.fizz.gdpr.infrastructure.ConfigService;
import io.fizz.gdpr.infrastructure.model.GDPRRequestES;
import io.fizz.gdpr.infrastructure.model.GDPRRequestESRequestBuilder;
import io.fizz.gdpr.infrastructure.model.GDPRRequestESSearchResult;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ESGDPRRequestRepository implements AbstractGDPRRequestRepository {
    private static final LoggingService.Log logger = LoggingService.getLogger(ESGDPRRequestRepository.class);

    private final String esHost;
    private final int esPort;
    private final String esProtocol;
    private final String esIndex;
    private final String esResource;

    public ESGDPRRequestRepository() {
        esProtocol = ConfigService.config().getString("gdpr.es.protocol");
        esHost = ConfigService.config().getString("gdpr.es.host");
        esPort = ConfigService.config().getNumber("gdpr.es.port").intValue();

        esIndex = ConfigService.config().getString("gdpr.es.index");
        esResource = ConfigService.config().getString("gdpr.es.resource");
    }

    @Override
    public CompletableFuture<String> save(GDPRRequest aRequest) {
        CompletableFuture<String> future = new CompletableFuture<>();

        GDPRRequestES gdprRequestES = adaptTo(aRequest);
        try {
            String id = save(gdprRequestES);
            future.complete(id);
        } catch (IOException e) {
            logger.error(e);
            future.completeExceptionally(e);
            return future;
        }

        return future;
    }

    @Override
    public CompletableFuture<GDPRRequestSearchResult> searchGDPRRequests(ApplicationId aAppId, String aRequestId, UserId aUserId, UserId aRequestedBy, UserId aCancelledBy, GDPRRequestStatus aStatus, QueryRange aRange, Integer aCursor, Integer aPageSize) {
        CompletableFuture<GDPRRequestSearchResult> future = new CompletableFuture<>();
        try {
            GDPRRequestESSearchResult results = getGDPRRequests(aAppId, aRequestId, aUserId, aRequestedBy, aCancelledBy, aStatus, aRange, aCursor, aPageSize);
            future.complete(adaptTo(results));
            return future;

        } catch (DomainErrorException | IOException e) {
            logger.error(e);
            future.completeExceptionally(e);
            return future;
        }
    }

    String save(GDPRRequestES aGDPRRequestES) throws IOException {
        String id = aGDPRRequestES.getId();

        final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(esHost, esPort, esProtocol)));
        final BulkRequest request = new BulkRequest();
        request.add(new IndexRequest(esIndex, esResource, id).source(new JSONObject(aGDPRRequestES).toString(), XContentType.JSON));
        client.bulk(request);
        client.close();

        return id;
    }

    GDPRRequestESSearchResult getGDPRRequests(final ApplicationId aAppId,
                                                      final String aRequestId,
                                                      final UserId aUserId,
                                                      final UserId aRequestedBy,
                                                      final UserId aCancelledBy,
                                                      final GDPRRequestStatus aStatus,
                                                      final QueryRange aRange,
                                                      final Integer aCursor,
                                                      final Integer aPageSize) throws IOException, DomainErrorException {
        SearchRequest request = new GDPRRequestESRequestBuilder(aAppId)
                .setUserId(aUserId)
                .setRequestedBy(aRequestedBy)
                .setCancelledBy(aCancelledBy)
                .setStatus(aStatus)
                .setRange(aRange)
                .setOffset(aCursor)
                .setPageSize(aPageSize)
                .setRequestId(aRequestId)
                .build();

        final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(esHost, esPort, esProtocol)));
        final SearchResponse searchResp = client.search(request);
        client.close();

        List<GDPRRequestES> result = mapSearchResponse(searchResp);

        long totalHits = searchResp.getHits().totalHits;
        return new GDPRRequestESSearchResult(result, totalHits);
    }

    private List<GDPRRequestES> mapSearchResponse(final SearchResponse aResponse) {
        final Gson gson = new GsonBuilder()
                .create();
        final List<GDPRRequestES> items = new ArrayList<>();

        for (final SearchHit hit: aResponse.getHits()) {
            GDPRRequestES requestES = gson.fromJson(hit.getSourceAsString(), GDPRRequestES.class);
            items.add(requestES);
        }

        return items;
    }

    private GDPRRequestES adaptTo(final GDPRRequest aRequest) {
        return new GDPRRequestES(
                aRequest.id(),
                aRequest.appId().value(),
                aRequest.userId().value(),
                aRequest.clearMessageData(),
                aRequest.requestedBy().value(),
                aRequest.status().value(),
                aRequest.created(),
                Objects.nonNull(aRequest.cancelledBy()) ? aRequest.cancelledBy().value() : "",
                aRequest.updated()
        );
    }

    private GDPRRequest adaptTo(final GDPRRequestES aRequest) throws DomainErrorException {
        return new GDPRRequest(
                aRequest.getId(),
                new ApplicationId(aRequest.getAppId()),
                new UserId(aRequest.getUserId()),
                aRequest.isClearMessageData(),
                new UserId(aRequest.getRequestedBy()),
                GDPRRequestStatus.fromValue(aRequest.getStatus()),
                aRequest.getCreated(),
                Objects.nonNull(aRequest.getCancelledBy()) && !aRequest.getCancelledBy().isEmpty() ?
                        new UserId(aRequest.getCancelledBy()) : null,
                aRequest.getUpdated()
        );
    }

    private GDPRRequestSearchResult adaptTo(final GDPRRequestESSearchResult aRequestsES) throws DomainErrorException {
        List<GDPRRequest> requests = new ArrayList<>();
        for (GDPRRequestES requestES: aRequestsES.reportedMessages()) {
            requests.add(adaptTo(requestES));
        }
        return new GDPRRequestSearchResult(requests, aRequestsES.resultSize());
    }
}