package chbcars.data;

import chbcars.util.DataException;
import chbcars.util.FileHandler;
import chbcars.util.ImageUtil;
import chbcars.util.ValidationException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Remembers the settings the admin can change (the logo picture, the logo text and whether the
 * text is shown). They are saved as simple "key=value" lines in data/settings.txt, read and
 * written with the same BufferedReader/BufferedWriter helper as every other file.
 */
public class AppSettings {

    public static final String DEFAULT_LOGO_TEXT = "chb cars.ph";

    private final String filePath;
    private String logoPath = "";
    private String logoText = DEFAULT_LOGO_TEXT;
    private boolean showLogoText = true;
    private BufferedImage logoImage;
    private String loadedPath = "";

    public AppSettings(String fileName) {
        this.filePath = FileHandler.path(fileName);
    }

    /** Creates the file at runtime when missing and reads the saved values. */
    public synchronized void load() {
        try {
            for (String line : FileHandler.readLines(filePath)) {
                int equals = line.indexOf('=');
                if (equals < 0) {
                    continue;
                }
                String key = line.substring(0, equals).trim();
                String value = line.substring(equals + 1).trim();
                switch (key) {
                    case "logoPath": logoPath = value; break;
                    case "logoText": logoText = value.isEmpty() ? DEFAULT_LOGO_TEXT : value; break;
                    case "showLogoText": showLogoText = !"false".equals(value); break;
                    default: break; // unknown line, ignore it
                }
            }
        } catch (IOException e) {
            throw new DataException("Cannot read the settings file " + filePath, e);
        }
        logoImage = null;
        loadedPath = "";
    }

    public synchronized void save() {
        List<String> lines = new ArrayList<>();
        lines.add("logoPath=" + logoPath);
        lines.add("logoText=" + logoText);
        lines.add("showLogoText=" + showLogoText);
        try {
            FileHandler.writeLines(filePath, lines);
        } catch (IOException e) {
            throw new DataException("Cannot save the settings file " + filePath, e);
        }
    }

    // ------------------------------------------------------------------ logo
    public String getLogoText() {
        return logoText;
    }

    /** Validates and stores the new text of the logo (it can not be empty). */
    public void setLogoText(String text) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("The logo text can not be empty.");
        }
        if (text.trim().length() > 24) {
            throw new ValidationException("The logo text must be 24 characters or fewer.");
        }
        this.logoText = text.trim();
    }

    /** The part of the text before the last dot, e.g. "chb cars" from "chb cars.ph". */
    public String getLogoMain() {
        int dot = logoText.lastIndexOf('.');
        return dot > 0 ? logoText.substring(0, dot) : logoText;
    }

    /** The red part of the text, e.g. ".ph". Empty when the text has no dot. */
    public String getLogoAccent() {
        int dot = logoText.lastIndexOf('.');
        return dot > 0 ? logoText.substring(dot) : "";
    }

    public boolean isShowLogoText() {
        return showLogoText;
    }

    public void setShowLogoText(boolean showLogoText) {
        this.showLogoText = showLogoText;
    }

    public synchronized boolean hasCustomLogo() {
        return getLogoImage() != null;
    }

    /** The custom logo picture, or null when the default drawn logo should be used. */
    public synchronized BufferedImage getLogoImage() {
        if (logoPath.isEmpty()) {
            return null;
        }
        if (logoImage == null || !logoPath.equals(loadedPath)) {
            try {
                logoImage = ImageIO.read(new File(logoPath));
            } catch (IOException e) {
                logoImage = null; // missing file: fall back to the default logo
            }
            loadedPath = logoPath;
        }
        return logoImage;
    }

    /** Copies the chosen picture into data/images and uses it as the logo. */
    public synchronized void setLogoFile(File source) throws ValidationException, IOException {
        logoPath = ImageUtil.importImage(source, "logo");
        logoImage = null;
    }

    /** Goes back to the default drawn logo. */
    public synchronized void resetLogo() {
        logoPath = "";
        logoImage = null;
        loadedPath = "";
    }
}
