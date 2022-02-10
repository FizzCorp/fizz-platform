package io.fizz.command.bus.impl.cluster.command;

import io.fizz.command.bus.AbstractCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class CommandSerdeTest {
    public static class TestCommand implements AbstractCommand {
        @Override
        public byte[] key() {
            return "aKey".getBytes();
        }
    }

    @Test
    @DisplayName("it should serde a command correctly")
    void serdeTest() throws Throwable {
        TestCommand command = new TestCommand();
        byte[] buffer = CommandSerde.serialize(command);
        TestCommand serdeCmd = CommandSerde.deserialize(buffer);

        Assertions.assertNotNull(serdeCmd);
        Assertions.assertEquals(command.getClass(), serdeCmd.getClass());
        Assertions.assertTrue(Arrays.equals("aKey".getBytes(), command.key()));
    }
}
