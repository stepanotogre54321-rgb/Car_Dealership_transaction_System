package chbcars.ui;

import chbcars.util.ErrorHandler;
import chbcars.util.FileHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/** Shows a slip / receipt / form as text, with Save and Close buttons. */
public class SlipDialog extends JDialog {

    public SlipDialog(Window owner, String title, String text, String fileName) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        JTextArea area = new JTextArea(text);
        area.setFont(Theme.MONO);
        area.setEditable(false);
        area.setBorder(new EmptyBorder(14, 18, 14, 18));
        area.setCaretPosition(0);

        JButton save = Theme.secondary("Save as .txt");
        save.setToolTipText("Save a copy in data/documents");
        save.addActionListener(e -> ErrorHandler.execute(this, () -> {
            String path = FileHandler.DOC_DIR + "/" + fileName + ".txt";
            FileHandler.writeText(path, text);
            Dialogs.success(this, "Saved to " + path);
        }));
        JButton close = Theme.primary("Close");
        close.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(0xF4, 0xF4, 0xF5));
        footer.add(save);
        footer.add(close);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BLACK);
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.MatteBorder(0, 0, 4, 0, Theme.RED), new EmptyBorder(12, 20, 12, 20)));
        header.add(new LogoPanel(30, Color.WHITE), BorderLayout.WEST);
        javax.swing.JLabel docTitle = new javax.swing.JLabel(title);
        docTitle.setFont(Theme.BOLD);
        docTitle.setForeground(Color.WHITE);
        header.add(docTitle, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(Theme.scroll(area), BorderLayout.CENTER);
        getContentPane().add(footer, BorderLayout.SOUTH);
        setSize(640, 640);
        setLocationRelativeTo(owner);
    }
}
