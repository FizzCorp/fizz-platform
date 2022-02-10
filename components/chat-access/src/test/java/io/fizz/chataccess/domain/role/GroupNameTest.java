package io.fizz.chataccess.domain.role;

import io.fizz.chataccess.domain.context.AuthContextId;
import io.fizz.chataccess.domain.role.GroupName;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupNameTest {
    private static ApplicationId appA;
    private static AuthContextId contextId;
    private static RoleName GROUP_NAME = new RoleName(" muTEs0 ");

    static {
        try {
            appA = new ApplicationId("appA");
            contextId = new AuthContextId(appA, "contextA");
        }
        catch (Exception ex) { System.out.println(ex.getMessage()); }
    }

    @Test
    @DisplayName("it should create a valid group roleName")
    void validGroupNameTest() {
        final GroupName appGroupName = new GroupName(appA, GROUP_NAME);
        Assertions.assertEquals(appGroupName.appId(), appA);
        Assertions.assertEquals(appGroupName.contextId(), new AuthContextId(appA));
        Assertions.assertEquals(appGroupName.roleName(), GROUP_NAME);

        final GroupName contextGroupName = new GroupName(contextId, GROUP_NAME);
        Assertions.assertEquals(contextGroupName.contextId(), contextId);
        Assertions.assertEquals(contextGroupName.roleName(), GROUP_NAME);

        final GroupName appGroupName2 = new GroupName(appA, GROUP_NAME);
        Assertions.assertEquals(appGroupName, appGroupName2);
        Assertions.assertNotEquals(appGroupName, contextGroupName);
        Assertions.assertNotEquals(appGroupName, null);
    }

    @Test
    @DisplayName("it should handle multiple applications")
    void multipleApplicationsTest() throws DomainErrorException {
        final GroupName nameA = new GroupName(appA, GROUP_NAME);
        final GroupName nameB = new GroupName(new ApplicationId("appB"), GROUP_NAME);

        Assertions.assertNotEquals(nameA, nameB);
    }

    @Test
    @DisplayName("it should handle multiple contexts")
    void multipleContextTest() throws DomainErrorException {
        final AuthContextId sameAppContext = new AuthContextId(appA, "contextB");
        final AuthContextId differentAppContext = new AuthContextId(new ApplicationId("appB"), contextId.value());

        final GroupName nameA = new GroupName(contextId, GROUP_NAME);
        final GroupName nameB = new GroupName(sameAppContext, GROUP_NAME);
        final GroupName nameC = new GroupName(differentAppContext, GROUP_NAME);

        Assertions.assertNotEquals(nameA, nameB);
        Assertions.assertNotEquals(nameA, nameC);
    }

    @Test
    @DisplayName("it should not create a group roleName with invalid input")
    void invalidInputGroupNameTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupName((ApplicationId) null, GROUP_NAME),
                "invalid_app_id"
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupName((AuthContextId) null, GROUP_NAME),
                "invalid_app_id"
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupName(contextId, null),
                "invalid_group_name"
        );
    }
}
