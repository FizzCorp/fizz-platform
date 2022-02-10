package io.fizz.chataccess.domain;

import io.fizz.chataccess.domain.role.Role;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.ArrayList;
import java.util.List;

public class Authorization {
    private static class Validation {
        final String permission;
        final boolean isOwned;

        private Validation(final String aPermission, boolean aIsOwned) {
            permission = aPermission;
            isOwned = aIsOwned;
        }
    }

    private static final SecurityException ERROR_FORBIDDEN = new SecurityException("forbidden");

    private final UserId ownerId;
    private final Role ownerRole;
    private final UserId operatorId;
    private final Role operatorRole;
    private final List<Validation> validations = new ArrayList<>();

    public Authorization(final UserId aOwnerId, final Role aOwner, final UserId aOperatorId, final Role aOperator) {
        Utils.assertRequiredArgument(aOwnerId, "invalid_owner_id");
        Utils.assertRequiredArgument(aOwner, "invalid_owner_role");
        Utils.assertRequiredArgument(aOperatorId, "invalid_operator_id");
        Utils.assertRequiredArgument(aOperator, "invalid_operator_role");

        ownerId = aOwnerId;
        ownerRole = aOwner;
        operatorId = aOperatorId;
        operatorRole = aOperator;
    }

    public Authorization assertOperateOwned(final String aPermission) {
        validations.add(new Validation(aPermission, true));

        return this;
    }

    public Authorization assertOperateNotOwned(final String aPermission) {
        validations.add(new Validation(aPermission, false));

        return this;
    }

    public void validate() {
        for(final Validation validation: validations) {
            boolean isValid;

            if (validation.isOwned) {
                isValid = operatorId.equals(ownerId) && operatorRole.has(validation.permission);
            }
            else {
                isValid = operatorRole.isRankedHigher(ownerRole) && operatorRole.has(validation.permission);
            }

            if (isValid) {
                return;
            }
        }

        throw ERROR_FORBIDDEN;
    }
}
