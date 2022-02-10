package io.fizz.gdpr.job.anonymize.elasticsearch.textmessages;

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

import java.io.IOException;
import java.util.*;

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
            final String messagesIndex = configService.get("elasticsearch.message.index");

            final UpdateByQueryRequest request = new UpdateByQueryRequest(messagesIndex);

            for (Map.Entry<String, List<String>> entry : getAppUserIdsMap().entrySet()) {
                anonymize(request, entry.getKey(), entry.getValue());
            }

            return 0;
        } catch (Exception e) {
            System.out.println(e);
            return 1;
        }
    }

    private Map<String, List<String>> getAppUserIdsMap() {
        final Map<String, List<String>> result = new HashMap<>();
        final List<GDPRRequest> requests = GDPRRequestRepository.fetch();

        for (GDPRRequest request: requests) {
            final String appId = request.getAppId();
            final String userId = request.getUserId();

            List<String> userIds = result.computeIfAbsent(appId, s -> new ArrayList<>());
            userIds.add(userId);
        }

        return result;
    }

    private void anonymize(final UpdateByQueryRequest aRequest,
                           final String aAppId,
                           final List<String> aActorIds) throws IOException {
        final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        queryBuilder.must(QueryBuilders.termsQuery("actorId", aActorIds.toArray(new String[0])));
        queryBuilder.filter(QueryBuilders.termQuery("appId", aAppId));

        aRequest.setQuery(queryBuilder);

        aRequest.setScript(
                new Script(
                        ScriptType.INLINE, "painless",
                        "ctx._source.actorId = 'fizz_anonymized'",
                        Collections.emptyMap()));

        esClient.updateByQuery(aRequest, RequestOptions.DEFAULT);
    }
}
