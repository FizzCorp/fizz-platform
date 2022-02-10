package io.fizz.chataccess.domain.context;

import io.fizz.chataccess.domain.DomainServicesRegistry;
import io.fizz.chataccess.domain.role.*;
import io.fizz.chatcommon.domain.MutationId;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UserMemberships {
    private final AuthContextId contextId;
    private final UserId userId;
    private Map<GroupName, GroupMembership> memberships = new HashMap<>();
    private final MutationId mutationId;

    public UserMemberships(final AuthContextId aContextId, final UserId aUserId) {
        this(aContextId, aUserId, new MutationId());
    }

    public UserMemberships(final AuthContextId aContextId,
                           final UserId aUserId,
                           final MutationId aMutationId) {
        Utils.assertRequiredArgument(aUserId, "invalid_user_id");
        Utils.assertRequiredArgument(aContextId, "invalid_context_id");
        Utils.assertRequiredArgument(aMutationId, "invalid_mutation_id");

        contextId = aContextId;
        userId = aUserId;
        mutationId = aMutationId;
    }

    public UserId userId() {
        return userId;
    }

    public Map<GroupName, GroupMembership> memberships() {
        return memberships;
    }

    public CompletableFuture<Role> appliedRole(final DomainServicesRegistry aRegistry) {
        return aRegistry.roleRepo().fetch(effectiveRoles())
                .thenApply(Role::compose);
    }

    Set<RoleName> effectiveRoles() {
        final Set<RoleName> roles = new HashSet<>();

        for (final Map.Entry<GroupName, GroupMembership> entry: memberships.entrySet()) {
            if (entry.getValue().isEffective()) {
                roles.add(entry.getKey().roleName());
            }
        }

        return roles;
    }

    public AuthContextId contextId() {
        return contextId;
    }

    public MutationId mutationId() {
        return mutationId;
    }

    public void add(final RoleName aRoleName) {
        final GroupName name = new GroupName(contextId, aRoleName);

        memberships.put(name, new GroupMembership(userId, name));
    }

    public void add(final RoleName aRoleName, final Date aEnds) {
        final GroupName name = new GroupName(contextId, aRoleName);

        memberships.put(name, new GroupMembership(userId, name, aEnds));
    }

    public void remove(final RoleName aRoleName) {
        final GroupName name = new GroupName(contextId, aRoleName);

        if (!memberships.containsKey(name)) {
            throw new IllegalStateException("role_not_assigned");
        }

        memberships.remove(name);
    }
}
