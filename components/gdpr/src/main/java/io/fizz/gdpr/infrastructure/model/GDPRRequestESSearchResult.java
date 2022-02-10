package io.fizz.gdpr.infrastructure.model;

import java.util.List;

public class GDPRRequestESSearchResult {
    final private List<GDPRRequestES> gdprRequests;
    final private long resultSize;

    public GDPRRequestESSearchResult(List<GDPRRequestES> gdprRequests, long resultSize) {
        this.gdprRequests = gdprRequests;
        this.resultSize = resultSize;
    }

    public List<GDPRRequestES> reportedMessages() {
        return gdprRequests;
    }

    public long resultSize() {
        return resultSize;
    }
}
