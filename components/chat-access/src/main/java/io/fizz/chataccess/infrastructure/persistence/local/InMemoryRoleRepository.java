package io.fizz.chataccess.infrastructure.persistence.local;

import io.fizz.chataccess.domain.role.*;
import io.fizz.common.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRoleRepository implements AbstractRoleRepository {
    private final ConcurrentHashMap<RoleName, Role> roles = new ConcurrentHashMap<>();

    public void save(final Role aRole) {
        Utils.assertRequiredArgument(aRole, "invalid role specified");

        roles.put(aRole.name(), aRole);
    }

    @Override
    public CompletableFuture<Role[]> fetch(Set<RoleName> aNames) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aNames, "invalid_role_names");

            if (aNames.size() <= 0) {
                return CompletableFuture.completedFuture(new Role[0]);
            }

            final List<Role> outGroups = new ArrayList<>();

            for (final RoleName name: aNames) {
                final Role role = roles.get(name);
                if (Objects.nonNull(role)) {
                    outGroups.add(role);
                }
            }

            return CompletableFuture.completedFuture(outGroups.toArray(new Role[0]));
        });
    }

    @Override
    public CompletableFuture<Role> fetch(RoleName aName) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aName, "invalid_role_name");

            return CompletableFuture.completedFuture(roles.get(aName));
        });
    }
}
