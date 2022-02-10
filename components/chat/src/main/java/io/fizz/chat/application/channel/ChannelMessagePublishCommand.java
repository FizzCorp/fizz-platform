package io.fizz.chat.application.channel;

import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;

import java.util.Set;
import java.util.Objects;

public class ChannelMessagePublishCommand extends AbstractChannelCommand {
    public static class Builder {
        private ChannelId channelId;
        private String authorId;
        private String nick;
        private String body;
        private String data;
        private String locale;
        private boolean translate;
        private boolean filter;
        private boolean persist;
        private Set<UserId> notifyList;
        private boolean internal = false;

        public ChannelMessagePublishCommand build() {
            return new ChannelMessagePublishCommand(
                    channelId,
                    authorId,
                    nick, body,
                    data, locale,
                    translate,
                    filter,
                    persist,
                    notifyList,
                    internal
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

        public Builder setTranslate(boolean translate) {
            this.translate = translate;
            return this;
        }

        public Builder setFilter(boolean filter) {
            this.filter = filter;
            return this;
        }

        public Builder setPersist(boolean persist) {
            this.persist = persist;
            return this;
        }

        public Builder setNotifyList(Set<UserId> aNotifyList) {
            this.notifyList = aNotifyList;
            return this;
        }

        public boolean isInternal() {
            return internal;
        }

        public Builder setInternal(boolean aInternal) {
            this.internal = aInternal;
            return this;
        }
    }

    private final UserId authorId;
    private final String nick;
    private final String body;
    private final String data;
    private final LanguageCode locale;
    private final boolean translate;
    private final boolean filter;
    private final boolean persist;
    private final Set<UserId> notifyList;
    private final boolean internal;

    public ChannelMessagePublishCommand(final ChannelId aChannelId,
                                        final String aAuthorId,
                                        final String aNick,
                                        final String aBody,
                                        final String aData,
                                        final String aLocale,
                                        final boolean aTranslate,
                                        final boolean aFilter,
                                        final boolean aPersist,
                                        final Set<UserId> aNotifyList,
                                        final boolean aInternal) {
        super(aChannelId);

        try {
            authorId = new UserId(aAuthorId);
            nick = aNick;
            body = aBody;
            data = aData;
            locale = Objects.isNull(aLocale) ? null : LanguageCode.fromValue(aLocale);
            translate = aTranslate;
            filter = aFilter;
            persist = aPersist;
            notifyList = aNotifyList;
            internal = aInternal;
        } catch (DomainErrorException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public UserId authorId() {
        return authorId;
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

    public boolean isTranslate() {
        return translate;
    }

    public boolean isFilter() {
        return filter;
    }

    public boolean isPersist() {
        return persist;
    }

    public Set<UserId> notifyList() {
        return notifyList;
    }
    public boolean isInternal() {
        return internal;
    }
}
