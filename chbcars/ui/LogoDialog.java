package chbcars.ui;

import chbcars.data.AppSettings;
import chbcars.data.DataStore;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Admin only (Settings > Customize Logo): upload a logo picture, change the logo text, or go back
 * to the default logo. The preview updates right away; nothing is saved until "Save Logo".
 */
public class LogoDialog extends BaseDialog {

    private final AppSettings settings = DataStore.get().settings;
    private final JTextField text = Theme.textField(20);
    private final JCheckBox showText = new JCheckBox("Show the text beside the picture");
    private final JLabel fileName = Theme.muted("Using the current logo");
    private final JPanel previewHolder = new JPanel(new BorderLayout());
    private File chosenFile;
    private BufferedImage previewImage;
    private boolean useDefault;

    public LogoDialog(Window owner) {
        super(owner, "Customize Logo", "Upload your own logo picture and choose the text that goes with it.", "Save Logo");
        previewImage = settings.getLogoImage();
        text.setText(settings.getLogoText());
        text.setToolTipText("Example: chb cars.ph  (the part after the last dot is shown in red)");
        showText.setSelected(settings.isShowLogoText());
        showText.setOpaque(false);
        showText.setToolTipText("Untick this when your picture already contains the name");
        showText.addActionListener(e -> updatePreview());
        text.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }
        });
        init();
        updatePreview();
    }

    @Override
    protected JComponent buildForm() {
        previewHolder.setBackground(Theme.BLACK);
        previewHolder.setPreferredSize(new Dimension(420, 110));
        previewHolder.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JButton browse = Theme.secondary("Choose Picture...");
        browse.setToolTipText("PNG or JPG, up to 5 MB. A transparent PNG looks best.");
        browse.addActionListener(e -> choosePicture());
        JButton reset = Theme.neutral("Use Default Logo");
        reset.setToolTipText("Remove the custom picture and use the built-in logo");
        reset.addActionListener(e -> {
            useDefault = true;
            chosenFile = null;
            previewImage = null;
            fileName.setText("The default logo will be used");
            text.setText(AppSettings.DEFAULT_LOGO_TEXT);
            showText.setSelected(true);
            updatePreview();
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(browse);
        buttons.add(reset);

        JPanel form = Theme.formPanel();
        Theme.addWide(form, 0, "Preview", previewHolder);
        Theme.addWide(form, 1, "Picture", buttons);
        Theme.addWide(form, 2, "", fileName);
        Theme.addWide(form, 3, "Logo text", text);
        Theme.addWide(form, 4, "", showText);
        Theme.addWide(form, 5, "", Theme.muted("Tip: use a PNG with a transparent background, wider than it is tall."));
        return form;
    }

    private void choosePicture() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose your logo");
        chooser.setFileFilter(new FileNameExtensionFilter("Images (png, jpg, gif, bmp)", "png", "jpg", "jpeg", "gif", "bmp"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            BufferedImage picture = ImageIO.read(chooser.getSelectedFile());
            if (picture == null) {
                Dialogs.warn(this, "The selected file is not a readable image.");
                return;
            }
            chosenFile = chooser.getSelectedFile();
            previewImage = picture;
            useDefault = false;
            fileName.setText("Selected: " + chosenFile.getName());
            updatePreview();
        } catch (IOException e) {
            Dialogs.warn(this, "That file could not be opened: " + e.getMessage());
        }
    }

    private void updatePreview() {
        previewHolder.removeAll();
        JPanel center = new JPanel(new java.awt.GridBagLayout());
        center.setOpaque(false);
        String value = text.getText().trim().isEmpty() ? AppSettings.DEFAULT_LOGO_TEXT : text.getText().trim();
        center.add(new LogoPanel(60, Color.WHITE, previewImage, value, showText.isSelected()));
        previewHolder.add(center, BorderLayout.CENTER);
        previewHolder.revalidate();
        previewHolder.repaint();
    }

    @Override
    protected void onSubmit() throws Exception {
        settings.setLogoText(text.getText());
        settings.setShowLogoText(showText.isSelected());
        if (useDefault) {
            settings.resetLogo();
        } else if (chosenFile != null) {
            settings.setLogoFile(chosenFile);
        }
        settings.save();
        DataStore.get().log("Logo Changed", "Logo text: " + settings.getLogoText());
        Dialogs.success(this, "Your logo was saved. It now appears on every screen.");
    }
}
