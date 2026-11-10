package chbcars.ui;

import chbcars.model.User;
import chbcars.service.AuthService;
import chbcars.util.PasswordUtil;
import java.awt.Window;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Admin creates an account (password is auto-generated) or edits permitted details. */
public class StaffFormDialog extends BaseDialog {

    private final User existing;
    private final AuthService auth = new AuthService();
    private final JComboBox<String> role = Theme.combo("Staff", "Admin");
    private final JTextField fullName = Theme.textField(24);
    private final JTextField username = Theme.textField(24);
    private final JTextField contact = Theme.textField(24);
    private final JTextField email = Theme.textField(24);

    public StaffFormDialog(Window owner, User existing) {
        super(owner, existing == null ? "Create Account" : "Edit Account",
                existing == null ? "A password will be generated automatically (" + PasswordUtil.PREFIX + "___)."
                        : "Username and role cannot be changed.",
                existing == null ? "Create Account" : "Save Changes");
        this.existing = existing;
        role.setToolTipText("Staff = limited access, Admin = full access");
        if (existing != null) {
            role.setSelectedItem(existing.getRole());
            role.setEnabled(false);
            fullName.setText(existing.getFullName());
            username.setText(existing.getUsername());
            username.setEnabled(false);
            contact.setText(existing.getContactNo());
            email.setText(existing.getEmail());
        }
        init();
    }

    @Override
    protected JComponent buildForm() {
        JPanel form = Theme.formPanel();
        Theme.addWide(form, 0, "Access level *", role);
        Theme.addWide(form, 1, "Full name *", fullName);
        Theme.addWide(form, 2, "Username *", username);
        Theme.addWide(form, 3, "Contact no. *", contact);
        Theme.addWide(form, 4, "Email", email);
        return form;
    }

    @Override
    protected void onSubmit() throws Exception {
        if (existing == null) {
            boolean admin = "Admin".equals(role.getSelectedItem());
            String password = auth.createAccount(admin, fullName.getText(), contact.getText(), email.getText(),
                    username.getText());
            Dialogs.success(this, "Account created!\n\nUsername: " + username.getText().trim()
                    + "\nTemporary password: " + password
                    + "\n\nGive these to the user. The password must be changed at the first login.");
        } else {
            auth.updateDetails(existing, fullName.getText(), contact.getText(), email.getText());
        }
    }
}
