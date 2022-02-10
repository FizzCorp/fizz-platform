package io.fizz.gateway.http.models;

import java.util.List;
import java.util.Map;

public class MetricsQueryRequest {
    public static class Metric {
        String metric;

        public Metric(String metric) {
            this.metric = metric;
        }
    }

    private final Long start;
    private final Long end;
    private final List<Metric> metrics;
    private final Map<String, String> segment;

    public MetricsQueryRequest(Long start, Long end, List<Metric> metrics) {
        this.start = start;
        this.end = end;
        this.metrics = metrics;
        segment = null;
    }

    public MetricsQueryRequest(Long start, Long end, List<Metric> metrics, Map<String, String> segment) {
        this.start = start;
        this.end = end;
        this.metrics = metrics;
        this.segment = segment;
    }
}
