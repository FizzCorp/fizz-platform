package io.fizz.chat.moderation.domain;

import io.fizz.common.Utils;

import java.util.List;

public class ReportedMessageSearchResult {
    final private List<ReportedMessage> reportedMessages;
    final private long resultSize;

    public ReportedMessageSearchResult(List<ReportedMessage> reportedMessages, long resultSize) {
        Utils.assertRequiredArgument(reportedMessages, "invalid_reported_message_list");
        Utils.assertArgumentRange(resultSize, 0L, Long.MAX_VALUE, "invalid_result_size");

        if (resultSize < reportedMessages.size()) {
            throw new IllegalArgumentException("invalid_result_size");
        }

        this.reportedMessages = reportedMessages;
        this.resultSize = resultSize;
    }

    public List<ReportedMessage> reportedMessages() {
        return reportedMessages;
    }

    public long resultSize() {
        return resultSize;
    }
}
