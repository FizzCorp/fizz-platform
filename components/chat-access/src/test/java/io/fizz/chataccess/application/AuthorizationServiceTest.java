package io.fizz.chataccess.application;

import io.fizz.chataccess.domain.Authorization;
import io.fizz.chataccess.domain.context.AuthContextId;
import io.fizz.chataccess.domain.role.Role;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chataccess.infrastructure.persistence.hbase.HBaseUserMembershipsRepository;
import io.fizz.chataccess.infrastructure.persistence.local.InMemoryRoleRepository;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

class AuthorizationServiceTest {
    private static InMemoryRoleRepository roleRepo = new InMemoryRoleRepository();
    private final static String PERMISSION_PUBLISH_MSGS = "publishMessages";
    private final static String PERMISSION_READ_MSGS = "readMessages";
    private final static String PERMISSION_EDIT_OWN_MSG = "editOwnMessage";
    private final static String PERMISSION_DELETE_OWN_MSG = "deleteOwnMessage";
    private final static String PERMISSION_DELETE_ANY_MSG = "deleteAnyMessage";
    private final static String PERMISSION_MANAGE_MEMBERSHIP = "manageMembership";
    private final static String PERMISSION_CREATE_CHANNEL = "createChannel";

    private final static String[] PERMISSIONS_ALL = new String[]{
            PERMISSION_PUBLISH_MSGS,
            PERMISSION_READ_MSGS,
            PERMISSION_EDIT_OWN_MSG,
            PERMISSION_DELETE_OWN_MSG,
            PERMISSION_DELETE_ANY_MSG,
            PERMISSION_CREATE_CHANNEL,
            PERMISSION_MANAGE_MEMBERSHIP
    };

    private final static String[] PERMISSIONS_MEMBER = new String[]{
            PERMISSION_PUBLISH_MSGS,
            PERMISSION_READ_MSGS,
            PERMISSION_EDIT_OWN_MSG,
            PERMISSION_DELETE_OWN_MSG
    };

    private static Role ROLE_ADMIN = new Role(new RoleName("admin"), 1, PERMISSIONS_ALL);
    private static Role ROLE_BANNED = new Role(new RoleName("banned"),2);
    private static Role ROLE_OWNER = new Role(new RoleName("owner"),3, PERMISSIONS_ALL);
    private static Role ROLE_MEMBER = new Role(new RoleName("member"),4, PERMISSIONS_MEMBER);
    private static Role ROLE_OUTCAST = new Role(new RoleName("outcast"),5);
    private static Role ROLE_USER = new Role(new RoleName("user"), 6);

    private static ApplicationId appA;
    private static UserId uid = new UserId("userA");

