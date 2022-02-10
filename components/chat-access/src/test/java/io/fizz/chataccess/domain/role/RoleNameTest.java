package io.fizz.chataccess.domain.role;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleNameTest {
    @Test
    @DisplayName("it should create a valid role roleName")
    void validRoleNameTest() {
        final String roleName = "  AB_ab-12  ";
        final String roleNameTrimmed = "AB_ab-12";
        final RoleName name = new RoleName(roleName);
        final RoleName name2 = new RoleName(roleName);

        Assertions.assertEquals(name.value(), roleNameTrimmed);
        Assertions.assertEquals(name, name2);
        Assertions.assertNotEquals(name, null);
    }

    @Test
    @DisplayName("it should not create role with invalid input")
    void invalidRoleNameTest() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new RoleName(null),
            "invalid_role_name"
        );

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new RoleName("A B"),
            "invalid_role_name"
        );

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new RoleName("A#B"),
            "invalid_role_name"
        );

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new RoleName("012345678901234567890123456789123"),
            "invalid_role_name"
        );
    }
}
