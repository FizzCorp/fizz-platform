package io.fizz.gdpr.job.anonymize.hbase.model;

import java.util.Objects;

public class GDPRUser {
    private final String appId;
    private final String userId;
    private final boolean clearUserData;

    public GDPRUser(String appId, String userId, boolean clearUserData) {
        this.appId = appId;
        this.userId = userId;
        this.clearUserData = clearUserData;
    }

    public boolean isClearUserData() {
        return clearUserData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GDPRUser gdprUser = (GDPRUser) o;
        return appId.equals(gdprUser.appId) &&
                userId.equals(gdprUser.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, userId);
    }
}
