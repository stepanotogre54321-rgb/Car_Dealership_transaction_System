package chbcars.ui;

import chbcars.service.AuthService;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/** Small reusable message boxes. */
public final class Dialogs {

    private Dialogs() {
    }

    /** Plain information (information icon). */
    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, Theme.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    /** Something worked: saved, created, verified... (information icon). */
    public static void success(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Something needs attention but nothing broke (warning icon). */
    public static void warn(Component parent, String message) {
        warn(parent, "Notice", message);
    }

    public static void warn(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /** Something failed or was refused (error icon). */
    public static void error(Component parent, String message) {
        error(parent, "Error", message);
    }

    public static void error(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Security check before any delete: the admin must type the password again. */
    public static boolean confirmAdminPassword(Component parent, String action) {
        JPasswordField field = Theme.passwordField(18);
        field.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                SwingUtilities.invokeLater(field::requestFocusInWindow);
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("<html>Security check: enter the <b>admin password</b> to " + action + ".</html>"), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        int choice = JOptionPane.showConfirmDialog(parent, panel, "Admin Authorization",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return false;
        }
        boolean ok = new AuthService().confirmAdminPassword(new String(field.getPassword()));
        if (!ok) {
            error(parent, "Access Denied", "Incorrect admin password. The action was cancelled.");
        }
        return ok;
    }
}
