package io.fizz.chataccess.domain.role;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface AbstractRoleRepository {
    CompletableFuture<Role[]> fetch(Set<RoleName> aNames);
    CompletableFuture<Role> fetch(RoleName aName);
}
