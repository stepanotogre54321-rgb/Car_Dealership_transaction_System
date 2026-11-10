package chbcars.ui;

import chbcars.model.ActivityLog;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Admin: review important system actions (newest first). */
public class ActivityLogPanel extends BasePanel {

    private final DefaultTableModel model = Theme.readOnlyModel("Time", "User", "Role", "Action", "Details");
    private final JTextField search = Theme.textField(20);

    public ActivityLogPanel() {
        search.setToolTipText("Search by user, action or details");
        add(top("Activity Logs", "A record of important actions taken in the system.", Theme.label("Search:"), search),
                BorderLayout.NORTH);
        javax.swing.JTable logTable = Theme.table(model);
        Theme.widths(logTable, 140, 100, 80, 160, 400);
        add(Theme.scroll(logTable), BorderLayout.CENTER);
        liveSearch(search, this::refresh);
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        String key = search.getText().trim().toLowerCase();
        for (ActivityLog log : db.logs.recent(500)) {
            String text = (log.getUsername() + " " + log.getAction() + " " + log.getDetails()).toLowerCase();
            if (key.isEmpty() || text.contains(key)) {
                model.addRow(new Object[]{log.getTimestamp(), log.getUsername(), log.getRole(), log.getAction(), log.getDetails()});
            }
        }
    }
}
