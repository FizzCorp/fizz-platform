package io.fizz.gdpr.model;

import java.io.Serializable;

public class GDPRRequest implements Serializable {
    private String id;
    private String appId;
    private String userId;
    private boolean clearMessageData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isClearMessageData() {
        return clearMessageData;
    }

    public void setClearMessageData(boolean clearMessageData) {
        this.clearMessageData = clearMessageData;
    }
}
