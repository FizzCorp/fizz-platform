package io.fizz.command.bus.impl.cluster.command;

class ServiceResult <T> {
    private final boolean succeeded;
    private final Throwable cause;
    private final T reply;

    public ServiceResult(boolean aSucceeded, Throwable aCause, T aReply) {
        this.succeeded = aSucceeded;
        this.cause = aCause;
        this.reply = aReply;
    }

    public boolean succeeded() {
        return succeeded;
    }

    public Throwable cause() {
        return cause;
    }

    public T reply() {
        return reply;
    }
}
