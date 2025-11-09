package utils;

/**
 * Session Manager
 * Manages current user session
 */
public class SessionManager {
    private static models.User currentUser;

    public static void setCurrentUser(models.User user) {
        currentUser = user;
    }

    public static models.User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole());
    }
}