    static {
        try {
            roleRepo.save(ROLE_ADMIN);
            roleRepo.save(ROLE_BANNED);
            roleRepo.save(ROLE_OWNER);
            roleRepo.save(ROLE_MEMBER);
            roleRepo.save(ROLE_OUTCAST);
            roleRepo.save(ROLE_USER);

            appA = new ApplicationId("appA");
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should assign application role properly")
    void appRoleAssignmentTest() throws InterruptedException, ExecutionException {
        final AuthorizationService service = buildService();

        Role effectiveRole = service.fetchAppRole(appA, uid).get();
        Assertions.assertEquals(effectiveRole, ROLE_USER);

        service.assignAppRole(appA, uid, ROLE_MEMBER.name(), Utils.TIME_END).get();
        effectiveRole = service.fetchAppRole(appA, uid).get();
        Assertions.assertEquals(effectiveRole, ROLE_MEMBER);

        service.assignAppRole(appA, uid, ROLE_ADMIN.name(), Utils.TIME_END).get();
        effectiveRole = service.fetchAppRole(appA, uid).get();
        Assertions.assertEquals(effectiveRole, ROLE_ADMIN);

        service.removeAppRole(appA, uid, ROLE_MEMBER.name()).get();
        service.removeAppRole(appA, uid, ROLE_ADMIN.name()).get();

        effectiveRole = service.fetchAppRole(appA, uid).get();
        Assertions.assertEquals(effectiveRole, ROLE_USER);
    }

    @Test
    @DisplayName("it should assign context role properly")
    void contextRoleAssignmentTest() throws InterruptedException, ExecutionException {
        final AuthorizationService service = buildService();
        final AuthContextId contextId = new AuthContextId(appA, "channelA");

        Role effectiveRole = service.fetchContextRole(contextId, uid).get();
        Assertions.assertEquals(effectiveRole, ROLE_OUTCAST);

        service.assignContextRole(contextId, uid, ROLE_MEMBER.name(), Utils.TIME_END).get();
        effectiveRole = service.fetchContextRole(contextId, uid).get();
        Assertions.assertEquals(effectiveRole, ROLE_MEMBER);

        service.assignContextRole(contextId, uid, ROLE_ADMIN.name(), Utils.TIME_END).get();
        effectiveRole = service.fetchContextRole(contextId, uid).get();
        Assertions.assertEquals(effectiveRole, ROLE_ADMIN);

        service.removeContextRole(contextId, uid, ROLE_MEMBER.name()).get();
        service.removeContextRole(contextId, uid, ROLE_ADMIN.name()).get();
        effectiveRole = service.fetchContextRole(contextId, uid).get();
        Assertions.assertEquals(effectiveRole, ROLE_OUTCAST);
    }

    @Test
    @DisplayName("it should create valid authorization")
    void authorizationTest() throws InterruptedException, ExecutionException {
        final AuthorizationService service = buildService();
        final AuthContextId contextId = new AuthContextId(appA, "channelA");
        final UserId ownerId = new UserId("owner");
        final UserId operatorId = new UserId("operator");

        service.assignAppRole(appA, operatorId, ROLE_ADMIN.name(), Utils.TIME_END).get();
        service.assignContextRole(contextId, ownerId, ROLE_MEMBER.name(), Utils.TIME_END).get();

        service.assertOperateNotOwned(contextId, ownerId, operatorId, PERMISSION_DELETE_ANY_MSG).get();
        service.assertOperateOwned(contextId, operatorId, operatorId, PERMISSION_READ_MSGS);

        final Authorization failedAuthNotOwned = service.buildAuthorization(contextId, operatorId, operatorId).get();
        Assertions.assertThrows(
                SecurityException.class,
                () -> failedAuthNotOwned.assertOperateNotOwned("readMessages").validate(),
                "forbidden"
        );

        final Authorization failedAuthOwned = service.buildAuthorization(contextId, ownerId, operatorId).get();
        Assertions.assertThrows(
                SecurityException.class,
                () -> failedAuthOwned.assertOperateOwned("readMessages").validate(),
                "forbidden"
        );

        final Authorization failedOutRankedNotOwned = service.buildAuthorization(contextId, operatorId, ownerId).get();
        Assertions.assertThrows(
                SecurityException.class,
                () -> failedOutRankedNotOwned.assertOperateNotOwned("readMessages").validate(),
                "forbidden"
        );
    }

    @Test
    @DisplayName("it should handle chat roles properly")
    void chatRolesTest() throws InterruptedException, ExecutionException {
        final AuthorizationService service = buildService();
        final AuthContextId channelA = new AuthContextId(appA, "channelA");
        final UserId admin = new UserId(UUID.randomUUID().toString());
        final UserId userA = new UserId(UUID.randomUUID().toString());
        final UserId userB = new UserId(UUID.randomUUID().toString());
        final UserId userC = new UserId(UUID.randomUUID().toString());

        service.assignAppRole(appA, admin, ROLE_ADMIN.name(), Utils.TIME_END).get();

        // add owner
        service.assertOperateOwned(channelA, admin, admin, PERMISSION_CREATE_CHANNEL);
        service.assignContextRole(channelA, userA, ROLE_OWNER.name(), Utils.TIME_END).get();

        // owner adds members
        service.assertOperateOwned(channelA, userA, userA, PERMISSION_MANAGE_MEMBERSHIP);
        service.assignContextRole(channelA, userB, ROLE_MEMBER.name(), Utils.TIME_END).get();
        service.assignContextRole(channelA, userC, ROLE_MEMBER.name(), Utils.TIME_END).get();

        Role effectiveRole = service.fetchContextRole(channelA, userB).get();
        Assertions.assertEquals(effectiveRole, ROLE_MEMBER);

        // ban user
        service.assignContextRole(channelA, userB, ROLE_BANNED.name(), Utils.TIME_END).get();
        effectiveRole = service.fetchContextRole(channelA, userB).get();
        Assertions.assertEquals(effectiveRole, ROLE_BANNED);
    }

    private AuthorizationService buildService() {
        final String NAMESPACE = "test_namespace";
        final AbstractHBaseClient client = new MockHBaseClient();
        return new AuthorizationService(
                new HBaseUserMembershipsRepository(client, NAMESPACE),
                roleRepo,
                ROLE_USER,
                ROLE_OUTCAST
        );
    }
}
