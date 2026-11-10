package chbcars.model;

/**
 * ABSTRACTION: the base of every saved record. It only knows an ID and that
 * each record must be able to turn itself into one line of the text database.
 */
public abstract class Entity {

    private String id;

    protected Entity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** Converts this record to one line for the .txt database. */
    public abstract String toFileLine();

    /** Short one-line description (used by combo boxes and logs). */
    public abstract String getSummary();

    @Override
    public String toString() {
        return getSummary();
    }
}
