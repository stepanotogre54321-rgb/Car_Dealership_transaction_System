package chbcars.ui;

import chbcars.data.DataStore;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * ABSTRACTION + INHERITANCE: every page of the dashboard extends BasePanel.
 * Each page decides for itself how to refresh() its data.
 */
public abstract class BasePanel extends JPanel {

    protected final DataStore db = DataStore.get();

    protected BasePanel() {
        super(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));
    }

    /** Reloads the data shown on this page. */
    public abstract void refresh();

    /** Page title + short subtitle + a row of tool buttons. */
    protected JPanel top(String title, String subtitle, JComponent... tools) {
        JPanel box = new JPanel(new BorderLayout(0, 10));
        box.setOpaque(false);
        JPanel heading = new JPanel(new BorderLayout(0, 2));
        heading.setOpaque(false);
        JPanel titleBox = new JPanel(new BorderLayout(0, 6));
        titleBox.setOpaque(false);
        titleBox.add(Theme.title(title), BorderLayout.NORTH);
        JPanel accent = new JPanel();
        accent.setBackground(Theme.RED);
        accent.setPreferredSize(new java.awt.Dimension(46, 4));
        JPanel accentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        accentRow.setOpaque(false);
        accentRow.add(accent);
        titleBox.add(accentRow, BorderLayout.CENTER);
        heading.add(titleBox, BorderLayout.NORTH);
        heading.add(Theme.muted(subtitle), BorderLayout.CENTER);
        box.add(heading, BorderLayout.NORTH);
        if (tools.length > 0) {
            JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bar.setOpaque(false);
            for (JComponent tool : tools) {
                bar.add(tool);
            }
            box.add(bar, BorderLayout.CENTER);
        }
        return box;
    }

    /** Runs the action every time the text in the field changes (live search). */
    protected void liveSearch(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    protected Window owner() {
        return SwingUtilities.getWindowAncestor(this);
    }
}
