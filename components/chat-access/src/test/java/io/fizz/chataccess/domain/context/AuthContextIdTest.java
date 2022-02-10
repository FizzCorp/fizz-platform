package io.fizz.chataccess.domain.context;

import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthContextIdTest {
    @Test
    @DisplayName("it should create a valid context id")
    void validContextIdCreationTest() throws DomainErrorException {
        final ApplicationId appId = new ApplicationId("appA");
        final String contextId = " contextA ";
        final String contextIdTrimmed = contextId.trim();

        final AuthContextId id = new AuthContextId(appId, contextId);
        final AuthContextId id2 = new AuthContextId(appId, contextId);
        final AuthContextId id3 = new AuthContextId(appId, "contextB");
        final AuthContextId id4 = new AuthContextId(new ApplicationId("appB"), contextId);

        Assertions.assertEquals(id.appId(), appId);
        Assertions.assertEquals(id.value(), contextIdTrimmed);
        Assertions.assertEquals(id, id2);
        Assertions.assertNotEquals(id, id3);
        Assertions.assertNotEquals(id, id4);
        Assertions.assertNotEquals(id, null);
    }

    @Test
    @DisplayName("it should not create context id for invalid input")
    void invalidContextIdCreationTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new AuthContextId(null, "context1"),
                "invalid_app_id"
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new AuthContextId(new ApplicationId("appA"), null),
                "invalid_context_id"
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new AuthContextId(new ApplicationId("appA"), "c ontex t"),
                "invalid_context_id"
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new AuthContextId(new ApplicationId("appA"), "c#ontex#t"),
                "invalid_context_id"
        );
    }
}
