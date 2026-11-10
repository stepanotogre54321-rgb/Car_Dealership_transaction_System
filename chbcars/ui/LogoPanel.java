package chbcars.ui;

import chbcars.data.AppSettings;
import chbcars.data.DataStore;
import chbcars.util.ImageUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * The dealership logo. By default it is drawn with Java2D (red badge + car + "chb cars.ph").
 * When the admin uploads a picture in Settings > Customize Logo, that picture is shown instead.
 * Two constructors: one that follows the saved settings, one that previews unsaved choices.
 */
public class LogoPanel extends JPanel {

    private final int height;
    private final Color textColor;
    private final Font font;
    private final boolean live;
    private BufferedImage image;
    private String main;
    private String accent;
    private boolean showText;

    /** Logo that always follows the saved settings (used in the dashboard). */
    public LogoPanel(int height, Color textColor) {
        this(height, textColor, null, null, true, true);
        reload();
    }

    /** Preview logo built from the values given (used by the Customize Logo window). */
    public LogoPanel(int height, Color textColor, BufferedImage image, String text, boolean showText) {
        this(height, textColor, image, text, showText, false);
    }

    private LogoPanel(int height, Color textColor, BufferedImage image, String text, boolean showText, boolean live) {
        this.height = height;
        this.textColor = textColor;
        this.live = live;
        this.font = new Font("Segoe UI", Font.BOLD, Math.max(14, height / 2));
        setOpaque(false);
        show(image, text, showText);
    }

    /** Reads the saved settings again (call this after the admin saves a new logo). */
    public final void reload() {
        AppSettings settings = DataStore.get().settings;
        BufferedImage picture = settings.getLogoImage();
        main = settings.getLogoMain();
        accent = settings.getLogoAccent();
        image = picture;
        showText = settings.isShowLogoText() || picture == null;
        resize();
        revalidate();
        repaint();
    }

    private void show(BufferedImage picture, String text, boolean visible) {
        image = picture;
        showText = visible || picture == null;
        String value = text == null ? AppSettings.DEFAULT_LOGO_TEXT : text;
        int dot = value.lastIndexOf('.');
        main = dot > 0 ? value.substring(0, dot) : value;
        accent = dot > 0 ? value.substring(dot) : "";
        resize();
    }

    private int imageWidth() {
        if (image == null) {
            return height;
        }
        double ratio = (double) image.getWidth() / image.getHeight();
        return (int) Math.max(height / 2.0, Math.min(height * ratio, height * 3.0));
    }

    private void resize() {
        FontMetrics fm = getFontMetrics(font);
        int textWidth = showText ? fm.stringWidth(main + accent) + 10 : 0;
        setPreferredSize(new Dimension(imageWidth() + textWidth + 4, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int y = (getHeight() - height) / 2;
        int slot = imageWidth();
        if (image == null) {
            g2.setColor(Theme.RED);
            g2.fillRoundRect(0, y, height, height, height / 3, height / 3);
            ImageUtil.drawCar(g2, height / 8, y + height / 5, height * 3 / 4, height * 3 / 5, Color.WHITE, Theme.RED);
        } else {
            double scale = Math.min((double) slot / image.getWidth(), (double) height / image.getHeight());
            int w = (int) (image.getWidth() * scale);
            int h = (int) (image.getHeight() * scale);
            g2.drawImage(image, (slot - w) / 2, y + (height - h) / 2, w, h, null);
        }
        if (showText) {
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int textY = y + (height + fm.getAscent() - fm.getDescent()) / 2;
            int x = slot + 10;
            g2.setColor(textColor);
            g2.drawString(main, x, textY);
            g2.setColor(Theme.RED);
            g2.drawString(accent, x + fm.stringWidth(main), textY);
        }
        g2.dispose();
    }

    /** True when this logo follows the saved settings. */
    public boolean isLive() {
        return live;
    }
}
