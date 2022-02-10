package io.fizz.gdpr.infrastructure.model;

import io.fizz.common.Utils;

public class GDPRRequestES {
    private String id;
    private String appId;
    private String userId;
    private boolean clearMessageData;
    private String requestedBy;
    private long created;
    private String status;
    private String cancelledBy;
    private long updated;

    public GDPRRequestES(String id,
                       String appId,
                       String userId,
                       boolean clearMessageData,
                       String requestedBy,
                       String status,
                       long created,
                       String cancelledBy,
                       long updated) {
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

    public String getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isClearMessageData() {
        return clearMessageData;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public long getCreated() {
        return created;
    }

    public String getStatus() {
        return status;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public long getUpdated() {
        return updated;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setClearMessageData(boolean clearMessageData) {
        this.clearMessageData = clearMessageData;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }
}

