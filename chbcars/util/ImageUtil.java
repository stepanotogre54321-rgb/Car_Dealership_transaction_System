package chbcars.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/** Image uploading, scaling and the vector car graphic used across the system. */
public final class ImageUtil {

    private static final Map<String, ImageIcon> CACHE = new HashMap<>();

    private ImageUtil() {
    }

    /** Copies an uploaded car picture into data/images and returns its stored path. */
    public static String importImage(File source) throws ValidationException, IOException {
        return importImage(source, "car");
    }

    /** Same as above, but the stored file name starts with the given prefix (car, logo...). */
    public static String importImage(File source, String prefix) throws ValidationException, IOException {
        if (source == null || !source.isFile()) {
            throw new ValidationException("Please choose a valid image file.");
        }
        String name = source.getName().toLowerCase();
        if (!(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
                || name.endsWith(".gif") || name.endsWith(".bmp"))) {
            throw new ValidationException("Only JPG, PNG, GIF or BMP images are allowed.");
        }
        if (source.length() > 5L * 1024 * 1024) {
            throw new ValidationException("The image must be 5 MB or smaller.");
        }
        if (ImageIO.read(source) == null) {
            throw new ValidationException("The selected file is not a readable image.");
        }
        new File(FileHandler.IMAGE_DIR).mkdirs();
        String extension = name.substring(name.lastIndexOf('.'));
        File target = new File(FileHandler.IMAGE_DIR, prefix + "_" + System.currentTimeMillis() + extension);
        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return target.getPath().replace('\\', '/');
    }

    /** Loads a picture scaled to fit w x h (shows a placeholder when missing). */
    public static ImageIcon load(String path, int w, int h) {
        String key = path + "|" + w + "x" + h;
        ImageIcon cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        BufferedImage canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(new Color(0xEE, 0xEE, 0xF0));
        g.fillRect(0, 0, w, h);

        BufferedImage picture = null;
        if (path != null && !path.isEmpty()) {
            try {
                picture = ImageIO.read(new File(path));
            } catch (IOException e) {
                picture = null;
            }
        }
        if (picture == null) {
            drawPlaceholder(g, w, h);
        } else {
            double scale = Math.min((double) w / picture.getWidth(), (double) h / picture.getHeight());
            int pw = (int) (picture.getWidth() * scale);
            int ph = (int) (picture.getHeight() * scale);
            g.drawImage(picture, (w - pw) / 2, (h - ph) / 2, pw, ph, null);
        }
        g.dispose();
        ImageIcon icon = new ImageIcon(canvas);
        CACHE.put(key, icon);
        return icon;
    }

    private static void drawPlaceholder(Graphics2D g, int w, int h) {
        int size = Math.min(w, h) * 6 / 10;
        drawCar(g, (w - size * 2) / 2, (h - size) / 2 - 6, size * 2, size, new Color(0xBDBDC2), new Color(0xEE, 0xEE, 0xF0));
        if (h >= 40) {
            g.setColor(new Color(0x8C8C92));
            g.setFont(new Font("SansSerif", Font.PLAIN, Math.max(9, Math.min(13, h / 8))));
            FontMetrics fm = g.getFontMetrics();
            String text = "No Image";
            g.drawString(text, (w - fm.stringWidth(text)) / 2, h - 6);
        }
    }

    /** Draws a simple side-view car inside the given box (used by the logo and placeholders). */
    public static void drawCar(Graphics2D g, int x, int y, int w, int h, Color body, Color detail) {
        double s = Math.min(w / 100.0, h / 50.0);
        AffineTransform old = g.getTransform();
        g.translate(x + (w - 100 * s) / 2, y + (h - 50 * s) / 2);
        g.scale(s, s);

        Path2D shape = new Path2D.Double();
        shape.moveTo(4, 36);
        shape.lineTo(4, 27);
        shape.lineTo(16, 23);
        shape.lineTo(30, 9);
        shape.lineTo(66, 9);
        shape.lineTo(80, 22);
        shape.lineTo(94, 26);
        shape.lineTo(96, 36);
        shape.closePath();
        g.setColor(body);
        g.fill(shape);

        g.setColor(detail);
        Path2D windowA = new Path2D.Double();
        windowA.moveTo(32, 12);
        windowA.lineTo(46, 12);
        windowA.lineTo(46, 22);
        windowA.lineTo(23, 22);
        windowA.closePath();
        g.fill(windowA);
        Path2D windowB = new Path2D.Double();
        windowB.moveTo(50, 12);
        windowB.lineTo(64, 12);
        windowB.lineTo(75, 22);
        windowB.lineTo(50, 22);
        windowB.closePath();
        g.fill(windowB);

        g.fill(new Ellipse2D.Double(14, 28, 20, 20));
        g.fill(new Ellipse2D.Double(66, 28, 20, 20));
        g.setColor(body);
        g.fill(new Ellipse2D.Double(20.5, 34.5, 7, 7));
        g.fill(new Ellipse2D.Double(72.5, 34.5, 7, 7));
        g.setTransform(old);
    }
}
