package io.fizz.gdpr.job.updatestatus;

import io.fizz.common.Utils;
import io.fizz.gdpr.job.AbstractExecutor;
import io.fizz.gdpr.model.GDPRRequest;
import io.fizz.gdpr.repository.GDPRRequestRepository;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.Collections;
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

            final UpdateByQueryRequest request = new UpdateByQueryRequest(requestsIndex);
            SearchSourceBuilder builder = new SearchSourceBuilder();

            final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.termsQuery("id", getProcessedIds()));

            builder.query(queryBuilder);

            request.setScript(
                    new Script(
                            ScriptType.INLINE, "painless",
                            "ctx._source.status = 'completed'; ctx._source.updated = " + (Utils.now() / 1000),
                            Collections.emptyMap()));

            esClient.updateByQuery(request, RequestOptions.DEFAULT);

            return 0;
        } catch (Exception e) {
            System.out.println(e);
            return 1;
        }
    }

    private List<String> getProcessedIds() {
        final List<String> ids = new ArrayList<>();

        final List<GDPRRequest> requests = GDPRRequestRepository.fetch();

        for (GDPRRequest request: requests) {
            ids.add(request.getId());
        }

        return ids;
    }
}
