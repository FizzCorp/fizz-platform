package io.fizz.chat.group.application.group;

import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.command.bus.AbstractCommand;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class PublishGroupMessageCommand implements AbstractCommand {
    private final GroupId groupId;
    private final UserId authorId;
    private final String nick;
    private final String body;
    private final String data;
    private final String locale;
    private final boolean translate;
    private final boolean filter;
    private final boolean persist;
    private final boolean internal;

    public PublishGroupMessageCommand(final String aAppId,
                                      final String aGroupId,
                                      final String aAuthorId,
                                      final String aNick,
                                      final String aBody,
                                      final String aData,
                                      final String aLocale,
                                      final Boolean aTranslate,
                                      final Boolean aFilter,
                                      final Boolean aPersist,
                                      final Boolean aInternal) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);

            this.groupId = new GroupId(appId, aGroupId);
            this.authorId = new UserId(aAuthorId);
            this.nick = aNick;
            this.body = aBody;
            this.data = aData;
            this.locale = aLocale;
            this.translate = Objects.nonNull(aTranslate) && aTranslate;
            this.filter = Objects.isNull(aFilter) || aFilter;
            this.persist = Objects.isNull(aPersist) || aPersist;
            this.internal = Objects.isNull(aInternal) || aInternal;
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    @Override
    public byte[] key() {
        return groupId.qualifiedValue().getBytes(StandardCharsets.UTF_8);
    }

    public GroupId groupId() {
        return groupId;
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

    public String locale() {
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

    public boolean isInternal() {
        return internal;
    }
}
