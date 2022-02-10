package io.fizz.chataccess.domain.context;

import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;

import java.util.Objects;

public class AuthContextId {
    private final String value;
    private final ApplicationId appId;

    public AuthContextId(final ApplicationId aAppId) {
        Utils.assertRequiredArgument(aAppId, "invalid_app_id");

        value = null;
        appId = aAppId;
    }

    public AuthContextId(final ApplicationId aAppId, final String aValue) {
        final String trimmed = Objects.isNull(aValue) ? null : aValue.trim();

        Utils.assertRequiredArgumentLength(trimmed, 256, "invalid_context_id");
        Utils.assertRequiedArgumentMatches(trimmed, "[A-Za-z0-9-._]+", "invalid_context_id");
        Utils.assertRequiredArgument(aAppId, "invalid_app_id");

        value = trimmed;
        appId = aAppId;
    }

    public String value() {
        return value;
    }

    public ApplicationId appId() {
        return appId;
    }

    public String qualifiedValue() {
        if (Objects.nonNull(value)) {
            return appId.value() + ":" + value;
        }
        else {
            return appId.value();
        }
    }

    public boolean isChild() {
        return Objects.nonNull(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthContextId that = (AuthContextId) o;
        return Objects.equals(value, that.value) &&
                appId.equals(that.appId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, appId);
    }
}
