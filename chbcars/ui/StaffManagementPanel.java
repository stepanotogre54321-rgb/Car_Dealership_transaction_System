package chbcars.ui;

import chbcars.data.Session;
import chbcars.model.User;
import chbcars.service.AuthService;
import chbcars.util.ErrorHandler;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Admin: create accounts (auto-generated password), approve sign-ups, block/unblock, reset and delete. */
public class StaffManagementPanel extends BasePanel {

    private final AuthService auth = new AuthService();
    private final DefaultTableModel model = Theme.readOnlyModel("User ID", "Full Name", "Username", "Access Level",
            "Contact", "Email", "Status", "Failed Tries", "Created");
    private final JTable table = Theme.table(model);
    private final JTextField search = Theme.textField(16);
    private final List<User> shown = new ArrayList<>();

    public StaffManagementPanel() {
        Theme.widths(table, 80,160,100,100,110,170,120,90,130);
        search.setToolTipText("Search by name or username");
        JButton create = Theme.primary("+ Create Account");
        create.setToolTipText("Create a staff or admin account with an auto-generated password");
        JButton edit = Theme.secondary("Edit");
        JButton approve = Theme.button("Approve", Theme.SUCCESS);
        approve.setToolTipText("Approve a pending sign-up request");
        JButton toggle = Theme.button("Activate / Deactivate", Theme.WARNING);
        JButton unblock = Theme.secondary("Unblock");
        unblock.setToolTipText("Unblock an account locked after 3 invalid logins");
        JButton reset = Theme.secondary("Reset Password");
        reset.setToolTipText("Generate a new temporary password (202600___)");
        JButton delete = Theme.button("Delete", Theme.GRAY);
        delete.setToolTipText("Delete the account (admin password required)");

        add(top("Staff Accounts", "Manage who can use the system and what they can access.",
                Theme.label("Search:"), search, create, edit, approve, toggle, unblock, reset, delete),
                BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);

        liveSearch(search, this::refresh);
        create.addActionListener(e -> {
            if (new StaffFormDialog(owner(), null).showDialog()) {
                refresh();
            }
        });
        edit.addActionListener(e -> {
            User u = selected();
            if (u != null && new StaffFormDialog(owner(), u).showDialog()) {
                refresh();
            }
        });
        approve.addActionListener(e -> changeStatus(User.PENDING, User.ACTIVE, "Only pending sign-up requests can be approved."));
        unblock.addActionListener(e -> changeStatus(User.BLOCKED, User.ACTIVE, "This account is not blocked."));
        toggle.addActionListener(e -> toggleSelected());
        reset.addActionListener(e -> resetSelected());
        delete.addActionListener(e -> deleteSelected());
    }

    @Override
    public void refresh() {
        shown.clear();
        model.setRowCount(0);
        String key = search.getText().trim().toLowerCase();
        for (User u : db.users.getAll()) {
            if (!key.isEmpty() && !(u.getFullName() + " " + u.getUsername()).toLowerCase().contains(key)) {
                continue;
            }
            shown.add(u);
            model.addRow(new Object[]{u.getId(), u.getFullName(), u.getUsername(), u.getRole(), u.getContactNo(),
                u.getEmail(), u.getStatus(), u.getFailedAttempts(), u.getDateCreated()});
        }
    }

    private User selected() {
        int row = Theme.selectedModelRow(table);
        if (row < 0) {
            Dialogs.warn(this, "Please select an account in the table first.");
            return null;
        }
        return db.users.findById(shown.get(row).getId());
    }

    private void changeStatus(String requiredCurrent, String newStatus, String errorMessage) {
        User u = selected();
        if (u == null) {
            return;
        }
        if (!requiredCurrent.equals(u.getStatus())) {
            Dialogs.warn(this, errorMessage);
            return;
        }
        ErrorHandler.execute(this, () -> auth.setStatus(u, newStatus));
        refresh();
    }

    private void toggleSelected() {
        User u = selected();
        if (u == null) {
            return;
        }
        String target = User.ACTIVE.equals(u.getStatus()) ? User.DEACTIVATED : User.ACTIVE;
        if (User.PENDING.equals(u.getStatus()) || User.BLOCKED.equals(u.getStatus())) {
            Dialogs.warn(this, "Use Approve or Unblock for this account.");
            return;
        }
        ErrorHandler.execute(this, () -> auth.setStatus(u, target));
        refresh();
    }

    private void resetSelected() {
        User u = selected();
        if (u != null && Dialogs.confirm(this, "Generate a new temporary password for " + u.getUsername() + "?")) {
            ErrorHandler.execute(this, () -> {
                String password = auth.resetPassword(u);
                Dialogs.success(this, "New temporary password for " + u.getUsername() + ":\n\n" + password
                        + "\n\nThe user must change it at the next login.");
            });
            refresh();
        }
    }

    private void deleteSelected() {
        User u = selected();
        if (u == null) {
            return;
        }
        if (Session.getUser().getId().equals(u.getId())) {
            Dialogs.warn(this, "You cannot delete your own account.");
            return;
        }
        if (!Dialogs.confirm(this, "Delete account " + u.getUsername() + "?")
                || !Dialogs.confirmAdminPassword(this, "delete this account")) {
            return;
        }
        ErrorHandler.execute(this, () -> auth.deleteAccount(u));
        refresh();
    }
}
