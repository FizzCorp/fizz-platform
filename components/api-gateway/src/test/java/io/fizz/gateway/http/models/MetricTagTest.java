package io.fizz.gateway.http.models;

import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.gateway.http.controllers.exploration.MetricTag;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class MetricTagTest {
    static final String appId = "appA";

    @Test
    @DisplayName("it should build metric tag.")
    void validMetricTagTest() throws DomainErrorException {
        MetricTag tag = new MetricTag(new ApplicationId(appId), new JsonObject().put("segment",
                new JsonObject().put("geo", "pk")));
        String hashedSegment = Utils.tsdbSegment("pk");

        Assertions.assertNotNull(tag);
        Map<String, Object> tagsMap = tag.buildTags();
        Assertions.assertTrue(tagsMap.containsKey("appId"));
        Assertions.assertTrue(tagsMap.containsValue(appId));
        Assertions.assertTrue(tagsMap.containsKey("segment.geo"));
        Assertions.assertTrue(tagsMap.containsValue(hashedSegment));
    }

    @Test
    @DisplayName("it should build metric tag with no segment.")
    void validMetricTagNoSegmentTest() throws DomainErrorException {
        MetricTag tag = new MetricTag(new ApplicationId(appId), new JsonObject());
        String hashedSegment = Utils.tsdbSegment("any");

        Assertions.assertNotNull(tag);
        Map<String, Object> tagsMap = tag.buildTags();
        Assertions.assertTrue(tagsMap.containsKey("appId"));
        Assertions.assertTrue(tagsMap.containsValue(appId));
        Assertions.assertTrue(tagsMap.containsKey("segment.any"));
        Assertions.assertTrue(tagsMap.containsValue(hashedSegment));
    }

    @Test
    @DisplayName("it should not build metric tag for null application id.")
    void invalidMetricTagNullApplicationIdTest() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new MetricTag(null, new JsonObject() .put("segment",
                        new JsonObject()));
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_app_id");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should not build metric tag for missing segment value.")
    void invalidMetricTagMissingSegmentValueTest() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new MetricTag(new ApplicationId(appId), new JsonObject() .put("segment",
                        new JsonObject()));
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_segment");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should not build metric tag for multiple segment value.")
    void invalidMetricTagMultipleSegmentValueTest() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new MetricTag(new ApplicationId(appId), new JsonObject() .put("segment",
                        new JsonObject().put("geo", "pk").put("spend", "none")));
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_segment");
                throw ex;
            }
        });
    }

    @Test
    @DisplayName("it should build metric tag.")
    void invalidMetricTagSegmentTest() {
        Assertions.assertThrows(DomainErrorException.class, () -> {
            try {
                new MetricTag(new ApplicationId(appId), new JsonObject().put("segment",
                        new JsonObject().put("country", "pk")));
            } catch (DomainErrorException ex) {
                Assertions.assertEquals(ex.error().reason(), "invalid_segment");
                throw ex;
            }
        });
    }
}
