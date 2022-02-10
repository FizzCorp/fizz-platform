package io.fizz.command.bus.impl.cluster.command;

import com.google.gson.*;
import io.fizz.command.bus.AbstractCommand;
import io.fizz.common.Utils;

import java.nio.charset.StandardCharsets;

class CommandSerde {
    private static final String TOKEN_TYPE = "type";
    private static final String TOKEN_VALUE = "value";

    public static byte[] serialize(AbstractCommand aCommand) {
        Utils.assertRequiredArgument(aCommand, "invalid_command");

        final Gson gson = new GsonBuilder().create();

        JsonObject json = new JsonObject();

        json.addProperty(TOKEN_TYPE, aCommand.getClass().getName());
        json.add(TOKEN_VALUE, gson.toJsonTree(aCommand));

        return gson.toJson(json).getBytes(StandardCharsets.UTF_8);
    }

    public static <T extends AbstractCommand> T deserialize(byte[] aBuffer) throws ClassNotFoundException {
        Utils.assertRequiredArgument(aBuffer, "invalid_buffer");

        Gson gson = new Gson();
        JsonParser parser = new JsonParser();

        JsonObject json = parser.parse(new String(aBuffer, StandardCharsets.UTF_8)).getAsJsonObject();

        String type = gson.fromJson(json.get(TOKEN_TYPE), String.class);

        return gson.<T>fromJson(json.get(TOKEN_VALUE), Class.forName(type));
    }
}
