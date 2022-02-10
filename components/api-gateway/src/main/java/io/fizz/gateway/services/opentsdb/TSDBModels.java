package io.fizz.gateway.services.opentsdb;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TSDBModels {
    public static class Version {
        final String short_revision;
        final String version;
        final String timestamp;

        public Version(String short_revision, String version, String timestamp) {
            this.short_revision = short_revision;
            this.version = version;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "Version {" +
                    "short_revision='" + short_revision + '\'' +
                    ", version='" + version + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    '}';
        }
    }

    public static class MetricQueryRequest {
        final String aggregator;
        final String metric;
        final Map<String,Object> tags;

        public MetricQueryRequest(String aggregator, String metric, Map<String, Object> tags) {
            this.aggregator = aggregator;
            this.metric = metric;
            this.tags = tags;
        }

        @Override
        public String toString() {
            return "MetricQueryRequest{" +
                    "aggregator='" + aggregator + '\'' +
                    ", metric='" + metric + '\'' +
                    ", tags=" + tags +
                    '}';
        }
    }

    public static class MultiMetricQueryRequest {
        final String start;
        final String end;
        final List<MetricQueryRequest> queries;

        public MultiMetricQueryRequest(String start, String end, List<MetricQueryRequest> queries) {
            this.start = start;
            this.end = end;
            this.queries = queries;
        }

        @Override
        public String toString() {
            return "MultiMetricQueryRequest{" +
                    "start='" + start + '\'' +
                    ", queries=" + queries +
                    '}';
        }
    }

    public static class Metric {
        final String metric;
        final Map<String,Object> tags;
        final List<String> aggregateTags;
        final Map<String,Object> dps;

        public Metric(String metric, Map<String,Object> tags, List<String> aggregateTags, Map<String,Object> dps) {
            if (dps == null) {
                throw new IllegalArgumentException("data points must be specified");
            }

            this.metric = metric;
            this.tags = tags;
            this.aggregateTags = aggregateTags;
            this.dps = dps;
        }

        public String metric() {
            return metric;
        }

        public Map<String,Object> dps() {
            return dps;
        }

        @Override
        public String toString() {
            return "Metric {" +
                    "metric='" + metric + '\'' +
                    "tags='" + tags + '\'' +
                    ", aggregateTags=" + aggregateTags +
                    ", dps=" + dps +
                    '}';
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

        @Override
        public String toString() {
            return "DataPoint{" +
                    "metric='" + metric + '\'' +
                    ", timestamp=" + timestamp +
                    ", value=" + value +
                    ", tags=" + tags +
                    '}';
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

        @Override
        public String toString() {
            return "DataPointBuilder{" +
                    "metric='" + metric + '\'' +
                    ", timestamp=" + timestamp +
                    ", value=" + value +
                    ", tags=" + tags +
                    '}';
        }
    }
}
