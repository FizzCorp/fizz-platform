package io.fizz.chat.presence;

import java.util.Date;
import java.util.Objects;

public class Presence {
    private final long lastSeenMs;

    public Presence(long lastSeenMs) {
        this.lastSeenMs = lastSeenMs;
    }

    public long lastSeen() {
        return lastSeenMs;
    }

    public static boolean isOnline(Presence aPresence, long aHeartbeatDur) {
        long now = new Date().getTime();

        return Objects.nonNull(aPresence) && (now - aPresence.lastSeen()) <= aHeartbeatDur;
    }
}
