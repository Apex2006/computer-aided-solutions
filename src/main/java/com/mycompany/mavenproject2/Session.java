package com.mycompany.mavenproject2;

/**
 * Who is using the app right now. No user means a guest, who can only
 * browse and read guides.
 *
 * @author Luis Gabriel Santiago
 */
public final class Session {

    private static User current;

    private Session() {
    }

    public static User user() {
        return current;
    }

    public static void start(User user) {
        current = user;
    }

    public static void end() {
        if (current != null) {
            current.logout();
        }
        current = null;
    }

    public static boolean isGuest() {
        return current == null;
    }

    public static boolean isExpert() {
        return current != null && current.isExpert();
    }

    public static boolean isAdmin() {
        return current != null && current.isAdmin();
    }
}
