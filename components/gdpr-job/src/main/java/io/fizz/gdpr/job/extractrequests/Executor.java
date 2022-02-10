package io.fizz.gdpr.job.extractrequests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fizz.gdpr.job.AbstractExecutor;
import io.fizz.gdpr.model.GDPRRequest;
import io.fizz.gdpr.repository.GDPRRequestRepository;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.List;

public class Executor extends AbstractExecutor {
    public static void main(String[] args) {
        try {
            System.exit(ToolRunner.run(new Configuration(), new Executor(), args));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int run(String[] args) {
        super.run(args);

        try {
            final String requestsIndex = configService.get("elasticsearch.request.index");
            final int requestsSize = configService.getInt("elasticsearch.request.size");

            final SearchRequest request = new SearchRequest(requestsIndex);
            SearchSourceBuilder builder = new SearchSourceBuilder();

            final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.filter(QueryBuilders.termQuery("status", "scheduled"));

            builder.query(queryBuilder);
            builder.size(requestsSize);

            request.source(builder);

            final SearchResponse searchResp = esClient.search(request, RequestOptions.DEFAULT);

            final List<GDPRRequest> result = mapSearchResponse(searchResp);
            GDPRRequestRepository.save(result);

            return 0;
        } catch (Exception e) {
            System.out.println(e);
            return 1;
        }
    }

    private List<GDPRRequest> mapSearchResponse(final SearchResponse aResponse) {
        final Gson gson = new GsonBuilder()
                .create();
        final List<GDPRRequest> items = new ArrayList<>();

        for (final SearchHit hit: aResponse.getHits()) {
            GDPRRequest requestES = gson.fromJson(hit.getSourceAsString(), GDPRRequest.class);
            items.add(requestES);
        }

        return items;
    }
}
