package io.fizz.gateway.http.controllers.exploration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.ConfigService;
import io.fizz.common.domain.*;
import io.fizz.common.infastructure.model.TextMessageES;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.gateway.http.annotations.SyncRestController;
import io.fizz.gateway.http.controllers.AbstractRestController;
import io.fizz.gateway.services.opentsdb.AbstractTSDBService;
import io.fizz.gateway.services.opentsdb.TSDBAPIErrorException;
import io.fizz.gateway.services.opentsdb.TSDBModels;
import io.fizz.gateway.services.opentsdb.TSDBServiceRetrofit2;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;

public class QueriesController extends AbstractRestController {
    private static class SentimentScore {
        ESSearchRequestBuilder.ComparisonOp op = null;
        String score = null;
    }

    private static final DomainErrorException ERROR_INVALID_METRICS = new DomainErrorException(new DomainError("invalid_metrics"));
    private static final DomainErrorException ERROR_INVALID_METRIC_ID = new DomainErrorException(new DomainError("invalid_metric_id"));
    private static final DomainErrorException ERROR_INVALID_CURSOR = new DomainErrorException(new DomainError("invalid_cursor"));
    private static final DomainErrorException ERROR_INVALID_SORT_ORDER = new DomainErrorException(new DomainError("invalid_sort_order"));
    private static final DomainErrorException ERROR_INVALID_SENTIMENT_SCORE = new DomainErrorException(new DomainError("invalid_sentiment_score"));

    private final String ES_PROTOCOL = ConfigService.instance().getString("es.protocol");
    private final String ES_HOST = ConfigService.instance().getString("es.host");
    private final int ES_PORT = ConfigService.instance().getNumber("es.port").intValue();
    private static long SEARCH_RESULT_SIZE_LIMIT = ConfigService.instance().getNumber("es.result.size.limit").longValue();
    private static int KEYWORDS_RESULT_SIZE_LIMIT = ConfigService.instance().getNumber("es.words.aggregation.result.size.limit").intValue();

    private final AbstractTSDBService tsdbService;
    private final Gson serde;

    public QueriesController(Vertx aVertx) throws Exception {
        super(aVertx);

        tsdbService = tsdbServiceFactory();
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(TSDBModels.Metric.class, new MetricsQueryResponseGsonAdapter());
        serde = builder.create();
    }

    @SyncRestController(path="/apps/:appId/queries/metrics", method= HttpMethod.POST, auth=AuthScheme.DIGEST)
    public void queryMetrics(final RoutingContext aContext, final HttpServerResponse aResponse) throws IOException {
        try {
            final ApplicationId appId = new ApplicationId(aContext.request().getParam("appId"));
            queryMetrics(aContext, aResponse, appId);
        } catch (DomainErrorException ex) {
            WebUtils.doErrorWithReason(aResponse, WebUtils.STATUS_BAD_REQUEST, ex.error().reason(), null);
        }
    }

    @SyncRestController(path="/apps/:appId/queries/messages", method=HttpMethod.POST, auth=AuthScheme.DIGEST)
    public void queryMessages(final RoutingContext aContext, final HttpServerResponse aResponse) throws IOException {
        try {
            final ApplicationId appId = new ApplicationId(aContext.request().getParam("appId"));
            queryMessages(aContext, aResponse, appId);
        } catch (DomainErrorException ex) {
            WebUtils.doErrorWithReason(aResponse, WebUtils.STATUS_BAD_REQUEST, ex.error().reason(), null);
        }
    }

    @SyncRestController(path="/apps/:appId/queries/keywords", method=HttpMethod.POST, auth=AuthScheme.DIGEST)
    public void queryKeywords(final RoutingContext aContext, final HttpServerResponse aResponse) throws IOException {
        try {
            final ApplicationId appId = new ApplicationId(aContext.request().getParam("appId"));
            queryKeywords(aContext, aResponse, appId);
        } catch (DomainErrorException ex) {
            WebUtils.doErrorWithReason(aResponse, WebUtils.STATUS_BAD_REQUEST, ex.error().reason(), null);
        }
    }

