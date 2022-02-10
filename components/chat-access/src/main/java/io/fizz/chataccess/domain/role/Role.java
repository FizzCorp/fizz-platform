package io.fizz.chataccess.domain.role;

import io.fizz.common.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Role {
    public static final Role NULL = new Role(new RoleName("null"), Integer.MAX_VALUE);
    
    private final RoleName name;
    private final int rank;
    private final Set<String> permissions;

    public Role(final RoleName aName, int aRank, final String... aPermissions) {
        Utils.assertRequiredArgument(aName, "invalid_role_name");
        Utils.assertRequiredArgument(aPermissions, "invalid_role_permissions");
        if (aRank < 0) {
            throw new IllegalArgumentException("invalid_rank_role");
        }

        name = aName;
        rank = aRank;
        permissions = new HashSet<>(Arrays.asList(aPermissions));
    }

    public RoleName name() {
        return name;
    }

    int rank() {
        return rank;
    }

    public boolean has(final String aPermission) {
        return permissions.contains(aPermission);
    }

    public boolean isRankedHigher(final Role aRHS) {
        if (Objects.isNull(aRHS)) {
            return true;
        }

        if (rank == aRHS.rank()) {
            return name.value().compareTo(aRHS.name.value()) < 0;
        }
        return rank < aRHS.rank();
    }

    public static Role compose(final Role... aRoles) {
        if (Objects.isNull(aRoles)) {
            return null;
        }

        Role outRole = null;
        for (Role role: aRoles) {
            if (Objects.isNull(role)) {
                continue;
            }

            if (role.isRankedHigher(outRole)) {
                outRole = role;
            }
        }

        return outRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }
}
