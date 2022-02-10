package io.fizz.chat.moderation.domain;

import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

public class ReportedUser {
    private final UserId userId;
    private final long count;

    public ReportedUser(UserId userId, long count) {
        Utils.assertRequiredArgument(userId, "invalid_user_id");
        Utils.assertArgumentRange(count, 0L, Long.MAX_VALUE, "invalid_count");
        this.userId = userId;
        this.count = count;
    }

    public UserId userId() {
        return userId;
    }

    public long count() {
        return count;
    }
}
