package chbcars.ui;

import chbcars.data.DataStore;
import chbcars.data.Session;
import chbcars.model.Admin;
import chbcars.model.User;
import chbcars.util.ErrorHandler;
import chbcars.util.Format;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * ABSTRACTION + INHERITANCE: the shared window for both roles (sidebar, header, page switching,
 * shortcuts, logout). AdminDashboard and StaffDashboard only decide which pages exist.
 */
public abstract class BaseDashboard extends JFrame {

    protected final User user;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    private final JLabel pageTitle = Theme.muted(" ");
    private LogoPanel logo;
    private final Map<String, BasePanel> pages = new HashMap<>();
    private final Map<String, NavButton> buttons = new HashMap<>();
    private String firstPage;
    private String currentPage;

    protected BaseDashboard(User user) {
        this.user = user;
        setTitle(Theme.APP_NAME + "  |  " + user.getRole() + " Portal");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1320, 780);
        setMinimumSize(new Dimension(1120, 660));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (Dialogs.confirm(BaseDashboard.this, "Exit the system?")) {
                    DataStore.get().log("Logout", "Closed the program");
                    System.exit(0);
                }
            }
        });

        getContentPane().setLayout(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        top.add(buildTopBar(), BorderLayout.NORTH);
        top.add(buildNavStrip(), BorderLayout.CENTER);
        getContentPane().add(top, BorderLayout.NORTH);
        content.setBackground(Theme.BG);
        getContentPane().add(content, BorderLayout.CENTER);
        getContentPane().add(buildFooter(), BorderLayout.SOUTH);

    }

    /** Subclasses add their pages here with addPage(...). */
    protected abstract void buildPages();

    protected void addPage(String key, String title, String tooltip, BasePanel panel) {
        pages.put(key, panel);
        content.add(panel, key);
        NavButton button = new NavButton(title);
        button.setToolTipText(tooltip);
        button.addActionListener(e -> showPage(key));
        buttons.put(key, button);
        nav.add(button);
        if (firstPage == null) {
            firstPage = key;
        }
    }

    protected void showFirstPage() {
        showPage(firstPage);
    }

    public void showPage(String key) {
        BasePanel panel = pages.get(key);
        if (panel == null) {
            return;
        }
        ErrorHandler.execute(this, panel::refresh);
        cards.show(content, key);
        currentPage = key;
        for (Map.Entry<String, NavButton> e : buttons.entrySet()) {
            e.getValue().markCurrent(e.getKey().equals(key));
        }
        pageTitle.setText("You are here:  " + buttons.get(key).getText().trim());
    }

    /** Black bar on top: the logo on the left, who is logged in and the Settings button on the right. */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BLACK);
        bar.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 3, 0, Theme.RED),
                new EmptyBorder(10, 22, 10, 18)));

        logo = new LogoPanel(40, Color.WHITE);
        bar.add(logo, BorderLayout.WEST);

        JLabel who = new JLabel(user.getFullName());
        who.setFont(Theme.BOLD);
        who.setForeground(Color.WHITE);
        JLabel role = new JLabel(" " + user.getRole().toUpperCase() + " ");
        role.setFont(Theme.SMALL.deriveFont(Font.BOLD));
        role.setForeground(Color.WHITE);
        role.setOpaque(true);
        role.setBackground(Theme.RED);
        role.setBorder(new EmptyBorder(2, 6, 2, 6));

        JButton settings = Theme.button("Settings \u25BE", Theme.CHARCOAL);
        settings.setToolTipText("Change password, customize the logo, switch account or log out");
        settings.addActionListener(e -> buildSettingsMenu().show(settings,
                settings.getWidth() - 230, settings.getHeight() + 4));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        actions.setOpaque(false);
        actions.add(who);
        actions.add(role);
        actions.add(settings);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    /** White strip under the black bar with one tab per page (the current tab has a red underline). */
    private JPanel buildNavStrip() {
        JPanel strip = new JPanel(new BorderLayout());
        strip.setBackground(Color.WHITE);
        strip.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(0, 18, 0, 22)));
        nav.setOpaque(false);
        strip.add(nav, BorderLayout.CENTER);
        pageTitle.setToolTipText("The page you are on");
        strip.add(pageTitle, BorderLayout.EAST);
        return strip;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.BLACK);
        footer.setBorder(new EmptyBorder(6, 22, 6, 22));
        JLabel left = new JLabel(user.getRole() + " Portal  \u2022  Logged in as " + user.getUsername());
        left.setFont(Theme.SMALL);
        left.setForeground(new Color(0xB4, 0xB4, 0xBA));
        JLabel right = new JLabel("\u00A9 " + Format.currentYear() + " " + Theme.APP_NAME);
        right.setFont(Theme.SMALL);
        right.setForeground(new Color(0xB4, 0xB4, 0xBA));
        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    /** The drop-down shown by the Settings button. Only the admin sees "Customize Logo". */
    private JPopupMenu buildSettingsMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(menuItem("Change Password", () -> new ChangePasswordDialog(this, user, false).showDialog()));
        if (user instanceof Admin) {
            menu.add(menuItem("Customize Logo", () -> {
                if (new LogoDialog(this).showDialog()) {
                    logo.reload();
                    repaint();
                }
            }));
        }
        menu.add(new JSeparator());
        menu.add(menuItem("Log In as Another User", this::switchAccount));
        menu.add(menuItem("Log Out", this::logout));
        return menu;
    }

    private JMenuItem menuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(Theme.FONT);
        item.setBorder(new EmptyBorder(9, 18, 9, 40));
        item.addActionListener(e -> action.run());
        return item;
    }

    private void logout() {
        if (Dialogs.confirm(this, "Do you want to log out?")) {
            DataStore.get().log("Logout", user.getRole() + " logged out");
            Session.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    /** Ends this session and opens the login screen so another account can log in. */
    private void switchAccount() {
        if (Dialogs.confirm(this, "Log out of this account and log in with a different one?")) {
            DataStore.get().log("Switch Account", user.getRole() + " switched account");
            Session.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    /** Tab button that highlights the page you are on. */
    private static class NavButton extends JButton {

        private boolean current;

        NavButton(String text) {
            super(text);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(Theme.BOLD);
            setForeground(new Color(0x55, 0x55, 0x5B));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(15, 16, 15, 16));
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        }

        void markCurrent(boolean selected) {
            current = selected;
            setForeground(selected ? Theme.BLACK : new Color(0x55, 0x55, 0x5B));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (current) {
                g2.setColor(Theme.SOFT_RED);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.RED);
                g2.fillRect(0, getHeight() - 4, getWidth(), 4);
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(0xF1, 0xF1, 0xF3));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
