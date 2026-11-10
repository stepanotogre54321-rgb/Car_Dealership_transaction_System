package chbcars.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

/** One place for colors, fonts and ready-made styled components so every screen looks consistent. */
public final class Theme {

    public static final String APP_NAME = "chb cars.ph";

    // Brand palette: black, red and white (plus soft grays for borders and muted text)
    public static final Color BLACK = new Color(0x0D, 0x0D, 0x0D);
    public static final Color CHARCOAL = new Color(0x26, 0x26, 0x26);
    public static final Color RED = new Color(0xE1, 0x1D, 0x2E);
    public static final Color DARK_RED = new Color(0xA8, 0x12, 0x20);
    public static final Color BG = new Color(0xF4, 0xF4, 0xF5);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x14, 0x14, 0x14);
    public static final Color MUTED = new Color(0x70, 0x70, 0x75);
    public static final Color BORDER = new Color(0xDE, 0xDE, 0xE0);
    public static final Color SOFT_RED = new Color(0xFD, 0xE7, 0xE9);
    // Status colors stay inside the palette: black = good, gray = waiting, red = problem
    public static final Color SUCCESS = new Color(0x1A, 0x1A, 0x1A);
    public static final Color WARNING = new Color(0x7A, 0x7A, 0x80);
    public static final Color INFO = new Color(0x3C, 0x3C, 0x41);
    public static final Color GRAY = new Color(0x6E, 0x6E, 0x73);

    public static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    private Theme() {
    }

    /** Applies the look and feel and default fonts. Call once at startup. */
    public static void install() {
        try {
            MetalLookAndFeel.setCurrentTheme(new BrandMetalTheme());
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // the default look and feel is fine as a fallback
        }
        for (String key : new String[]{"Label.font", "Button.font", "TextField.font", "PasswordField.font",
            "ComboBox.font", "TextArea.font", "CheckBox.font", "Table.font", "OptionPane.messageFont",
            "OptionPane.buttonFont", "ToolTip.font", "List.font", "FormattedTextField.font"}) {
            UIManager.put(key, FONT);
        }
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("ToolTip.background", BLACK);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", new EmptyBorder(5, 8, 5, 8));
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("PopupMenu.border", new LineBorder(BORDER));
        UIManager.put("MenuItem.selectionBackground", SOFT_RED);
        UIManager.put("MenuItem.selectionForeground", BLACK);
        UIManager.put("MenuItem.acceleratorForeground", MUTED);
        UIManager.put("MenuItem.acceleratorSelectionForeground", MUTED);
        UIManager.put("ComboBox.selectionBackground", SOFT_RED);
        UIManager.put("ComboBox.selectionForeground", BLACK);
        UIManager.put("TextField.selectionBackground", SOFT_RED);
        UIManager.put("PasswordField.selectionBackground", SOFT_RED);
        UIManager.put("TextArea.selectionBackground", SOFT_RED);
        UIManager.put("TextField.selectionForeground", BLACK);
        UIManager.put("PasswordField.selectionForeground", BLACK);
        UIManager.put("TextArea.selectionForeground", BLACK);
        UIManager.put("CheckBox.focus", RED);
        UIManager.put("Button.focus", RED);
    }

    /** Replaces the default blue-purple Metal colors with the black / red / white brand colors. */
    private static class BrandMetalTheme extends DefaultMetalTheme {

        @Override
        public String getName() {
            return "chb cars.ph";
        }

        @Override
        protected ColorUIResource getPrimary1() {
            return new ColorUIResource(CHARCOAL);
        }

        @Override
        protected ColorUIResource getPrimary2() {
            return new ColorUIResource(RED);
        }

        @Override
        protected ColorUIResource getPrimary3() {
            return new ColorUIResource(SOFT_RED);
        }

        @Override
        protected ColorUIResource getSecondary1() {
            return new ColorUIResource(0xB5B5B8);
        }

        @Override
        protected ColorUIResource getSecondary2() {
            return new ColorUIResource(0xD2D2D5);
        }

        @Override
        protected ColorUIResource getSecondary3() {
            return new ColorUIResource(0xF4F4F5);
        }
    }

    // ------------------------------------------------------------- components
    /** Rounded button that paints itself, so it looks the same on every computer. */
    public static class StyledButton extends JButton {

        private final Color base;
        private boolean hover;

        StyledButton(String text, Color base) {
            super(text);
            this.base = base;
            setFont(BOLD);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(9, 18, 9, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = base;
            if (!isEnabled()) {
                fill = new Color(0xCF, 0xCF, 0xD3);
            } else if (getModel().isPressed()) {
                fill = base.darker();
            } else if (hover) {
                fill = base.brighter();
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JButton button(String text, Color color) {
        return new StyledButton(text, color);
    }

    public static JButton primary(String text) {
        return new StyledButton(text, RED);
    }

    public static JButton secondary(String text) {
        return new StyledButton(text, CHARCOAL);
    }

    public static JButton neutral(String text) {
        return new StyledButton(text, GRAY);
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SMALL);
        l.setForeground(MUTED);
        return l;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(TITLE);
        l.setForeground(BLACK);
        return l;
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(HEADING);
        l.setForeground(BLACK);
        return l;
    }

    public static JTextField textField(int columns) {
        JTextField f = new JTextField(columns);
        styleField(f);
        return f;
    }

    public static JPasswordField passwordField(int columns) {
        JPasswordField f = new JPasswordField(columns);
        styleField(f);
        return f;
    }

    private static void styleField(JTextField f) {
        f.setFont(FONT);
        f.setForeground(TEXT);
        f.setBorder(fieldBorder(BORDER));
        f.setCaretColor(RED);
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(fieldBorder(RED));
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(fieldBorder(BORDER));
            }
        });
    }

    private static javax.swing.border.Border fieldBorder(Color line) {
        return BorderFactory.createCompoundBorder(new LineBorder(line, 1, true), new EmptyBorder(6, 9, 6, 9));
    }

    public static JComboBox<String> combo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(FONT);
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(Math.max(150, c.getPreferredSize().width + 10), 34));
        return c;
    }

    public static JTextArea textArea(int rows, int columns) {
        JTextArea a = new JTextArea(rows, columns);
        a.setFont(FONT);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(6, 8, 6, 8));
        return a;
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(14, 16, 14, 16)));
        return p;
    }

    /** Adds "label + field" to a GridBagLayout form. col is 0 or 1 (two columns of fields). */
    public static void addField(JPanel form, int row, int col, String label, JComponent field) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = col * 2;
        lc.gridy = row;
        lc.anchor = GridBagConstraints.NORTHWEST;
        lc.insets = new Insets(8, 4, 4, 10);
        JLabel l = label(label);
        l.setFont(BOLD);
        form.add(l, lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = col * 2 + 1;
        fc.gridy = row;
        fc.weightx = 1;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.insets = new Insets(4, 0, 4, 16);
        form.add(field, fc);
    }

    /** A field that spans the whole width of a two-column form. */
    public static void addWide(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0;
        lc.gridy = row;
        lc.anchor = GridBagConstraints.NORTHWEST;
        lc.insets = new Insets(8, 4, 4, 10);
        JLabel l = label(label);
        l.setFont(BOLD);
        form.add(l, lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1;
        fc.gridy = row;
        fc.gridwidth = 3;
        fc.weightx = 1;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.insets = new Insets(4, 0, 4, 16);
        form.add(field, fc);
    }

    public static JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        return p;
    }

    // ----------------------------------------------------------------- tables
    public static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JTable table(TableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(32);
        t.setFont(FONT);
        t.setForeground(TEXT);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xEC, 0xEC, 0xEE));
        t.setSelectionBackground(SOFT_RED);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setDefaultRenderer(Object.class, new CellRenderer());

        JTableHeader header = t.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                    boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, sel, focus, row, col);
                l.setOpaque(true);
                l.setBackground(BLACK);
                l.setForeground(Color.WHITE);
                l.setFont(BOLD);
                l.setBorder(BorderFactory.createCompoundBorder(new javax.swing.border.MatteBorder(0, 0, 3, 0, RED),
                        new EmptyBorder(0, 10, 0, 10)));
                return l;
            }
        });
        return t;
    }

    /** Sets the preferred width of each column (so prices and dates are never cut off). */
    public static void widths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /** Converts the selected table row to the row number in the model (-1 when nothing is selected). */
    public static int selectedModelRow(JTable table) {
        int row = table.getSelectedRow();
        return row < 0 ? -1 : table.convertRowIndexToModel(row);
    }

    /** Color used to show a status word (Paid, Pending, Cancelled...). Null when it is not a status. */
    public static Color statusColor(String text) {
        switch (text) {
            case "Available": case "Paid": case "Completed": case "Active": case "Ready for Release":
                return SUCCESS;
            case "Reserved": case "Pending": case "Under Review": case "Awaiting Payment": case "Partially Paid":
            case "Pending Verification": case "Pending Approval": case "On Hold":
                return WARNING;
            case "Unavailable": case "Cancelled": case "Failed": case "Blocked": case "Deactivated": case "Unpaid":
                return RED;
            case "Released": case "Refunded":
                return INFO;
            default:
                return null;
        }
    }

    /** Zebra rows, colored status words and picture cells. */
    private static class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int col) {
            boolean picture = value instanceof Icon;
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, picture ? "" : value, selected, false, row, col);
            l.setIcon(picture ? (Icon) value : null);
            l.setHorizontalAlignment(picture ? SwingConstants.CENTER : SwingConstants.LEFT);
            l.setBorder(new EmptyBorder(0, 10, 0, 10));
            if (!selected) {
                l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFA, 0xFA, 0xFB));
            }
            Color status = picture || value == null ? null : statusColor(value.toString());
            l.setForeground(status != null ? status : TEXT);
            l.setFont(status != null ? BOLD : FONT);
            return l;
        }
    }
}
