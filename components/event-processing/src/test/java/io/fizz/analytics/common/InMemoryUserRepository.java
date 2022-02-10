package io.fizz.analytics.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fizz.analytics.common.repository.AbstractUserRepository;
import io.fizz.analytics.domain.User;
import io.fizz.common.domain.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements AbstractUserRepository, Serializable {
    private static final Map<String,User> actors = new ConcurrentHashMap<>();

    @Override
    public User fetchWithId(final ApplicationId aAppId, final UserId aUserId) {
        if (Objects.isNull(aAppId)) {
            throw new IllegalArgumentException("invalid app id specified.");
        }
        if (Objects.isNull(aUserId)) {
            throw new IllegalArgumentException("invalid user id specified.");
        }

        final User user = actors.get(aAppId.value() + ":" + aUserId.value());
        return clone(user);
    }

    @Override
    public void save(final User aUser) {
        if (Objects.isNull(aUser)) {
            throw new IllegalArgumentException("invalid user specified.");
        }

        final User user = clone(aUser);
        actors.put(aUser.appId().value() + ":" + aUser.id().value(), user);
    }

    @Override
    public void save(List<User> aUsers) throws IOException {
        for (User user: aUsers) {
            save(user);
        }
    }

    private User clone(final User aUser) {
        if (Objects.isNull(aUser)) {
            return null;
        }

        final Gson gson = new GsonBuilder().create();

        return gson.fromJson(gson.toJson(aUser), User.class);
    }
}
