package io.fizz.chataccess.domain.role;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {
    private final static String PERMISSION_READMESSAGE = "readMessages";
    private final static String PERMISSION_EDITMESSAGE = "editMessage";
    private final static String PERMISSION_PUBLISHMESSAGE = "publishMessage";

    @Test
    @DisplayName("it should create a valid role")
    void createValidRoleTest() {
        final RoleName name = new RoleName("member");
        final RoleName name2 = new RoleName("member2");

        final int rank = 1;
        final Role role = new Role(name, rank, PERMISSION_EDITMESSAGE);
        final Role role2 = new Role(name, rank, PERMISSION_EDITMESSAGE);
        final Role role3 = new  Role(name2, rank, PERMISSION_EDITMESSAGE);

        Assertions.assertEquals(role.name(), name);
        Assertions.assertEquals(role.rank(), 1);
        Assertions.assertTrue(role.has(PERMISSION_EDITMESSAGE));
        Assertions.assertFalse(role.has(PERMISSION_PUBLISHMESSAGE));
        Assertions.assertEquals(role, role2);
        Assertions.assertNotEquals(role, null);
        Assertions.assertNotEquals(role, role3);
    }

    @Test
    @DisplayName("it should not create role for invalid input")
    void invalidInputTest() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Role(null, 1),
            "invalid_role_name"
        );

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Role(new RoleName("123456789012345678901234567890123"), 1),
            "invalid_role_name"
        );

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Role(new RoleName("role"), -1, PERMISSION_PUBLISHMESSAGE),
            "invalid_role_rank"
        );

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Role(new RoleName("role"), 0, (String[])null),
            "invalid_role_permissions"
        );
    }

    @Test
    @DisplayName("it should compare rank correctly")
    void rankComparisonTest() {
        final Role role1 = new Role(new RoleName("role1"), 1, PERMISSION_READMESSAGE);
        final Role role2 = new Role(new RoleName("role2"), 2, PERMISSION_PUBLISHMESSAGE);
        final Role role3 = new Role(new RoleName("role3"),1, PERMISSION_READMESSAGE, PERMISSION_PUBLISHMESSAGE);

        Assertions.assertTrue(role1.isRankedHigher(role2));
        Assertions.assertFalse(role2.isRankedHigher(role1));

        Assertions.assertTrue(role1.isRankedHigher(role3));
        Assertions.assertFalse(role3.isRankedHigher(role1));

        Assertions.assertTrue(role1.isRankedHigher(null));
    }

    @Test
    @DisplayName("it should compose ranks properly")
    void composeRanksTest() {
        final Role role1 = new Role(new RoleName("role1"), 1, PERMISSION_READMESSAGE);
        final Role role2 = new Role(new RoleName("role2"), 2, PERMISSION_PUBLISHMESSAGE);
        final Role role3 = new Role(new RoleName("role3"), 3, PERMISSION_READMESSAGE, PERMISSION_PUBLISHMESSAGE);

        Role effectiveRole = Role.compose(role1, role2);
        Assertions.assertEquals(effectiveRole, role1);

        effectiveRole = Role.compose(role2, role3);
        Assertions.assertEquals(effectiveRole, role2);

        effectiveRole = Role.compose(role1, role3);
        Assertions.assertEquals(effectiveRole, role1);

        effectiveRole = Role.compose(null, role3);
        Assertions.assertEquals(effectiveRole, role3);

        Assertions.assertNull(Role.compose((Role[]) null));
        Assertions.assertNull(Role.compose());
        Assertions.assertNull(Role.compose(null, null, null));
    }

    @Test
    @DisplayName("it should compose same ranks properly")
    void composeSameRanksTest() {
        final Role role1 = new Role(new RoleName("role1"), 1, PERMISSION_READMESSAGE);
        final Role role2 = new Role(new RoleName("role2"), 1, PERMISSION_PUBLISHMESSAGE);

        Role effectiveRole = Role.compose(role1, role2);
        Assertions.assertEquals(effectiveRole, role1);

        effectiveRole = Role.compose(role2, role1);
        Assertions.assertEquals(effectiveRole, role1);
    }

    @Test
    @DisplayName("it should compose same role names properly")
    void composeSameRoleNameTest() {
        final Role role1 = new Role(new RoleName("role1"), 1, PERMISSION_READMESSAGE);
        final Role role2 = new Role(new RoleName("role1"), 1, PERMISSION_PUBLISHMESSAGE);

        Role effectiveRole = Role.compose(role1, role2);
        Assertions.assertSame(effectiveRole, role1);

        effectiveRole = Role.compose(role2, role1);
        Assertions.assertSame(effectiveRole, role2);
    }
}
