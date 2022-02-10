package io.fizz.chat.domain.channel;

import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChannelMessage {
    static final int MAX_NICK_LEN = 64;
    static final int MAX_BODY_LEN = 1024;
    static final int MAX_DATA_LEN = 1024;

    private static final IllegalArgumentException ERROR_INVALID_NICK =
                                                        new IllegalArgumentException("invalid_message_nick");
    private static final IllegalArgumentException ERROR_INVALID_BODY =
                                                        new IllegalArgumentException("invalid_message_body");
    private static final IllegalArgumentException ERROR_INVALID_DATA =
                                                        new IllegalArgumentException("invalid_message_data");
    private static final IllegalArgumentException ERROR_INVALID_CREATED =
                                                        new IllegalArgumentException("invalid_message_created");

    private final ChannelMessageId id;
    private final UserId from;
    private String nick;
    private final ChannelId to;
    private final TopicId topic;
    private String body;
    private String data;
    private LanguageCode locale;
    private boolean deleted;
    private final long created;
    private long updated;
    private Map<LanguageCode,String> translations;

    public ChannelMessage(long aId,
                          final UserId aFrom,
                          final String aNick,
                          final ChannelId aTo,
                          final TopicId aTopic,
                          final String aBody,
                          final String aData,
                          final LanguageCode aLocale,
                          final Date aCreated,
                          final Date aUpdated,
                          final Boolean aDeleted,
                          final Map<LanguageCode,String> aTranslations) {
        this(aId, aFrom, aNick, aTo, aTopic, aBody, aData, aLocale, aCreated, aTranslations);

        Utils.assertRequiredArgument(aUpdated, "invalid_message_updated");

        updated = aUpdated.getTime();
        deleted = aDeleted;
    }

    public ChannelMessage(long aId,
                          final UserId aFrom,
                          final String aNick,
                          final ChannelId aTo,
                          final TopicId aTopic,
                          final String aBody,
                          final String aData,
                          final LanguageCode aLocale,
                          final Date aCreated,
                          final Map<LanguageCode,String> aTranslations) {
        Utils.assertRequiredArgument(aTo, "invalid_message_to");
        Utils.assertRequiredArgument(aTopic, "invalid_message_topic");
        Utils.assertRequiredArgument(aFrom, "invalid_message_from");
        Utils.assertRequiredArgument(aCreated, ERROR_INVALID_CREATED);

        id = new ChannelMessageId(aId);
        from = aFrom;
        setNick(aNick);
        to = aTo;
        topic = aTopic;
        setBody(aBody, aLocale);
        setData(aData);
        created = aCreated.getTime();
        updated = created;
        translations = Objects.isNull(aTranslations) ? new HashMap<>() : aTranslations;
    }

    void updateNick(final String aValue) {
        setNick(aValue);
    }

    void updateBody(final String aValue, final LanguageCode aLocale) {
        setBody(aValue, aLocale);
    }

    void updateData(final String aValue) {
        setData(aValue);
    }

    void markAsDeleted() {
        deleted = true;
    }

    public void setTranslation(final LanguageCode aLanguage, final  String aContent) {
        translations.put(aLanguage, aContent);
    }

    public long id() {
        return id.value();
    }

    public UserId from() {
        return from;
    }

    public String nick() {
        return nick;
    }

    public ChannelId to() {
        return to;
    }

    public TopicId topic() {
        return topic;
    }

    public String body() {
        return body;
    }

    public String data() {
        return data;
    }

    public LanguageCode locale() {
        return locale;
    }

    public Map<LanguageCode, String> translations() {
        return translations;
    }

    public long created() {
        return created;
    }

    public long updated() {
        return updated;
    }

    public boolean deleted() {
        return deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelMessage message = (ChannelMessage) o;
        return id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void setNick(final String aValue) {
        final String value = Objects.isNull(aValue) ? null : aValue.trim();

        Utils.assertOptionalArgumentLength(value, MAX_NICK_LEN, ERROR_INVALID_NICK);

        nick = value;
        updated = new Date().getTime();
    }

    private void setBody(final String aValue, final LanguageCode aLocale) {
        final String value = Objects.isNull(aValue) ? "" : aValue.trim();

        Utils.assertOptionalArgumentLength(value, MAX_BODY_LEN, ERROR_INVALID_BODY);

        if (!Objects.isNull(body) && !body.equals(value)) {
            translations = null;
        }

        body = value;
        locale = aLocale;
        updated = new Date().getTime();
    }

    private void setData(final String aValue) {
        final String value = Objects.isNull(aValue) ? null : aValue.trim();

        Utils.assertOptionalArgumentLength(value, MAX_DATA_LEN, ERROR_INVALID_DATA);

        data = value;
        updated = new Date().getTime();
    }
}
