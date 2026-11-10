package chbcars.model;

import chbcars.util.FileHandler;

/** One important system action (login, sale, payment, etc.). */
public class ActivityLog extends Entity {

    private String timestamp = "";
    private String username = "";
    private String role = "";
    private String action = "";
    private String details = "";

    public ActivityLog() {
        super("");
    }

    public ActivityLog(String timestamp, String username, String role, String action, String details) {
        super("");
        this.timestamp = timestamp;
        this.username = username;
        this.role = role;
        this.action = action;
        this.details = details;
    }

    public String getTimestamp() { return timestamp; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getAction() { return action; }
    public String getDetails() { return details; }

    @Override
    public String getSummary() {
        return timestamp + " | " + username + " | " + action;
    }

    @Override
    public String toFileLine() {
        return FileHandler.join(getId(), timestamp, username, role, action, details);
    }

    public static ActivityLog fromFields(String[] f) {
        ActivityLog log = new ActivityLog(f[1], f[2], f[3], f[4], f[5]);
        log.setId(f[0]);
        return log;
    }
}
