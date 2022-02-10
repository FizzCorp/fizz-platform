package io.fizz.analytics.common.opentsdb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

class TSDBModelsTest {
    @Nested
    class VersionTest {
        @Test
        @DisplayName("it should create a valid version")
        void basicValidityTest() {
            final String shortRevision = "11c5eef";
            final String version = "2.0.0";
            final String timestamp = "1362712695";

            final TSDBModels.Version ver = new TSDBModels.Version(shortRevision, version, timestamp);
            assert (ver.version.equals(version));
            assert (ver.shortRevision.equals(shortRevision));
            assert (ver.timestamp.equals(timestamp));
        }
    }

    @Nested
    class MetricQueryRequestTest {
        @Test
        @DisplayName("it should create a valid version")
        void basicValidityTest() {
            final String aggregator = "max";
            final String metric = "activeUsersCountMonthly";
            final Map<String,Object> tags = new HashMap<String,Object>(){
                {
                    put("segmentValue", "any");
                }
            };

            final TSDBModels.MetricQueryRequest req = new TSDBModels.MetricQueryRequest(aggregator, metric, tags);
            assert (req.aggregator.equals(aggregator));
            assert (req.metric.equals(metric));
            assert (req.tags == tags);
        }

        @Test
        @DisplayName("it should not create a metric query request for invalid aggregator")
        void invalidAggregatorTest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new TSDBModels.MetricQueryRequest(null, null, null);
            });
        }

        @Test
        @DisplayName("it should not create a metric query requuest for invalid metric")
        void invalidMetricTest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new TSDBModels.MetricQueryRequest("max", null, null);
            });
        }

        @Test
        @DisplayName("it should create metric query request for missing tags")
        void missingTagsTest() {
            new TSDBModels.MetricQueryRequest("max", "activeUsersCountMonthly", null);
        }

        @Test
        @DisplayName("it should create metric query request for empty tags")
        void emptyTagsTest() {
            new TSDBModels.MetricQueryRequest("max", "activeUsersCountMonthly", new HashMap<>());
        }
    }

    @Nested
    class MultiMetricQueryRequestTest {
        private final String startTS = "1362712695";
        private final String aggregator = "max";
        private final String metric = "activeUsersCountMonthly";
        private final Map<String,Object> tags = new HashMap<String,Object>(){
            {
                put("segmentValue", "any");
            }
        };
        private final TSDBModels.MetricQueryRequest query = new TSDBModels.MetricQueryRequest(aggregator, metric, tags);

        @Test
        @DisplayName("it should create a valid multi-metric query request")
        void basicValidityTest() {
            final List<TSDBModels.MetricQueryRequest> queries = new ArrayList<TSDBModels.MetricQueryRequest>() {
                {
                    add(query);
                }
            };

            final TSDBModels.MultiMetricQueryRequest req = new TSDBModels.MultiMetricQueryRequest(startTS, queries);

            assert (req.start.equals(startTS));
            assert (req.queries == queries);
        }

        @Test
        @DisplayName("it should not create a multi-metric query request for invalid start time")
        void invalidStartTSTest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new TSDBModels.MultiMetricQueryRequest(null, new ArrayList<TSDBModels.MetricQueryRequest>() {
                    {
                        add(query);
                    }
                });
            });
        }

        @Test
        @DisplayName("it should not create a multi-metric query request for invalid queries")
        void invalidQueriesTest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new TSDBModels.MultiMetricQueryRequest(startTS, null);
            });
        }
    }

    @Nested
    class MetricTest {
        private final String metricTitle = "activeUsersCountMonthly";
        private final Map<String,Object> tags = new HashMap<String,Object>(){
            {
                put("segmentValue", "any");
            }
        };
        private final List<String> aggtegateTags = new ArrayList<String>() {
            {
                add("segmentValue.any");
            }
        };
        private final Map<String,Object> dps = new HashMap<String,Object>(){
            {
                put("1524983608", 64);
                put("1524983609", 32);
            }
        };

        @Test
        @DisplayName("it should create a valid metric")
        void basicValidityTest() {
            final TSDBModels.Metric metric = new TSDBModels.Metric(metricTitle, tags, aggtegateTags, dps);
            assert (metric.metric.equals(metricTitle));
            assert (metric.tags == tags);
            assert (metric.aggregateTags == aggtegateTags);
            assert (metric.dps == dps);
        }

        @Test
        @DisplayName("it should not create a metric for invalid data points")
        void invalidDataPointsTest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new TSDBModels.Metric(metricTitle, tags, aggtegateTags, null);
            });
        }
    }

    @Test
    public void itShouldCreateDataPoint() {
        final String metric = "activeUsersCountDaily";
        final Date timestamp = new Date(1483228800000L);
        final Double value = 122.3667;
        final Map<String,Object> tags = new HashMap<String,Object>() {
            {
                put("appId", 1234);
                put("segmentValue.any", "any");
            }
        };

        TSDBModels.DataPointBuilder builder = new TSDBModels.DataPointBuilder();
        builder.metric(metric).timestamp(timestamp).value(value);

        addTagsToBuilder(builder, tags);

        TSDBModels.DataPoint dp = builder.build();

        assert (Objects.equals(dp.metric, metric));
        assert (Objects.equals(dp.timestamp, (int)(timestamp.getTime()/1000)));
        assert (Objects.equals(dp.value, value));

        for (Map.Entry<String,Object> entry: tags.entrySet()) {
            final Object lhs = entry.getValue();
            final Object rhs = dp.tags.get(entry.getKey());
            assert (Objects.equals(lhs,rhs));
        }
    }

    private void addTagsToBuilder(TSDBModels.DataPointBuilder builder, Map<String,Object> tags) {
        for (Map.Entry<String,Object> entry: tags.entrySet()) {
            builder.addTag(entry.getKey(), entry.getValue());
        }
    }
}
