package io.fizz.command.bus;

import io.fizz.command.bus.impl.CommandBus;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ExtendWith(VertxExtension.class)
public class CommandBusTest {
    private static class TestCommand implements AbstractCommand {
        private final String value = UUID.randomUUID().toString();

        public String value() {
            return value;
        }

        @Override
        public byte[] key() {
            return "123".getBytes();
        }
    }

    public static class TestHandler {
        @CommandHandler
        public CompletableFuture<Object> handle(TestCommand aCommand) {
            return CompletableFuture.completedFuture(aCommand.value());
        }
    }

    private static class NonPublicHandlerClass {
        @CommandHandler
        public CompletableFuture<Object> handle(TestCommand aCommand) {
            return CompletableFuture.completedFuture(null);
        }
    }

    public static class NonPublicHandler {
        @CommandHandler
        private CompletableFuture<Object> handle(TestCommand aCommand) {
            return CompletableFuture.completedFuture(null);
        }
    }

    public static class NoParameterHandler {
        @CommandHandler
        private CompletableFuture<Object> handle() {
            return CompletableFuture.completedFuture(null);
        }
    }

    public static class InvalidFutureTypeHandler {
        @CommandHandler
        private Object handle(TestCommand aCommand) {
            return null;
        }
    }

    @Test
    @DisplayName("it should only allow valid handlers to be registered")
    public void invalidHandlerTest() {
        AbstractCommandBus bus = AbstractCommandBus.create(Vertx.vertx());
        Assertions.assertThrows(IllegalArgumentException.class, () -> bus.register(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bus.register(new NonPublicHandlerClass()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bus.register(new NonPublicHandler()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bus.register(new NoParameterHandler()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bus.register(new InvalidFutureTypeHandler()));
    }

    @Test
    @DisplayName("it should allow a valid handler to be registered")
    void validHandlerTest() throws Throwable {
        CommandBus bus = (CommandBus)AbstractCommandBus.create(Vertx.vertx());
        bus.register(new TestHandler());

        TestCommand cmd = new TestCommand();
        CompletableFuture<String> reply = bus.execute(cmd);
        Assertions.assertEquals(cmd.value(), reply.get());
    }
}
