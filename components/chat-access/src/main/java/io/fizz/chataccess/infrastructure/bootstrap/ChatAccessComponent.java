package io.fizz.chataccess.infrastructure.bootstrap;

import io.fizz.chataccess.application.AuthorizationService;
import io.fizz.chataccess.domain.context.AbstractUserMembershipsRepository;
import io.fizz.chataccess.domain.role.Role;
import io.fizz.chataccess.infrastructure.persistence.hbase.HBaseUserMembershipsRepository;
import io.fizz.chataccess.infrastructure.persistence.local.InMemoryRoleRepository;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.Config;
import io.fizz.common.Utils;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;

public class ChatAccessComponent {
    private static final Config config = new Config("chat-access");
    private static final String HBASE_NAMESPACE = config.getString("chat.access.hbase.namespace");

    private AuthorizationService authorizationService;
    private AbstractUserMembershipsRepository membershipsRepo;
    private InMemoryRoleRepository roleRepo;

    public ChatAccessComponent() {}

    public AuthorizationService authorizationService() {
        return authorizationService;
    }

    public InMemoryRoleRepository roleRepo() {
        return roleRepo;
    }

    public CompletableFuture<Void> open(final Vertx aVertx,
                                        final AbstractHBaseClient aClient,
                                        final Role aDefaultAppRole,
                                        final Role aDefaultContextRole) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aVertx, "invalid vertx instance specified");

            return buildRepositories(aClient)
                    .thenCompose(aEventBus -> buildAuthorizationService(aDefaultAppRole, aDefaultContextRole));
        });
    }

    private CompletableFuture<Void> buildRepositories(final AbstractHBaseClient aClient) {
        roleRepo = new InMemoryRoleRepository();
        membershipsRepo = new HBaseUserMembershipsRepository(aClient, HBASE_NAMESPACE);

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> buildAuthorizationService(final Role aDefaultAppRole,
                                                              final Role aDefaultContextRole) {
        authorizationService = new AuthorizationService(
                membershipsRepo,
                roleRepo,
                aDefaultAppRole,
                aDefaultContextRole
        );

        return CompletableFuture.completedFuture(null);
    }
}