    private void queryMetrics(final RoutingContext aContext, final HttpServerResponse aResponse, final ApplicationId aAppId) throws IOException {
        try {
            final JsonObject body = aContext.getBodyAsJson();

            final List<TSDBModels.MetricQueryRequest> requests = buildRequests(body, aAppId);
            if (requests.size() <= 0) {
                WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "[]");
                return;
            }

            final TSDBModels.MultiMetricQueryRequest request = buildCompositeRequest(body, requests);
            final List<TSDBModels.Metric> metrics = tsdbService.query(request);

            WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, serde.toJson(metrics));
        }
        catch (DomainErrorException ex) {
            WebUtils.doErrorWithReason(aResponse, WebUtils.STATUS_BAD_REQUEST, ex.error().reason(), null);
        }
        catch (DecodeException ex) {
            WebUtils.doErrorWithReason(aResponse, WebUtils.STATUS_BAD_REQUEST, "invalid_request_body", null);
        }
        catch (TSDBAPIErrorException ex) {
            WebUtils.doErrorWithReason(aResponse, WebUtils.STATUS_BAD_REQUEST, "invalid_metrics_request", null);
        }
    }

    void queryMessages(final RoutingContext aContext, final HttpServerResponse aResponse, final ApplicationId aAppId) throws IOException {
        try {
            final JsonObject body = aContext.getBodyAsJson();
            final SentimentScore sentimentScore = parseSentimentScore(body);
            final RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(ES_HOST, ES_PORT, ES_PROTOCOL))
            );

            Integer cursor = body.getInteger("cursor", 0);
            final SearchRequest request = new ESSearchRequestBuilder(aAppId)
                    .setRange(new QueryRange(body.getLong("start"), body.getLong("end")))
                    .setOffset(cursor)
                    .setPageSize(body.getInteger("page_size"))
                    .setText(body.getString("text"))
                    .setPhrase(body.getString("phrase"))
                    .setCountryCode(body.getString("geo"))
                    .setChannelId(body.getString("channel_id"))
                    .setSenderId(body.getString("user_id"))
                    .setSenderNick(body.getString("nick"))
                    .setPlatform(body.containsKey("platform") ? new Platform(body.getString("platform")) : null)
                    .setBuild(body.getString("build"))
                    .setCustom01(body.getString("custom_01"))
                    .setCustom02(body.getString("custom_02"))
                    .setCustom03(body.getString("custom_03"))
                    .setAge(body.getString("age"))
                    .setSpender(body.getString("spend"))
                    .setSentimentScore(sentimentScore.score)
                    .setOp(sentimentScore.op)
                    .setSort(parseSort(body))
                    .build();

            final SearchResponse searchResp = client.search(request);
            client.close();

            long totalHits = searchResp.getHits().totalHits;
            if (cursor != null && cursor > totalHits) {
                throw ERROR_INVALID_CURSOR;
            }

            final JsonArray items = mapSearchResponse(searchResp);
            final JsonObject responseBody = new JsonObject();
            if (totalHits > SEARCH_RESULT_SIZE_LIMIT) {
                totalHits = SEARCH_RESULT_SIZE_LIMIT;
            }
            responseBody.put("resultSize", totalHits);
            responseBody.put("items", items);
            WebUtils.doOK(aResponse, responseBody);
        }
        catch (DomainErrorException ex) {
            WebUtils.doErrorWithReason(aResponse, WebUtils.STATUS_BAD_REQUEST, ex.error().reason(), null);
        }
        catch (DecodeException ex) {
            WebUtils.doErrorWithReason(aResponse, WebUtils.STATUS_BAD_REQUEST, "invalid_request_body", null);
        }
    }

    void queryKeywords(final RoutingContext aContext, final HttpServerResponse aResponse, final ApplicationId aAppId) throws IOException {
        try {
            final JsonObject body = aContext.getBodyAsJson();
            final SentimentScore sentimentScore = parseSentimentScore(body);
            final RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(ES_HOST, ES_PORT, ES_PROTOCOL))
            );
            final SearchRequest request = new ESSearchRequestBuilder(aAppId)
                    .setRange(new QueryRange(body.getLong("start"), body.getLong("end")))
                    .setPageSize(0)
                    .setText(body.getString("text"))
                    .setPhrase(body.getString("phrase"))
                    .setCountryCode(body.getString("geo"))
                    .setChannelId(body.getString("channel_id"))
                    .setSenderId(body.getString("user_id"))
                    .setSenderNick(body.getString("nick"))
                    .setPlatform(body.containsKey("platform") ? new Platform(body.getString("platform")) : null)
                    .setBuild(body.getString("build"))
                    .setCustom01(body.getString("custom_01"))
                    .setCustom02(body.getString("custom_02"))
                    .setCustom03(body.getString("custom_03"))
                    .setAge(body.getString("age"))
                    .setSpender(body.getString("spend"))
                    .setSentimentScore(sentimentScore.score)
                    .setOp(sentimentScore.op)
                    .setAggResultSize(KEYWORDS_RESULT_SIZE_LIMIT)
                    .setAggregateColumn("keywords")
                    .build();

            final SearchResponse searchResp = client.search(request);
            client.close();

            final JsonArray keywords = mapKeywordsResponse(searchResp);
            final JsonObject responseBody = new JsonObject();
            responseBody.put("keywords", keywords);
            WebUtils.doOK(aResponse, responseBody);
        }
        catch (DomainErrorException ex) {
            WebUtils.doErrorWithReason(aResponse, WebUtils.STATUS_BAD_REQUEST, ex.error().reason(), null);
        }
        catch (DecodeException ex) {
            WebUtils.doErrorWithReason(aResponse, WebUtils.STATUS_BAD_REQUEST, "invalid_request_body", null);
        }
    }

    private String encodeInt(int value) {
        return Base64.getEncoder().encodeToString(BigInteger.valueOf(value).toByteArray());
    }

    private JsonArray mapSearchResponse(final SearchResponse aResponse) {
        final Gson gson = new GsonBuilder()
                            .registerTypeAdapter(TextMessageES.class, new ElasticSearchResultSerializer())
                            .create();
        final JsonArray items = new JsonArray();
        for (final SearchHit hit: aResponse.getHits()) {
            TextMessageES textMessageES = gson.fromJson(hit.getSourceAsString(), TextMessageES.class);
            JsonObject item = new JsonObject(gson.toJson(textMessageES));
            items.add(item);
        }
        return items;
    }

    private JsonArray mapKeywordsResponse(final SearchResponse aResponse) {
        final JsonArray keywords = new JsonArray();
        Terms contractSums = aResponse.getAggregations().get("keywords");
        for (Terms.Bucket bucket : contractSums.getBuckets()) {
            JsonObject word = new JsonObject() {{
                put("keyword", bucket.getKeyAsString());
                put("count", bucket.getDocCount());
            }};
            keywords.add(word);
        }
        return keywords;
    }

    private SortOrder parseSort(final JsonObject aBody) throws DomainErrorException {
        final String sort = aBody.getString("sort");
        if (Objects.isNull(sort)) {
            return null;
        }

        try {
            return SortOrder.fromString(sort.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            throw ERROR_INVALID_SORT_ORDER;
        }
    }

    private SentimentScore parseSentimentScore(final JsonObject aBody) throws DomainErrorException {
        final SentimentScore score = new SentimentScore();
        try {
            final JsonObject data = aBody.getJsonObject("sentiment_score");
            if (Objects.isNull(data)) {
                return score;
            }

            if (!data.containsKey("score") || !data.containsKey("op")) {
                throw ERROR_INVALID_SENTIMENT_SCORE;
            }

            score.score = String.valueOf(data.getValue("score"));
            score.op = ESSearchRequestBuilder.ComparisonOp.valueOf(data.getString("op").toUpperCase());

            return score;
        }
        catch (DecodeException|ClassCastException|IllegalArgumentException ex) {
            throw ERROR_INVALID_SENTIMENT_SCORE;
        }
    }

    private TSDBModels.MultiMetricQueryRequest buildCompositeRequest(final JsonObject aBody,
                                                                     final List<TSDBModels.MetricQueryRequest> aRequests) throws DomainErrorException {
        final QueryRange range = new QueryRange(
            aBody.getLong("start"),
            aBody.getLong("end")
        );

        return new TSDBModels.MultiMetricQueryRequest(
            Long.toString(range.getStart()),
            Long.toString(range.getEnd()),
            aRequests
        );
    }

    private List<TSDBModels.MetricQueryRequest> buildRequests(final JsonObject aBody,
                                                              final ApplicationId aAppId) throws DomainErrorException {
        final Map<String,Object> tags = new MetricTag(aAppId, aBody).buildTags();
        final JsonArray metrics = aBody.getJsonArray("metrics");

        if (Objects.isNull(metrics)) {
            throw ERROR_INVALID_METRICS;
        }

        final List<TSDBModels.MetricQueryRequest> requests = new ArrayList<>();
        for (int mi = 0; mi < metrics.size(); mi++) {
            try {
                final JsonObject json = metrics.getJsonObject(mi);
                final String metric = json.getString("metric");
                final MetricId id = MetricId.valueOf(metric.toUpperCase());

                requests.add(new TSDBModels.MetricQueryRequest("max", id.value(), tags));
            }
            catch (IllegalArgumentException ex) {
                throw ERROR_INVALID_METRIC_ID;
            }
        }

        return requests;
    }

    private AbstractTSDBService tsdbServiceFactory() throws Exception {
        final String host = ConfigService.instance().getString("tsdb.host");
        final int port = ConfigService.instance().getNumber("tsdb.port").intValue();
        final URL url = new URL("http", host, port, "");

        return new TSDBServiceRetrofit2(url);
    }
}
