package io.fizz.chat.application;

public class Preferences {
    private Boolean forceContentModeration;

    public Boolean isForceContentModeration() {
        return forceContentModeration;
    }

    public void setForceContentModeration(final boolean aForceContentModeration) {
        this.forceContentModeration = aForceContentModeration;
    }
}
