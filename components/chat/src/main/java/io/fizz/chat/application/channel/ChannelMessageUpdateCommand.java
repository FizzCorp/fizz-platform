package io.fizz.chat.application.channel;

import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;

import java.util.Set;
import java.util.Objects;

public class ChannelMessageUpdateCommand extends AbstractChannelCommand {
    public static class Builder {
        private ChannelId channelId;
        private String authorId;
        private long messageId;
        private String nick;
        private String body;
        private String data;
        private String locale;
        private Boolean translate;
        private Boolean filter;
        private Set<UserId> notifyList;

        public ChannelMessageUpdateCommand build() {
            return new ChannelMessageUpdateCommand(
                    channelId, authorId, messageId, nick, body, data, locale, translate, filter, notifyList
            );
        }

        public Builder setChannelId(ChannelId channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder setAuthorId(String authorId) {
            this.authorId = authorId;
            return this;
        }

        public Builder setMessageId(long messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder setNick(String nick) {
            this.nick = nick;
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setLocale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder setTranslate(Boolean translate) {
            this.translate = translate;
            return this;
        }

        public Builder setFilter(Boolean filter) {
            this.filter = filter;
            return this;
        }

        public Builder setNotifyList(Set<UserId> notifyList) {
            this.notifyList = notifyList;
            return this;
        }
    }

    private final UserId authorId;
    private final long messageId;
    private final String nick;
    private final String body;
    private final String data;
    private final LanguageCode locale;
    private final Boolean translate;
    private final Boolean filter;
    private final Set<UserId> notifyList;

    public ChannelMessageUpdateCommand(final ChannelId aChannelId,
                                       final String aAuthorId,
                                       final long aMessageId,
                                       final String aNick,
                                       final String aBody,
                                       final String aData,
                                       final String aLocale,
                                       final Boolean aTranslate,
                                       final Boolean aFilter,
                                       final Set<UserId> aNotifyList) {
        super(aChannelId);

        try {
            authorId = new UserId(aAuthorId);
            messageId = aMessageId;
            nick = aNick;
            body = aBody;
            data = aData;
            locale = Objects.isNull(aLocale) ? null : LanguageCode.fromValue(aLocale);
            translate = aTranslate;
            filter = aFilter;
            notifyList = aNotifyList;
        } catch (DomainErrorException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public UserId authorId() {
        return authorId;
    }

    public long messageId() {
        return messageId;
    }

    public String nick() {
        return nick;
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

    public Boolean translate() {
        return translate;
    }

    public Boolean filter() {
        return filter;
    }

    public Set<UserId> notifyList() {
        return notifyList;
    }
}
