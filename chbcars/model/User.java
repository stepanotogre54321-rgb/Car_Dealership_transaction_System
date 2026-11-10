package chbcars.model;

import chbcars.util.FileHandler;

/**
 * ABSTRACTION + POLYMORPHISM: a User is either an Admin or a Staff member.
 * The permission methods are abstract, so every role answers them differently.
 */
public abstract class User extends Person {

    public static final String ACTIVE = "Active";
    public static final String PENDING = "Pending Approval";
    public static final String BLOCKED = "Blocked";
    public static final String DEACTIVATED = "Deactivated";

    private String username = "";
    private String passwordHash = "";
    private String status = ACTIVE;
    private boolean mustChangePassword;
    private int failedAttempts;
    private String dateCreated = "";

    protected User(String id) {
        super(id);
    }

    public abstract String getRole();

    public abstract boolean canManageCars();

    public abstract boolean canManageStaff();

    public abstract boolean canViewReports();

    public abstract boolean canChangePrices();

    public abstract boolean canApproveOrders();

    /** Admin accounts can never be locked out; staff accounts can. */
    public abstract boolean canBeBlocked();

    @Override
    public String getPersonType() {
        return getRole();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isActive() {
        return ACTIVE.equals(status);
    }

    @Override
    public String getSummary() {
        return getFullName() + " (" + username + ", " + getRole() + ")";
    }

    @Override
    public String toFileLine() {
        return FileHandler.join(getId(), getRole(), username, passwordHash, getFullName(), getContactNo(),
                getEmail(), status, mustChangePassword, failedAttempts, dateCreated);
    }

    public static User fromFields(String[] f) {
        User u = "Admin".equals(f[1]) ? new Admin(f[0]) : new Staff(f[0]);
        u.setUsername(f[2]);
        u.setPasswordHash(f[3]);
        u.setFullName(f[4]);
        u.setContactNo(f[5]);
        u.setEmail(f[6]);
        u.setStatus(f[7]);
        u.setMustChangePassword(Boolean.parseBoolean(f[8]));
        u.setFailedAttempts(Integer.parseInt(f[9]));
        u.setDateCreated(f[10]);
        return u;
    }
}
