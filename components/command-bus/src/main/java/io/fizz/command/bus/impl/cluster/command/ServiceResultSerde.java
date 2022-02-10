package io.fizz.command.bus.impl.cluster.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.fizz.common.Utils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

class ServiceResultSerde {
    private static final String TOKEN_SUCCEEDED = "success";
    private static final String TOKEN_REPLY = "reply";
    private static final String TOKEN_REPLY_TYPE = "replyT";
    private static final String TOKEN_CAUSE = "cause";
    private static final String TOKEN_CAUSE_TYPE = "causeT";

    public static <T> byte[] serialize(ServiceResult<T> aResult) {
        Utils.assertRequiredArgument(aResult, "invalid_result");

        Gson gson = new GsonBuilder().create();
        JsonObject json = new JsonObject();

        json.addProperty(TOKEN_SUCCEEDED, aResult.succeeded());

        if (Objects.nonNull(aResult.cause())) {
            json.addProperty(TOKEN_CAUSE_TYPE, aResult.cause().getClass().getName());
            json.add(TOKEN_CAUSE, gson.toJsonTree(aResult.cause()));
        }

        if (Objects.nonNull(aResult.reply())) {
            json.addProperty(TOKEN_REPLY_TYPE, aResult.reply().getClass().getName());
            json.add(TOKEN_REPLY, gson.toJsonTree(aResult.reply()));
        }

        return gson.toJson(json).getBytes(StandardCharsets.UTF_8);
    }

    public static <T> ServiceResult<T> deserialize(byte[] aBuffer) throws ClassNotFoundException {
        Utils.assertRequiredArgument(aBuffer, "invalid_buffer");

        final Gson gson = new GsonBuilder().create();

        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(new String(aBuffer, StandardCharsets.UTF_8)).getAsJsonObject();
        boolean succeeded;
        Throwable throwable = null;
        T result = null;

        succeeded = gson.fromJson(json.get(TOKEN_SUCCEEDED), Boolean.class);

        if (json.has(TOKEN_CAUSE)) {
            String type = gson.fromJson(json.get(TOKEN_CAUSE_TYPE), String.class);
            throwable = gson.<Throwable>fromJson(json.get(TOKEN_CAUSE), Class.forName(type));
        }

        if (json.has(TOKEN_REPLY)) {
            String type = gson.fromJson(json.get(TOKEN_REPLY_TYPE), String.class);
            result = gson.<T>fromJson(json.get(TOKEN_REPLY), Class.forName(type));
        }

        return new ServiceResult<>(succeeded, throwable, result);
    }
}
