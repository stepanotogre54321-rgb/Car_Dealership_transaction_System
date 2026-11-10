package chbcars.data;

import chbcars.model.ActivityLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogRepository extends Repository<ActivityLog> {

    public LogRepository() {
        super("logs.txt", "LOG");
    }

    @Override
    protected ActivityLog parse(String[] fields) {
        return ActivityLog.fromFields(fields);
    }

    /** Newest first. */
    public List<ActivityLog> recent(int limit) {
        List<ActivityLog> all = getAll();
        Collections.reverse(all);
        return new ArrayList<>(all.subList(0, Math.min(limit, all.size())));
    }
}
