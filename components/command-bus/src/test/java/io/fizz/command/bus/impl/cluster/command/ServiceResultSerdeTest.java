package io.fizz.command.bus.impl.cluster.command;

import io.fizz.command.bus.AbstractCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ServiceResultSerdeTest {
    public static class TestCommand implements AbstractCommand {
        private final byte[] key;

        public TestCommand(byte[] aKey) {
            key = aKey;
        }

        @Override
        public byte[] key() {
            return key;
        }
    }

    @Test
    @DisplayName("it should serde a successful result correctly")
    void serdeTest() throws Throwable {
        TestCommand cmd = new TestCommand("123".getBytes());
        ServiceResult<TestCommand> result = new ServiceResult<>(true, null, cmd);
        byte[] buffer = ServiceResultSerde.serialize(result);
        ServiceResult<TestCommand> result2 = ServiceResultSerde.deserialize(buffer);

        Assertions.assertTrue(result2.succeeded());
        Assertions.assertNull(result2.cause());
        Assertions.assertNotNull(result2.reply());
        Assertions.assertEquals(result2.reply().getClass(), TestCommand.class);
    }

    @Test
    @DisplayName("it should serde a failed result correctly")
    void failedResultSerdeTest() throws Throwable {
        ServiceResult<TestCommand> result = new ServiceResult<>(
                false, new IllegalArgumentException("failed"), null
        );
        byte[] buffer = ServiceResultSerde.serialize(result);
        ServiceResult<TestCommand> result2 = ServiceResultSerde.deserialize(buffer);

        Assertions.assertFalse(result2.succeeded());
        Assertions.assertNull(result2.reply());
        Assertions.assertTrue(result2.cause() instanceof IllegalArgumentException);
        Assertions.assertEquals("failed", result2.cause().getMessage());
    }
}
