package integration;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import app.dao.PermissionRepository;
import app.dao.RoleRepository;
import app.entity.PermissionEntity;
import app.entity.RoleEntity;
import app.enums.Permissions;

public class PermissionsTest extends BaseTest {
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testPermissionIntegrity() {
        List<PermissionEntity> fromDB = permissionRepository.findAll();
        Permissions[] fromEnum = Permissions.values();

        // Checking necessary elements in DB
        Set<String> dbSet = fromDB.stream().map(e -> e.getName()).collect(Collectors.toSet());
        for (Permissions t : fromEnum) {
            assertTrue("Element '" + t.name() + "' is not found in database", dbSet.contains(t.name()));
        }

        // Checking useless elements in DB
        Set<String> enumSet = Arrays.stream(fromEnum).map(e -> e.name()).collect(Collectors.toSet());
        for (PermissionEntity t : fromDB) {
            assertTrue("Element '" + t.getName() + "' found in database, but never used", enumSet.contains(t.getName()));
        }
    }

    @Test
    public void testGhostPermissions() {
        List<PermissionEntity> permissions = permissionRepository.findAll();
        List<RoleEntity> roles = roleRepository.findAll();
        EnumSet<Permissions> noNeedToCheck = EnumSet.noneOf(Permissions.class);
        noNeedToCheck.addAll(Arrays.asList(Permissions.SYSTEM));

        for (PermissionEntity permission : permissions) {
            if (noNeedToCheck.contains(Permissions.valueOf(permission.getName()))) {
                continue;
            }
            boolean found = false;
            for (RoleEntity role : roles) {
                if (role.getPermissions().contains(permission)) {
                    found = true;
                }
            }
            assertTrue("Permission " + permission.getName() + " is not assigned to any Role", found);
        }
    }
}
