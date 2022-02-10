package io.fizz.chataccess.domain.role;

import io.fizz.chataccess.domain.context.AuthContextId;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;

import java.util.Objects;

public class GroupName {
    private final AuthContextId contextId;
    private final RoleName name;

    GroupName(final ApplicationId aAppId, final RoleName aName) {
        this(new AuthContextId(aAppId), aName);
    }

    public GroupName(final AuthContextId aContextId, final RoleName aName) {
        Utils.assertRequiredArgument(aContextId, "invalid_context_id");
        Utils.assertRequiredArgument(aName, "invalid_role_name");

        contextId = aContextId;
        name = aName;
    }

    public ApplicationId appId() {
        return contextId.appId();
    }

    AuthContextId contextId() {
        return contextId;
    }

    public RoleName roleName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupName groupName = (GroupName) o;
        return contextId.equals(groupName.contextId) &&
                name.equals(groupName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextId, name);
    }
}
