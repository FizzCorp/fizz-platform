package io.fizz.gdpr.job;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public abstract class AbstractExecutor extends Configured implements Tool {
    protected ConfigService configService;
    protected RestHighLevelClient esClient;

    @Override
    public void setConf(Configuration conf) {
        super.setConf(conf);
        configService = new ConfigService(conf);
    }

    @Override
    public int run(String[] args) {
        final String protocol = configService.get("elasticsearch.protocol");
        final String host = configService.get("elasticsearch.host");
        final int port = configService.getInt("elasticsearch.port");

        esClient = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, protocol)));

        return 0;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        esClient.close();
    }
}
