package io.fizz.chat.group.domain.group;

import io.fizz.chataccess.domain.context.AuthContextId;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;

import java.util.Objects;

public class GroupId {
    private final ApplicationId appId;
    private final String value;

    public GroupId(final ApplicationId aAppId, final String aValue) {
        Utils.assertRequiredArgument(aAppId, "invalid_app_id");
        Utils.assertRequiredArgumentLength(aValue, 64, "invalid_group_id");
        Utils.assertRequiedArgumentMatches(aValue, "[A-Za-z0-9_-]+", "invalid_group_id");

        this.appId = aAppId;
        this.value = aValue.trim();
    }

    public ApplicationId appId() {
        return appId;
    }

    public String value() {
        return value;
    }

    public String qualifiedValue() {
        return appId.value() + value;
    }

    public AuthContextId authContextId() {
        return new AuthContextId(appId, "grp."+value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupId groupId = (GroupId) o;
        return appId.equals(groupId.appId) &&
                value.equals(groupId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, value);
    }
}
