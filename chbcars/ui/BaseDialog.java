package chbcars.ui;

import chbcars.util.ErrorHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Template for every form pop-up (ABSTRACTION). A subclass only builds its form
 * and says what happens on submit. Errors are handled once, here.
 */
public abstract class BaseDialog extends JDialog {

    private final String title;
    private final String subtitle;
    private final String okText;
    private boolean saved;

    protected BaseDialog(Window owner, String title, String subtitle, String okText) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.title = title;
        this.subtitle = subtitle;
        this.okText = okText;
    }

    /** The input fields of the dialog. */
    protected abstract JComponent buildForm();

    /** Validates and saves. Throw an exception to keep the dialog open. */
    protected abstract void onSubmit() throws Exception;

    /** Subclasses call this at the end of their constructor. */
    protected final void init() {
        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setBackground(Theme.BLACK);
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.MatteBorder(0, 0, 4, 0, Theme.RED), new EmptyBorder(14, 22, 14, 22)));
        javax.swing.JLabel t = new javax.swing.JLabel(title);
        t.setFont(Theme.HEADING);
        t.setForeground(Color.WHITE);
        javax.swing.JLabel s = new javax.swing.JLabel(subtitle);
        s.setFont(Theme.SMALL);
        s.setForeground(new Color(0xBE, 0xBE, 0xC4));
        header.add(t, BorderLayout.NORTH);
        header.add(s, BorderLayout.CENTER);

        JComponent form = buildForm();
        form.setBorder(new EmptyBorder(14, 18, 10, 18));
        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);

        JButton cancel = Theme.neutral("Cancel");
        cancel.setToolTipText("Close without saving (Esc)");
        cancel.addActionListener(e -> dispose());
        JButton ok = Theme.primary(okText);
        ok.setToolTipText("Save and close");
        ok.addActionListener(e -> submit());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(0xF4, 0xF4, 0xF5));
        footer.add(cancel);
        footer.add(ok);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(scroll, BorderLayout.CENTER);
        getContentPane().add(footer, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);

        pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(getWidth(), screen.width - 80), Math.min(getHeight(), screen.height - 100));
        setLocationRelativeTo(getOwner());
    }

    private void submit() {
        if (ErrorHandler.execute(this, this::onSubmit)) {
            saved = true;
            dispose();
        }
    }

    /** Shows the dialog and waits. Returns true when the user saved. */
    public boolean showDialog() {
        setVisible(true);
        return saved;
    }
}
