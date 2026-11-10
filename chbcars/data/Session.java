package chbcars.data;

import chbcars.model.User;

/** Remembers who is logged in on this computer. */
public final class Session {

    private static User currentUser;

    private Session() {
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getUser() {
        return currentUser;
    }
}
