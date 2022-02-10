package io.fizz.chatcommon.domain;

import io.fizz.common.Utils;

public abstract class AbstractVersionedObject {
    private final MutationId mutationId;

    public AbstractVersionedObject(MutationId aMutationId) {
        Utils.assertRequiredArgument(aMutationId, "invalid_mutation_id");

        mutationId = aMutationId;
    }

    public MutationId mutationId() {
        return mutationId;
    }
}
