package io.fizz.chataccess.domain;

import io.fizz.chataccess.domain.context.AbstractUserMembershipsRepository;
import io.fizz.chataccess.domain.role.AbstractRoleRepository;
import io.fizz.common.Utils;

public class DomainServicesRegistry {
    private final AbstractUserMembershipsRepository membershipsRepo;
    private final AbstractRoleRepository roleRepo;

    public DomainServicesRegistry(final AbstractUserMembershipsRepository aMembershipRepo,
                                  final AbstractRoleRepository aRoleRepo) {
        Utils.assertRequiredArgument(aMembershipRepo, "invalid user memberships repo specified");
        Utils.assertRequiredArgument(aRoleRepo, "invalid role repo specified");

        membershipsRepo = aMembershipRepo;
        roleRepo = aRoleRepo;
    }

    public AbstractUserMembershipsRepository userMembershipsRepo() {
        return membershipsRepo;
    }

    public AbstractRoleRepository roleRepo() {
        return roleRepo;
    }
}
