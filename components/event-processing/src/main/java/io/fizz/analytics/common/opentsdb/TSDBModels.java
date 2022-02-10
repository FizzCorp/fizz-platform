package io.fizz.analytics.common.opentsdb;

import java.util.*;

public class TSDBModels {
    public static class Version {
        final String shortRevision;
        final String version;
        final String timestamp;

        public Version(String aShortRevision, String aVersion, String aTimestamp) {
            this.shortRevision = aShortRevision;
            this.version = aVersion;
            this.timestamp = aTimestamp;
        }
    }

    public static class MetricQueryRequest {
        final String aggregator;
        final String metric;
        final Map<String,Object> tags;

        public MetricQueryRequest(String aggregator, String metric, Map<String, Object> tags) {
            if (Objects.isNull(aggregator)) {
                throw new IllegalArgumentException("aggregator must be specified in a metric query.");
            }
            if (Objects.isNull(metric)) {
                throw new IllegalArgumentException("metric must be specified in a metric query.");
            }

            this.aggregator = aggregator;
            this.metric = metric;
            this.tags = tags;
        }
    }

    public static class MultiMetricQueryRequest {
        final String start;
        final List<MetricQueryRequest> queries;

        public MultiMetricQueryRequest(String aStart, List<MetricQueryRequest> aQueries) {
            if (Objects.isNull(aStart)) {
                throw new IllegalArgumentException("start time must be specified for a metric query request");
            }
            if (Objects.isNull(aQueries) || aQueries.size() <= 0) {
                throw new IllegalArgumentException("at least one query must be specified for a metric query request");
            }

            this.start = aStart;
            this.queries = aQueries;
        }
    }

    public static class Metric {
        final String metric;
        final Map<String,Object> tags;
        final List<String> aggregateTags;
        final Map<String,Object> dps;

        public Metric(String aMetric, Map<String,Object> aTags, List<String> aAggregateTags, Map<String,Object> aDPs) {
            if (aDPs == null) {
                throw new IllegalArgumentException("data points must be specified");
            }

            this.metric = aMetric;
            this.tags = aTags;
            this.aggregateTags = aAggregateTags;
            this.dps = aDPs;
        }
    }

    public static class DataPoint {
        public final String metric;
        public final int timestamp;
        public final Object value;
        public final Map<String,Object> tags;

        DataPoint(String metric, Date timestamp, Object value, Map<String,Object> tags) {
            this.metric = metric;
            this.timestamp = (int)(timestamp.getTime()/1000);
            this.value = value;
            this.tags = tags;
        }
    }

    public static class DataPointBuilder {
        private String metric;
        private Date timestamp;
        private Object value;
        private Map<String,Object> tags;

        public DataPointBuilder metric(String metric) {
            this.metric = metric;
            return this;
        }

        public DataPointBuilder timestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public DataPointBuilder value(Object value) {
            this.value = value;
            return this;
        }

        public DataPointBuilder addTag(String key, Object value) {
            if (tags == null) {
                tags = new HashMap<>();
            }
            tags.put(key, value);
            return this;
        }

        public DataPoint build() {
            return new DataPoint(metric, timestamp, value, tags);
        }
    }
}
