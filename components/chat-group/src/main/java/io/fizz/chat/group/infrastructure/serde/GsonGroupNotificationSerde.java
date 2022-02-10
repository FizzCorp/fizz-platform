package io.fizz.chat.group.infrastructure.serde;

import com.google.gson.*;
import io.fizz.chat.group.application.notifications.AbstractGroupNotificationSerde;
import io.fizz.chat.group.application.notifications.GroupMemberNotification;
import io.fizz.chat.group.application.notifications.GroupProfileNotification;

import java.lang.reflect.Type;

public class GsonGroupNotificationSerde implements AbstractGroupNotificationSerde {
    static class MemberNotificationSerializer implements JsonSerializer<GroupMemberNotification> {
        private static final String KEY_ID = "id";
        private static final String KEY_STATE = "state";
        private static final String KEY_ROLE = "role";

        @Override
        public JsonElement serialize(final GroupMemberNotification aNotification,
                                     final Type aType,
                                     final JsonSerializationContext aContext) {
            final JsonObject json = new JsonObject();

            json.addProperty(KEY_ID, aNotification.memberId().value());
            json.addProperty(KEY_STATE, aNotification.state().value());
            json.addProperty(KEY_ROLE, aNotification.role().value());

            return json;
        }
    }

    static class ProfileNotificationSerializer implements JsonSerializer<GroupProfileNotification> {
        private static final String KEY_REASON = "reason";
        private static final String KEY_TITLE = "title";
        private static final String KEY_IMAGE_URL = "image_url";
        private static final String KEY_DESCRIPTION = "description";
        private static final String KEY_TYPE = "type";

        @Override
        public JsonElement serialize(final GroupProfileNotification aNotification,
                                     final Type aType,
                                     final JsonSerializationContext aContext) {
            final JsonObject json = new JsonObject();

            json.addProperty(KEY_REASON, "profile");
            json.addProperty(KEY_TITLE, aNotification.title());
            json.addProperty(KEY_IMAGE_URL, aNotification.imageURL());
            json.addProperty(KEY_DESCRIPTION, aNotification.description());
            json.addProperty(KEY_TYPE, aNotification.type());

            return json;
        }
    }

    @Override
    public String serialize(final GroupMemberNotification aNotification) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(GroupMemberNotification.class, new MemberNotificationSerializer())
                .create();
        return gson.toJson(aNotification);
    }

    @Override
    public String serialize(GroupProfileNotification aNotification) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(GroupProfileNotification.class, new ProfileNotificationSerializer())
                .create();
        return gson.toJson(aNotification);
    }
}
