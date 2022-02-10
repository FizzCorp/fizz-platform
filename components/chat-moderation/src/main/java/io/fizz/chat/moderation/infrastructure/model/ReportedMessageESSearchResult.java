package io.fizz.chat.moderation.infrastructure.model;

import java.util.List;

public class ReportedMessageESSearchResult {
    final private List<ReportedMessageES> reportedMessages;
    final private long resultSize;

    public ReportedMessageESSearchResult(List<ReportedMessageES> reportedMessages, long resultSize) {
        this.reportedMessages = reportedMessages;
        this.resultSize = resultSize;
    }

    public List<ReportedMessageES> reportedMessages() {
        return reportedMessages;
    }

    public long resultSize() {
        return resultSize;
    }
}
