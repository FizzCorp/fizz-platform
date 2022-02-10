package io.fizz.chat.application;

import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;

public class Application {
    private final ApplicationId id;
    private FCMConfiguration configFCM;
    private Preferences prefs;

    public Application(final ApplicationId aId) {
        Utils.assertRequiredArgument(aId, "invalid_application_id");

        this.id = aId;
    }

    public Application(final ApplicationId aId, FCMConfiguration aConfigFCM) {
        Utils.assertRequiredArgument(aId, "invalid_application_id");

        this.id = aId;
        this.configFCM = aConfigFCM;
    }

    public void setPrefs(final Preferences aPrefs) {
        this.prefs = aPrefs;
    }

    public void setConfigFCM(final FCMConfiguration aConfig) {
        this.configFCM = aConfig;
    }

    public ApplicationId id() {
        return id;
    }

    public Preferences prefs() {
        return prefs;
    }

    public FCMConfiguration configFCM() {
        return configFCM;
    }
}
