package io.fizz.analytics.jobs.profileBuildup;

import io.fizz.analytics.common.repository.AbstractUserRepository;
import io.fizz.analytics.common.InMemoryUserRepository;

public class InMemoryProfileSegmentTransformer extends ProfileSegmentTransformer {
    @Override
    protected AbstractUserRepository userRepoFactory() {
        return new InMemoryUserRepository();
    }
}
