package chbcars.service;

import chbcars.data.DataStore;
import chbcars.data.Session;
import chbcars.model.Admin;
import chbcars.model.Staff;
import chbcars.model.User;
import chbcars.util.Format;
import chbcars.util.PasswordUtil;
import chbcars.util.ValidationException;
import chbcars.util.Validator;

public class AuthService {

    public static final int MAX_TRIES = 3;

    private final DataStore db = DataStore.get();
    private int unknownUserFailures = 0;

    public User login(String username, String password) throws ValidationException {
        String name = Validator.requireText(username, "Username");
        Validator.requireText(password, "Password");

        User user = db.users.findByUsername(name);
        if (user == null) {
            return failUnknownUser();
        }
        checkAccountStatus(user);

        if (!PasswordUtil.matches(user.getUsername(), password, user.getPasswordHash())) {
            return failWrongPassword(user);
        }
        user.setFailedAttempts(0);
        db.users.update(user);
        Session.login(user);
        db.log("Login", user.getRole() + " logged in");
        return user;
    }

    private User failUnknownUser() throws AuthException {
        unknownUserFailures++;
        int left = MAX_TRIES - unknownUserFailures;
        if (left <= 0) {
            throw new AuthException("Too many invalid login attempts.\nThe system will now close.", true);
        }
        throw new AuthException("Invalid username or password.\nAttempts left: " + left, false);
    }

    private void checkAccountStatus(User user) throws AuthException {
        if (User.BLOCKED.equals(user.getStatus())) {
            throw new AuthException("This account is BLOCKED.\nOnly the admin can unblock it.", false);
        }
        if (User.PENDING.equals(user.getStatus())) {
            throw new AuthException("Your account is still waiting for the admin's approval.", false);
        }
        if (User.DEACTIVATED.equals(user.getStatus())) {
            throw new AuthException("This account has been deactivated. Please contact the admin.", false);
        }
    }

    private User failWrongPassword(User user) throws AuthException {
        int failed = user.getFailedAttempts() + 1;
        user.setFailedAttempts(failed);
        if (failed < MAX_TRIES) {
            db.users.update(user);
            throw new AuthException("Invalid username or password.\nAttempts left: " + (MAX_TRIES - failed), false);
        }
        String message;
        if (user.canBeBlocked()) {
            user.setStatus(User.BLOCKED);
            message = "Three invalid attempts. Your account is now BLOCKED.\nOnly the admin can unblock it.\nThe system will now close.";
        } else {
            user.setFailedAttempts(0);
            message = "Three invalid attempts.\nThe system will now close.";
        }
        db.users.update(user);
        db.logs.add(new chbcars.model.ActivityLog(Format.now(), user.getUsername(), user.getRole(),
                "Failed Login", "3 invalid attempts"));
        throw new AuthException(message, true);
    }

    public void signUp(String fullName, String contact, String email, String username,
            String password, String confirm) throws ValidationException {
        User user = buildUser(false, fullName, contact, email, username);
        Validator.checkPasswordStrength(password);
        if (!password.equals(confirm)) {
            throw new ValidationException("Password and confirmation do not match.");
        }
        user.setPasswordHash(PasswordUtil.hash(user.getUsername(), password));
        user.setStatus(User.PENDING);
        db.users.add(user);
        db.logs.add(new chbcars.model.ActivityLog(Format.now(), user.getUsername(), "Staff",
                "Sign Up", "Requested a staff account"));
    }

    public String createAccount(boolean admin, String fullName, String contact, String email,
            String username) throws ValidationException {
        requireAdmin();
        User user = buildUser(admin, fullName, contact, email, username);
        String password = PasswordUtil.generate();
        user.setPasswordHash(PasswordUtil.hash(user.getUsername(), password));
        user.setStatus(User.ACTIVE);
        user.setMustChangePassword(true);
        db.users.add(user);
        db.log("Account Created", user.getRole() + " account " + user.getUsername());
        return password;
    }

    private User buildUser(boolean admin, String fullName, String contact, String email,
            String username) throws ValidationException {
        User user = admin ? new Admin("") : new Staff("");
        user.setFullName(Validator.requireName(fullName, "Full name"));
        user.setContactNo(Validator.requirePhone(contact));
        user.setEmail(Validator.optionalEmail(email));
        user.setUsername(Validator.requireUsername(username));
        if (db.users.findByUsername(user.getUsername()) != null) {
            throw new ValidationException("The username \"" + user.getUsername() + "\" is already taken.");
        }
        user.setDateCreated(Format.now());
        return user;
    }

    public void updateDetails(User user, String fullName, String contact, String email) throws ValidationException {
        requireAdmin();
        user.setFullName(Validator.requireName(fullName, "Full name"));
        user.setContactNo(Validator.requirePhone(contact));
        user.setEmail(Validator.optionalEmail(email));
        db.users.update(user);
        db.log("Account Edited", user.getUsername());
    }

    public void setStatus(User target, String newStatus) throws ValidationException {
        requireAdmin();
        User me = Session.getUser();
        if (me.getId().equals(target.getId()) && !User.ACTIVE.equals(newStatus)) {
            throw new ValidationException("You cannot deactivate or block your own account.");
        }
        target.setStatus(newStatus);
        if (User.ACTIVE.equals(newStatus)) {
            target.setFailedAttempts(0);
        }
        db.users.update(target);
        db.log("Account " + newStatus, target.getUsername());
    }

    public String resetPassword(User target) throws ValidationException {
        requireAdmin();
        String password = PasswordUtil.generate();
        target.setPasswordHash(PasswordUtil.hash(target.getUsername(), password));
        target.setMustChangePassword(true);
        target.setFailedAttempts(0);
        db.users.update(target);
        db.log("Password Reset", target.getUsername());
        return password;
    }

    public void deleteAccount(User target) throws ValidationException {
        requireAdmin();
        if (Session.getUser().getId().equals(target.getId())) {
            throw new ValidationException("You cannot delete your own account.");
        }
        if (target.canManageStaff()) {
            int admins = 0;
            for (User u : db.users.getAll()) {
                if (u.canManageStaff()) {
                    admins++;
                }
            }
            if (admins <= 1) {
                throw new ValidationException("The last admin account cannot be deleted.");
            }
        }
        db.users.delete(target.getId());
        db.log("Account Deleted", target.getUsername());
    }

    public void changePassword(User user, String oldPassword, String newPassword, String confirm)
            throws ValidationException {
        if (!PasswordUtil.matches(user.getUsername(), oldPassword, user.getPasswordHash())) {
            throw new ValidationException("Your current password is incorrect.");
        }
        Validator.checkPasswordStrength(newPassword);
        if (!newPassword.equals(confirm)) {
            throw new ValidationException("New password and confirmation do not match.");
        }
        if (newPassword.equals(oldPassword)) {
            throw new ValidationException("The new password must be different from the current one.");
        }
        user.setPasswordHash(PasswordUtil.hash(user.getUsername(), newPassword));
        user.setMustChangePassword(false);
        db.users.update(user);
        db.log("Password Changed", user.getUsername());
    }

    public boolean confirmAdminPassword(String password) {
        User me = Session.getUser();
        return me != null && me.canManageStaff()
                && PasswordUtil.matches(me.getUsername(), password, me.getPasswordHash());
    }

    private void requireAdmin() throws ValidationException {
        User me = Session.getUser();
        if (me == null || !me.canManageStaff()) {
            throw new ValidationException("Only the admin is allowed to do this.");
        }
    }
}
