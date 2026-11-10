package chbcars.ui;

import chbcars.service.AuthService;
import chbcars.util.Validator;
import java.awt.Window;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Staff sign-up form. The account stays "Pending" until the admin approves it. */
public class SignUpDialog extends BaseDialog {

    private final AuthService auth = new AuthService();
    private final JTextField fullName = Theme.textField(22);
    private final JTextField contact = Theme.textField(22);
    private final JTextField email = Theme.textField(22);
    private final JTextField username = Theme.textField(22);
    private final JPasswordField password = Theme.passwordField(22);
    private final JPasswordField confirm = Theme.passwordField(22);
    private final JLabel strength = Theme.muted("Strength: -");
    private final JCheckBox show = new JCheckBox("Show passwords");

    public SignUpDialog(Window owner) {
        super(owner, "Staff Sign Up", "Your account will be activated after the admin approves it.", "Submit Request");
        fullName.setToolTipText("Your complete name");
        contact.setToolTipText("e.g. 09171234567");
        username.setToolTipText("4-20 letters, numbers, dot or underscore");
        password.setToolTipText("At least 8 characters with letters and numbers");
        password.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                String text = new String(password.getPassword());
                strength.setText("Strength: " + (text.isEmpty() ? "-" : Validator.passwordStrength(text)));
            }
        });
        show.setOpaque(false);
        show.addActionListener(e -> {
            char echo = show.isSelected() ? (char) 0 : '\u2022';
            password.setEchoChar(echo);
            confirm.setEchoChar(echo);
        });
        init();
    }

    @Override
    protected JComponent buildForm() {
        JPanel form = Theme.formPanel();
        Theme.addWide(form, 0, "Full name *", fullName);
        Theme.addWide(form, 1, "Contact no. *", contact);
        Theme.addWide(form, 2, "Email", email);
        Theme.addWide(form, 3, "Username *", username);
        Theme.addWide(form, 4, "Password *", password);
        Theme.addWide(form, 5, "", strength);
        Theme.addWide(form, 6, "Confirm password *", confirm);
        Theme.addWide(form, 7, "", show);
        return form;
    }

    @Override
    protected void onSubmit() throws Exception {
        auth.signUp(fullName.getText(), contact.getText(), email.getText(), username.getText(),
                new String(password.getPassword()), new String(confirm.getPassword()));
        Dialogs.success(this, "Request submitted!\nYou can log in once the admin approves your account.");
    }
}
