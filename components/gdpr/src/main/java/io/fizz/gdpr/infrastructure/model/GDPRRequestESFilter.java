package io.fizz.gdpr.infrastructure.model;

import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.QueryRange;
import io.fizz.common.domain.UserId;
import io.fizz.gdpr.domain.GDPRRequestStatus;

import java.util.Objects;

public class GDPRRequestESFilter {
    private boolean filterPassed = true;
    private final GDPRRequestES requestES;

    public GDPRRequestESFilter(GDPRRequestES requestES) {
        this.requestES = requestES;
    }

    public GDPRRequestESFilter filterAppId(final ApplicationId aAppId) {
        filterPassed = filterPassed && (Objects.isNull(aAppId) || aAppId.value().equals(requestES.getAppId()));
        return this;
    }

    public GDPRRequestESFilter filterUserId(final UserId aUserId) {
        filterPassed = filterPassed && (Objects.isNull(aUserId) || aUserId.value().equals(requestES.getUserId()));
        return this;
    }

    public GDPRRequestESFilter filterRequestedBy(final UserId aRequestedBy) {
        filterPassed = filterPassed && (Objects.isNull(aRequestedBy) || aRequestedBy.value().equals(requestES.getRequestedBy()));
        return this;
    }

    public GDPRRequestESFilter filterCancelledBy(final UserId aCancelledBy) {
        filterPassed = filterPassed && (Objects.isNull(aCancelledBy) || aCancelledBy.value().equals(requestES.getCancelledBy()));
        return this;
    }

    public GDPRRequestESFilter filterStatus(final GDPRRequestStatus aStatus) {
        filterPassed = filterPassed && (Objects.isNull(aStatus) || aStatus.value().equals(requestES.getStatus()));
        return this;
    }

    public GDPRRequestESFilter filterRange(final QueryRange aRange) {
        filterPassed = filterPassed && (aRange.getStart() <= requestES.getCreated() / 1000 && aRange.getEnd() >= requestES.getCreated() / 1000);
        return this;
    }

    public GDPRRequestES get() {
        return filterPassed ? requestES : null;
    }
}
