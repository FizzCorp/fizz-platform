package io.fizz.chat.moderation.infrastructure.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fizz.chat.moderation.application.repository.AbstractChatModerationRepository;
import io.fizz.chat.moderation.domain.*;
import io.fizz.chat.moderation.infrastructure.ConfigService;
import io.fizz.chat.moderation.infrastructure.model.ReportedMessageESRequestBuilder;
import io.fizz.chat.moderation.infrastructure.model.ReportedMessageESSearchResult;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.QueryRange;
import io.fizz.common.domain.UserId;
import io.fizz.chat.moderation.infrastructure.model.ReportedMessageES;
import io.vertx.core.http.RequestOptions;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ESChatModerationRepository implements AbstractChatModerationRepository {
    private static final LoggingService.Log logger = LoggingService.getLogger(ESChatModerationRepository.class);

    private final String esHost;
    private final int esPort;
    private final String esProtocol;
    private final String esIndex;
    private final String esResource;

    public ESChatModerationRepository() {
        esProtocol = ConfigService.config().getString("chat.content.es.protocol");
        esHost = ConfigService.config().getString("chat.content.es.host");
        esPort = ConfigService.config().getNumber("chat.content.es.port").intValue();

        esIndex = ConfigService.config().getString("chat.content.es.index");
        esResource = ConfigService.config().getString("chat.content.es.resource");
    }

    @Override
    public CompletableFuture<String> save(final ReportedMessage aMessage) {
        CompletableFuture<String> future = new CompletableFuture<>();

        ReportedMessageES reportedMessageES = adaptTo(aMessage);
        try {
            String id = save(reportedMessageES);
            future.complete(id);
        } catch (IOException e) {
            logger.error(e);
            future.completeExceptionally(e);
            return future;
        }

        return future;
    }

    @Override
    public CompletableFuture<ReportedMessageSearchResult> searchMessages(final ApplicationId aAppId,
                                                                         final UserId aUserId,
                                                                         final ChannelId aChannelId,
                                                                         final LanguageCode aLang,
                                                                         final QueryRange aRange,
                                                                         final Integer aCursor,
                                                                         final Integer aPageSize,
                                                                         final String aSort) {
        CompletableFuture<ReportedMessageSearchResult> future = new CompletableFuture<>();
        try {

            ReportedMessageESSearchResult results = getReportedMessages(aAppId, aUserId, aChannelId, aLang, aRange, aCursor, aPageSize, aSort);
            future.complete(adaptTo(results));
            return future;

        } catch (DomainErrorException | IOException e) {
            logger.error(e);
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<List<ReportedUser>> searchUsers(final ApplicationId aAppId,
                                                             final ChannelId aChannelId,
                                                             final LanguageCode aLang,
                                                             final Integer aResultLimit,
                                                             final QueryRange aRange) {
        CompletableFuture<List<ReportedUser>> future = new CompletableFuture<>();
        try {

            List<ReportedUser> results = getReportedUserAggregation(aAppId, aChannelId, aLang, aResultLimit, aRange);
            future.complete(results);
            return future;

        } catch (DomainErrorException | IOException e) {
            logger.error(e);
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<List<ReportedChannel>> searchChannels(final ApplicationId aAppId,
                                                                   final LanguageCode aLang,
                                                                   final Integer aResultLimit,
                                                                   final QueryRange aRange) {
        CompletableFuture<List<ReportedChannel>> future = new CompletableFuture<>();
        try {

            List<ReportedChannel> results = getChannelAggregation(aAppId, aLang, aResultLimit, aRange);
            future.complete(results);
            return future;

        } catch (DomainErrorException | IOException e) {
            logger.error(e);
            future.completeExceptionally(e);
            return future;
        }
    }

    String save(ReportedMessageES aReportedMessageES) throws IOException {
        String id = aReportedMessageES.getId();

        final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(esHost, esPort, esProtocol)));
        final BulkRequest request = new BulkRequest();
        request.add(new IndexRequest(esIndex, esResource, id).source(new JSONObject(aReportedMessageES).toString(), XContentType.JSON));
        client.bulk(request);
        client.close();

        return id;
    }

    ReportedMessageESSearchResult getReportedMessages(final ApplicationId aAppId,
                                                      final UserId aUserId,
                                                      final ChannelId aChannelId,
                                                      final LanguageCode aLang,
                                                      final QueryRange aRange,
                                                      final Integer aCursor,
                                                      final Integer aPageSize,
                                                      final String aSort) throws IOException, DomainErrorException {
        SearchRequest request = new ReportedMessageESRequestBuilder(aAppId)
                .setReportedUserId(aUserId)
                .setChannelId(aChannelId)
                .setLang(aLang)
                .setRange(aRange)
                .setOffset(aCursor)
                .setSort(SortOrder.fromString(aSort))
                .setPageSize(aPageSize)
                .build();


        final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(esHost, esPort, esProtocol)));
        final SearchResponse searchResp = client.search(request);
        client.close();

        List<ReportedMessageES> result = mapSearchResponse(searchResp);

        long totalHits = searchResp.getHits().totalHits;
        return new ReportedMessageESSearchResult(result, totalHits);
    }

    List<ReportedUser> getReportedUserAggregation(final ApplicationId aAppId,
                                                  final ChannelId aChannelId,
                                                  final LanguageCode aLang,
                                                  final Integer aResultLimit,
                                                  final QueryRange aRange) throws IOException, DomainErrorException {
        SearchRequest request = new ReportedMessageESRequestBuilder(aAppId)
                .setChannelId(aChannelId)
                .setLang(aLang)
                .setRange(aRange)
                .setPageSize(0)
                .setAggResultSize(aResultLimit)
                .setAggregateColumn("reportedUserId")
                .build();


        final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(esHost, esPort, esProtocol)));
        final SearchResponse searchResp = client.search(request);
        client.close();

        final List<ReportedUser> results = new ArrayList<>();

        Terms contractSums = searchResp.getAggregations().get("reports");
        for (Terms.Bucket bucket : contractSums.getBuckets()) {
            ReportedUser reportedUser = new ReportedUser(new UserId(bucket.getKeyAsString()), bucket.getDocCount());
            results.add(reportedUser);
        }

        return results;
    }

    List<ReportedChannel> getChannelAggregation(final ApplicationId aAppId,
                                                  final LanguageCode aLang,
                                                  final Integer aResultLimit,
                                                  final QueryRange aRange) throws IOException, DomainErrorException {
        SearchRequest request = new ReportedMessageESRequestBuilder(aAppId)
                .setLang(aLang)
                .setRange(aRange)
                .setPageSize(0)
                .setAggResultSize(aResultLimit)
                .setAggregateColumn("channelId")
                .build();


        final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(esHost, esPort, esProtocol)));
        final SearchResponse searchResp = client.search(request);
        client.close();

        final List<ReportedChannel> results = new ArrayList<>();

        Terms contractSums = searchResp.getAggregations().get("reports");
        for (Terms.Bucket bucket : contractSums.getBuckets()) {
            ReportedChannel reportedUser = new ReportedChannel(new ChannelId(aAppId, bucket.getKeyAsString()), bucket.getDocCount());
            results.add(reportedUser);
        }

        return results;
    }

    private List<ReportedMessageES> mapSearchResponse(final SearchResponse aResponse) {
        final Gson gson = new GsonBuilder()
                .create();
        final List<ReportedMessageES> items = new ArrayList<>();

        for (final SearchHit hit: aResponse.getHits()) {
            ReportedMessageES messageES = gson.fromJson(hit.getSourceAsString(), ReportedMessageES.class);
            items.add(messageES);
        }

        return items;
    }

    private ReportedMessageES adaptTo(final ReportedMessage aMessage) {
        return new ReportedMessageES(
                aMessage.id(),
                aMessage.appId().value(),
                aMessage.reporterUserId().value(),
                aMessage.reportedUserId().value(),
                aMessage.message(),
                aMessage.messageId(),
                aMessage.channelId().value(),
                aMessage.language().value(),
                aMessage.offense().value(),
                aMessage.description(),
                aMessage.time()
        );
    }

    private ReportedMessage adaptTo(final ReportedMessageES aMessage) throws DomainErrorException {
        return new ReportedMessage (
                aMessage.getId(),
                new ApplicationId(aMessage.getAppId()),
                new UserId(aMessage.getReporterUserId()),
                new UserId(aMessage.getReportedUserId()),
                aMessage.getMessage(),
                aMessage.getMessageId(),
                new ChannelId(new ApplicationId(aMessage.getAppId()), aMessage.getChannelId()),
                LanguageCode.fromValue(aMessage.getLanguage()),
                new ReportOffense(aMessage.getOffense()),
                aMessage.getDescription(),
                aMessage.getTimestamp() / 1000
        );
    }

    private ReportedMessageSearchResult adaptTo(final ReportedMessageESSearchResult aMessagesES) throws DomainErrorException {
        List<ReportedMessage> messages = new ArrayList<>();
        for (ReportedMessageES messageES: aMessagesES.reportedMessages()) {
            messages.add(adaptTo(messageES));
        }
        return new ReportedMessageSearchResult(messages, aMessagesES.resultSize());
    }
}