package app.util;

import java.util.Set;

import app.entity.Permissions;
import app.entity.UserEntity;

public class SecurityUtils {
    public static boolean hasPermission(UserEntity user, Permissions permission) {
        Set<String> permissions = user.getPermissions();
        boolean result = permissions.contains(permission.name());
        return result;
    }
}
