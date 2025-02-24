package chattcp.ServerConfig;

import java.util.concurrent.ConcurrentHashMap;

public class UsuariosActivos {
    private static final ConcurrentHashMap<String, Boolean> activeUsers = new ConcurrentHashMap<>();

    public static synchronized boolean isUserActive(String username) {
        return activeUsers.getOrDefault(username, false);
    }

    public static synchronized boolean setUserActive(String username) {
        if (isUserActive(username)) {
            return false;
        }
        activeUsers.put(username, true);
        return true;
    }

    public static synchronized void setUserInactive(String username) {
        activeUsers.remove(username);
    }
}