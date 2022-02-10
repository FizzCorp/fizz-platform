package io.fizz.gateway.services.opentsdb;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AbstractTSDBService {
    TSDBModels.Version version() throws Exception;
    List<TSDBModels.Metric> query(TSDBModels.MultiMetricQueryRequest request) throws TSDBAPIErrorException, IOException;
    CompletableFuture<List<TSDBModels.Metric>> queryAsync(TSDBModels.MultiMetricQueryRequest request);
    void put(List<TSDBModels.DataPoint> request) throws Exception;
}
