package io.fizz.command.bus.impl.cluster.command;

public interface AbstractCommandBusMetrics {
    void commandExecutionStarted();
    void commandExecutionEnded();
    void commandSent();
    void commandReplied();
    void commandReceived();
    void commandFailed();
}
