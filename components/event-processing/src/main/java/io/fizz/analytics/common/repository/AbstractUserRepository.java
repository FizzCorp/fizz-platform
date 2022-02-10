package io.fizz.analytics.common.repository;

import io.fizz.analytics.domain.User;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.io.IOException;
import java.util.List;

public interface AbstractUserRepository {
    User fetchWithId(final ApplicationId aAppId, final UserId aActorId) throws IOException;
    void save(final User aUser) throws IOException;
    void save(final List<User> aUsers) throws IOException;
}
