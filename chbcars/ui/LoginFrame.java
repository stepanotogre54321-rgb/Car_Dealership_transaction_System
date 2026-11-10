package chbcars.ui;

import chbcars.data.AppSettings;
import chbcars.data.DataStore;
import chbcars.data.Session;
import chbcars.model.Admin;
import chbcars.model.User;
import chbcars.service.AuthException;
import chbcars.service.AuthService;
import chbcars.util.ErrorHandler;
import chbcars.util.ImageUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {

    private final AuthService auth = new AuthService();
    private final JTextField username = Theme.textField(20);
    private final JPasswordField password = Theme.passwordField(20);
    private final JCheckBox show = new JCheckBox("Show password");

    public LoginFrame() {
        super(Theme.APP_NAME + " - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(940, 580);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new BrandPanel(), BorderLayout.EAST);
        getContentPane().add(buildForm(), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(70, 56, 40, 56));

        JLabel welcome = Theme.title("Welcome back");
        JLabel sub = Theme.muted("Log in as Admin or Staff to continue.");
        username.setToolTipText("Your username");
        password.setToolTipText("Your password (press Enter to log in)");
        show.setOpaque(false);
        show.setFont(Theme.SMALL);
        show.addActionListener(e -> password.setEchoChar(show.isSelected() ? (char) 0 : '\u2022'));

        JButton login = Theme.primary("Log In");
        login.setToolTipText("Log in to the system");
        login.addActionListener(e -> doLogin());
        JButton signUp = Theme.secondary("Staff Sign Up");
        signUp.setToolTipText("Request a staff account (needs admin approval)");
        signUp.addActionListener(e -> new SignUpDialog(this).showDialog());
        getRootPane().setDefaultButton(login);

        JPanel buttons = new JPanel(new java.awt.GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        buttons.add(login);
        buttons.add(signUp);

        JPanel accent = new JPanel();
        accent.setBackground(Theme.RED);
        accent.setPreferredSize(new Dimension(52, 4));
        panel.add(left(welcome));
        panel.add(javax.swing.Box.createVerticalStrut(6));
        panel.add(left(accent));
        panel.add(javax.swing.Box.createVerticalStrut(8));
        panel.add(left(sub));
        panel.add(javax.swing.Box.createVerticalStrut(28));
        panel.add(left(Theme.label("Username")));
        panel.add(javax.swing.Box.createVerticalStrut(5));
        panel.add(full(username));
        panel.add(javax.swing.Box.createVerticalStrut(14));
        panel.add(left(Theme.label("Password")));
        panel.add(javax.swing.Box.createVerticalStrut(5));
        panel.add(full(password));
        panel.add(javax.swing.Box.createVerticalStrut(6));
        panel.add(left(show));
        panel.add(javax.swing.Box.createVerticalStrut(22));
        panel.add(full(buttons));
        panel.add(javax.swing.Box.createVerticalGlue());
        panel.add(left(Theme.muted("Only 3 invalid attempts are allowed before the system closes.")));
        return panel;
    }

    private JPanel left(javax.swing.JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.WEST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 2));
        return p;
    }

    private JPanel full(javax.swing.JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 2));
        return p;
    }

    private void doLogin() {
        try {
            User user = auth.login(username.getText(), new String(password.getPassword()));
            openDashboard(user);
        } catch (AuthException e) {
            Dialogs.error(this, "Login Failed", e.getMessage());
            password.setText("");
            if (e.mustTerminate()) {
                System.exit(0);
            }
        } catch (Exception e) {
            ErrorHandler.execute(this, () -> {
                throw e;
            });
        }
    }

    private void openDashboard(User user) {
        if (user.isMustChangePassword()) {
            Dialogs.info(this, "Welcome, " + user.getFullName() + "!\nYou are using a temporary password, so you must set a new one.");
            if (!new ChangePasswordDialog(this, user, true).showDialog()) {
                Session.logout();
                password.setText("");
                return;
            }
        }
        BaseDashboard dashboard = user instanceof Admin ? new AdminDashboard(user) : new StaffDashboard(user);
        dashboard.setVisible(true);
        dispose();
    }

    private static class BrandPanel extends JPanel {

        BrandPanel() {
            setPreferredSize(new Dimension(430, 0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setPaint(new GradientPaint(0, 0, Theme.BLACK, 0, getHeight(), new Color(0x2A, 0x0A, 0x0E)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            g2.setColor(Theme.RED);
            g2.fillRect(0, 0, 6, getHeight());

            AppSettings settings = DataStore.get().settings;
            BufferedImage logo = settings.getLogoImage();
            if (logo == null) {
                ImageUtil.drawCar(g2, 40, 150, getWidth() - 80, 190, Theme.RED, Theme.BLACK);
            } else {
                double scale = Math.min((getWidth() - 100.0) / logo.getWidth(), 190.0 / logo.getHeight());
                int w = (int) (logo.getWidth() * scale);
                int h = (int) (logo.getHeight() * scale);
                g2.drawImage(logo, (getWidth() - w) / 2, 150 + (190 - h) / 2, w, h, null);
            }

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 44));
            FontMetrics fm = g2.getFontMetrics();
            int x = 44;
            g2.drawString(settings.getLogoMain(), x, 410);
            g2.setColor(Theme.RED);
            g2.drawString(settings.getLogoAccent(), x + fm.stringWidth(settings.getLogoMain()), 410);
            g2.setColor(new Color(0xD0, 0xD0, 0xD6));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            g2.drawString("Car Dealership Management System", x, 440);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            g2.drawString("Drive home your dream car.", x, 466);
            g2.dispose();
        }
    }
}
