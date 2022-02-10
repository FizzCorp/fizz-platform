package io.fizz.gdpr.domain;

import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

public class GDPRRequest {
    private final String id;
    private final ApplicationId appId;
    private final UserId userId;
    private final boolean clearMessageData;
    private final UserId requestedBy;
    private final Long created;
    private GDPRRequestStatus status;
    private UserId cancelledBy;
    private Long updated;

    public GDPRRequest(String id,
                       ApplicationId appId,
                       UserId userId,
                       boolean clearMessageData,
                       UserId requestedBy,
                       GDPRRequestStatus status,
                       Long created,
                       UserId cancelledBy,
                       Long updated) {
        Utils.assertRequiredArgument(id, "invalid_id");
        Utils.assertRequiredArgument(appId, "invalid_app_id");
        Utils.assertRequiredArgument(userId, "invalid_user_id");
        Utils.assertRequiredArgument(requestedBy, "invalid_requester_user_id");
        Utils.assertRequiredArgument(created, "invalid_created_time");

        if (created < 0) {
            throw new IllegalArgumentException("invalid_time");
        }

        this.id = id;
        this.appId = appId;
        this.userId = userId;
        this.clearMessageData = clearMessageData;
        this.requestedBy = requestedBy;
        this.status = status;
        this.created = created;
        this.cancelledBy = cancelledBy;
        this.updated = updated;
    }

    public String id() {
        return id;
    }

    public ApplicationId appId() {
        return appId;
    }

    public UserId userId() {
        return userId;
    }

    public boolean clearMessageData() {
        return clearMessageData;
    }

    public UserId requestedBy() {
        return requestedBy;
    }

    public GDPRRequestStatus status() {
        return status;
    }

    public long created() {
        return created;
    }

    public UserId cancelledBy() {
        return cancelledBy;
    }

    public long updated() {
        return updated;
    }

    public void complete(final GDPRRequestStatus aStatus, final long aUpdated) {
        Utils.assertRequiredArgument(aUpdated, "invalid_created_time");

        this.status = aStatus;
        this.updated = aUpdated;
    }

    public void cancel(final UserId aCancelledBy, final GDPRRequestStatus aStatus, final long aUpdated) {
        Utils.assertRequiredArgument(aCancelledBy, "invalid_cancelled_by_user_id");
        Utils.assertRequiredArgument(aUpdated, "invalid_created_time");

        this.cancelledBy = aCancelledBy;
        this.status = aStatus;
        this.updated = aUpdated;
    }
}
