package io.fizz.chataccess.domain.context;

import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

class UserMembershipsTest {
    private static final UserId uid = new UserId("userA");
    private static ApplicationId appId;
    private static AuthContextId contextId;
    private static RoleName roleName;
    private static RoleName roleName2;
    private static RoleName roleName3;

    static {
        try {
            appId = new ApplicationId("appA");
            contextId = new AuthContextId(appId, "ctxA");
            roleName = new RoleName("bans");
            roleName2 = new RoleName("mutes");
            roleName3 = new RoleName("mutes2");
        }
        catch (DomainErrorException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should create a mapping with valid input")
    void validUserMappingsTest()  {
        UserMemberships appMemberships = new UserMemberships(contextId, uid);

        appMemberships.add(roleName);
        Assertions.assertEquals(appMemberships.userId(), uid);
        Assertions.assertEquals(appMemberships.contextId(), contextId);
        Assertions.assertTrue(appMemberships.effectiveRoles().contains(roleName));
        Assertions.assertTrue(appMemberships.mutationId().isNull());

        appMemberships = new UserMemberships(new AuthContextId(appId), uid);
        Assertions.assertEquals(appMemberships.userId(), uid);
        Assertions.assertEquals(appMemberships.effectiveRoles().size(), 0);

        UserMemberships contextMemberships = new UserMemberships(contextId, uid);
        contextMemberships.add(roleName);
        Assertions.assertEquals(contextMemberships.userId(), uid);
        Assertions.assertTrue(contextMemberships.effectiveRoles().contains(roleName));

        contextMemberships = new UserMemberships(contextId, uid);
        Assertions.assertEquals(contextMemberships.userId(), uid);
        Assertions.assertEquals(contextMemberships.effectiveRoles().size(), 0);
    }

    @Test
    @DisplayName("it should add or delete roles")
    void addRemoveGroupsTest() throws InterruptedException {
        UserMemberships memberships = new UserMemberships(contextId, uid);

        Assertions.assertEquals(memberships.userId(), uid);
        Assertions.assertEquals(memberships.effectiveRoles().size(), 0);

        memberships.add(roleName);
        memberships.add(roleName2);
        memberships.add(roleName3, new Date());

        TimeUnit.SECONDS.sleep(1);

        Assertions.assertEquals(2, memberships.effectiveRoles().size());
        Assertions.assertEquals(3, memberships.memberships().size());
        Assertions.assertTrue(memberships.effectiveRoles().contains(roleName));
        Assertions.assertTrue(memberships.effectiveRoles().contains(roleName2));

        memberships.remove(roleName);
        Assertions.assertFalse(memberships.effectiveRoles().contains(roleName));
    }

    @Test
    @DisplayName("it should not create mappings for invalid input")
    void invalidUserMappingsTest() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new UserMemberships(new AuthContextId(appId), null),
            "invalid_user_id"
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new UserMemberships(null, uid),
                "invalid_context_id"
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new UserMemberships(new AuthContextId(appId), uid, null),
                "invalid_mutation_id"
        );
    }

    @Test
    @DisplayName("it should manage membership correctly")
    void membershipMgmtTest() {
        final UserMemberships mapping = new UserMemberships(contextId, uid);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> mapping.add(null),
                "invalid_membership"
        );
    }
}
