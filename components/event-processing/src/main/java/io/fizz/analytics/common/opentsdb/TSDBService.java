package io.fizz.analytics.common.opentsdb;

import java.util.List;

public interface TSDBService {
    TSDBModels.Version version() throws Exception;
    List<TSDBModels.Metric> query(TSDBModels.MultiMetricQueryRequest request) throws Exception;
    void put(List<TSDBModels.DataPoint> request) throws Exception;
}
