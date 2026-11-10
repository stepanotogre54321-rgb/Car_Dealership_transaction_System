package chbcars.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable text-file helper. Every read/write uses BufferedReader / BufferedWriter.
 * Files and folders are created at runtime if they do not exist yet.
 */
public final class FileHandler {

    public static final String DATA_DIR = "data";
    public static final String IMAGE_DIR = DATA_DIR + "/images";
    public static final String DOC_DIR = DATA_DIR + "/documents";

    private FileHandler() {
    }

    public static String path(String fileName) {
        return DATA_DIR + "/" + fileName;
    }

    /** Creates the folder and the empty file when missing. */
    public static void ensureFile(String path) throws IOException {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public static List<String> readLines(String path) throws IOException {
        ensureFile(path);
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    public static void writeLines(String path, List<String> lines) throws IOException {
        ensureFile(path);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void appendLine(String path, String line) throws IOException {
        ensureFile(path);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path, true), StandardCharsets.UTF_8))) {
            writer.write(line);
            writer.newLine();
        }
    }

    /** Saves any plain text (reports, receipts, slips). */
    public static void writeText(String path, String text) throws IOException {
        ensureFile(path);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write(text);
        }
    }

    /** Joins fields into one database line separated by '|'. */
    public static String join(Object... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append('|');
            }
            sb.append(escape(fields[i] == null ? "" : String.valueOf(fields[i])));
        }
        return sb.toString();
    }

    /** Splits a database line back into its fields. */
    public static String[] split(String line) {
        String[] parts = line.split("\\|", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = unescape(parts[i]);
        }
        return parts;
    }

    private static String escape(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '|': sb.append("\\p"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String unescape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                if (next == 'p') {
                    sb.append('|');
                } else if (next == 'n') {
                    sb.append('\n');
                } else {
                    sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
