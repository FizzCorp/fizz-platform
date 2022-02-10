package io.fizz.gateway.http.controllers.chat;

import io.fizz.session.SessionUtils;
import io.vertx.ext.web.Session;

public class SessionContext {
    private final String appId;
    private final String userId;
    private final String subscriberId;
    private final String locale;
    private final String channelFilter;

    public SessionContext(final Session aSession) {
        appId = SessionUtils.getAppId(aSession);
        userId = SessionUtils.getUserId(aSession);
        subscriberId = SessionUtils.getSubscriberId(aSession);
        locale = SessionUtils.getLocale(aSession);
        channelFilter = SessionUtils.getChannels(aSession);
    }

    public String appId() {
        return appId;
    }

    public String userId() {
        return userId;
    }

    public String subscriberId() {
        return subscriberId;
    }

    public String locale() {
        return locale;
    }

    public String channelFilter() {
        return channelFilter;
    }
}
