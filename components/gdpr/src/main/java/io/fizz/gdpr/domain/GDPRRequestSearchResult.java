package io.fizz.gdpr.domain;

import io.fizz.common.Utils;

import java.util.List;

public class GDPRRequestSearchResult {
    final private List<GDPRRequest> gdprRequests;
    final private long resultSize;

    public GDPRRequestSearchResult(List<GDPRRequest> gdprRequests, long resultSize) {
        Utils.assertRequiredArgument(gdprRequests, "invalid_gdpr_requests_list");
        Utils.assertArgumentRange(resultSize, 0L, Long.MAX_VALUE, "invalid_result_size");

        if (resultSize < gdprRequests.size()) {
            throw new IllegalArgumentException("invalid_result_size");
        }

        this.gdprRequests = gdprRequests;
        this.resultSize = resultSize;
    }

    public List<GDPRRequest> gdprRequests() {
        return gdprRequests;
    }

    public long resultSize() {
        return resultSize;
    }
}

