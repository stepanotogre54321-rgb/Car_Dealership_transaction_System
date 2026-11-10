package chbcars.ui;

import chbcars.model.User;
import chbcars.service.AuthService;
import java.awt.Window;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/** Lets the logged-in user change his/her own password. */
public class ChangePasswordDialog extends BaseDialog {

    private final User user;
    private final AuthService auth = new AuthService();
    private final JPasswordField current = Theme.passwordField(20);
    private final JPasswordField newPass = Theme.passwordField(20);
    private final JPasswordField confirm = Theme.passwordField(20);

    public ChangePasswordDialog(Window owner, User user, boolean forced) {
        super(owner, "Change Password",
                forced ? "You are using a temporary password. Please set your own password." : "Choose a new password for your account.",
                "Update Password");
        this.user = user;
        current.setToolTipText("Your current password");
        newPass.setToolTipText("At least 8 characters with letters and numbers");
        init();
    }

    @Override
    protected JComponent buildForm() {
        JPanel form = Theme.formPanel();
        Theme.addWide(form, 0, "Current password", current);
        Theme.addWide(form, 1, "New password", newPass);
        Theme.addWide(form, 2, "Confirm password", confirm);
        Theme.addWide(form, 3, "", Theme.muted("Use at least 8 characters with both letters and numbers."));
        return form;
    }

    @Override
    protected void onSubmit() throws Exception {
        auth.changePassword(user, new String(current.getPassword()), new String(newPass.getPassword()),
                new String(confirm.getPassword()));
        Dialogs.success(this, "Your password was changed successfully.");
    }
}
