package io.fizz.session;

import io.vertx.ext.web.Session;

import java.util.Objects;

public final class SessionUtils {
    private static final String KEY_APP_ID = "app_id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_SUBSCRIBER_ID = "sub_id";
    private static final String KEY_LOCALE = "locale";
    private static final String KEY_CHANNELS = "channels";

    public static String getAppId(final Session aSession) {
        if (Objects.isNull(aSession)) {
            return null;
        }
        return aSession.get(KEY_APP_ID);
    }

    public static void setAppId(final Session aSession, final String aValue) {
        if (!Objects.isNull(aSession)) {
            aSession.put(KEY_APP_ID, aValue);
        }
    }

    public static String getUserId(final Session aSession) {
        if (Objects.isNull(aSession)) {
            return null;
        }

        return aSession.get(KEY_USER_ID);
    }

    public static void setUserId(final Session aSession, final String aValue) {
        if (!Objects.isNull(aSession)) {
            aSession.put(KEY_USER_ID, aValue);
        }
    }

    public static String getSubscriberId(final Session aSession) {
        if (Objects.isNull(aSession)) {
            return null;
        }

        return aSession.get(KEY_SUBSCRIBER_ID);
    }

    public static void setSubscriberId(final Session aSession, final String aValue) {
        if (!Objects.isNull(aSession)) {
            aSession.put(KEY_SUBSCRIBER_ID, aValue);
        }
    }

    public static String getLocale(final Session aSession) {
        if (Objects.isNull(aSession)) {
            return null;
        }

        return aSession.get(KEY_LOCALE);
    }

    public static void setLocale(final Session aSession, final String aValue) {
        if (!Objects.isNull(aSession)) {
            aSession.put(KEY_LOCALE, aValue);
        }
    }

    public static String getChannels(final Session aSession) {
        if (Objects.isNull(aSession)) {
            return null;
        }

        return aSession.get(KEY_CHANNELS);
    }

    public static void setChannelFilter(final Session aSession, final String aValue) {
        if (!Objects.isNull(aSession)) {
            aSession.put(KEY_CHANNELS, aValue);
        }
    }


}
