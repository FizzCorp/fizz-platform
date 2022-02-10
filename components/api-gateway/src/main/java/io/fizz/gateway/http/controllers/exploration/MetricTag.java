package io.fizz.gateway.http.controllers.exploration;

import io.fizz.common.MetricSegmentType;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainError;
import io.fizz.common.domain.DomainErrorException;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MetricTag {
    private static final DomainErrorException ERROR_INVALID_SEGMENT = new DomainErrorException(new DomainError("invalid_segment"));
    private String segmentKey = "segment.any";
    private String segmentValue = Utils.tsdbSegment("any");
    private static final String metricSegmentPrefix = "segment.";

    private final ApplicationId appId;

    public MetricTag(final ApplicationId aAppId, JsonObject aBody) throws DomainErrorException {
        if (Objects.isNull(aAppId)) {
            throw ApplicationId.ERROR_INVALID_APP_ID;
        }
        appId = aAppId;

        if (!aBody.containsKey("segment")) {
            return;
        }

        JsonObject segment = aBody.getJsonObject("segment");
        if (Objects.isNull(segment)) {
            throw ERROR_INVALID_SEGMENT;
        }
        Set<String> segmentKeys = segment.getMap().keySet();
        if (segmentKeys.size() != 1) {
            throw ERROR_INVALID_SEGMENT;
        }
        segmentKey = (String) segmentKeys.toArray()[0];

        if (Objects.isNull(MetricSegmentType.fromValue(metricSegmentPrefix + segmentKey))){
            throw ERROR_INVALID_SEGMENT;
        }

        segmentValue = Utils.tsdbSegment(segment.getString(segmentKey));
        segmentKey = metricSegmentPrefix + segmentKey;
    }

    public Map<String,Object> buildTags() {
        Map<String, Object> tags = new HashMap<>();
        tags.put("appId", appId.value());
        tags.put(segmentKey, segmentValue);
        return tags;
    }
}
