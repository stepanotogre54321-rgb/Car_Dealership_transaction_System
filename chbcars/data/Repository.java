package chbcars.data;

import chbcars.model.Entity;
import chbcars.util.DataException;
import chbcars.util.ErrorHandler;
import chbcars.util.FileHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ABSTRACTION + reusable code: one generic class that knows how to load, add,
 * update and delete records from a text file. Each repository only has to say
 * how one line becomes an object (parse). Data is always re-read from the file
 * first, so two users running the system at the same time see the same records.
 */
public abstract class Repository<T extends Entity> {

    private final String filePath;
    private final String prefix;
    private final List<T> items = new ArrayList<>();

    protected Repository(String fileName, String prefix) {
        this.filePath = FileHandler.path(fileName);
        this.prefix = prefix;
    }

    /** Turns the fields of one line into an object. */
    protected abstract T parse(String[] fields);

    /** Creates the .txt file at runtime when it does not exist. */
    public void ensureStorage() {
        try {
            FileHandler.ensureFile(filePath);
        } catch (IOException e) {
            throw new DataException("Cannot create the data file " + filePath, e);
        }
    }

    public synchronized void load() {
        items.clear();
        try {
            for (String line : FileHandler.readLines(filePath)) {
                try {
                    items.add(parse(FileHandler.split(line)));
                } catch (RuntimeException badLine) {
                    ErrorHandler.logError(badLine); // skip a damaged line instead of crashing
                }
            }
        } catch (IOException e) {
            throw new DataException("Cannot read the data file " + filePath, e);
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (T item : items) {
            lines.add(item.toFileLine());
        }
        try {
            FileHandler.writeLines(filePath, lines);
        } catch (IOException e) {
            throw new DataException("Cannot save the data file " + filePath, e);
        }
    }

    public synchronized List<T> getAll() {
        load();
        return new ArrayList<>(items);
    }

    public synchronized T findById(String id) {
        load();
        for (T item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /** Adds a record, giving it an auto-numbered ID (e.g. CAR-0001). Returns the new ID. */
    public synchronized String add(T item) {
        load();
        if (item.getId() == null || item.getId().isEmpty()) {
            item.setId(nextId());
        }
        items.add(item);
        save();
        return item.getId();
    }

    public synchronized void update(T item) {
        load();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                save();
                return;
            }
        }
        throw new DataException("Record " + item.getId() + " no longer exists.");
    }

    public synchronized void delete(String id) {
        load();
        if (items.removeIf(item -> item.getId().equals(id))) {
            save();
        }
    }

    private String nextId() {
        int max = 0;
        for (T item : items) {
            String id = item.getId();
            int dash = id.lastIndexOf('-');
            if (dash >= 0) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(dash + 1)));
                } catch (NumberFormatException ignored) {
                    // not a numbered ID, skip it
                }
            }
        }
        return prefix + "-" + String.format("%04d", max + 1);
    }
}
