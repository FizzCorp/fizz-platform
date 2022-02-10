package io.fizz.chat.moderation.domain;

import io.fizz.common.Utils;

public class ReportOffense {
    private final String offense;

    public ReportOffense(String offense) {
        Utils.assertRequiredArgument(offense, "invalid_offense");
        this.offense = offense;
    }

    public String value() {
        return offense;
    }
}
