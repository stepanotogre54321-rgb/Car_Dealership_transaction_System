package chbcars.ui;

import chbcars.service.ReportService;
import chbcars.util.ErrorHandler;
import chbcars.util.FileHandler;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/** Admin: sales, inventory, order, payment and staff activity reports. */
public class ReportsPanel extends BasePanel {

    private final ReportService reports = new ReportService();
    private final JComboBox<String> type = Theme.combo(ReportService.REPORT_TYPES);
    private final JTextArea output = new JTextArea();

    public ReportsPanel() {
        output.setFont(Theme.MONO);
        output.setEditable(false);
        output.setBorder(new EmptyBorder(12, 14, 12, 14));

        JButton generate = Theme.primary("Generate");
        generate.setToolTipText("Create the selected report");
        JButton export = Theme.secondary("Export .txt");
        export.setToolTipText("Save the report in data/documents");

        add(top("Reports", "Generate sales, inventory, order, payment and staff activity reports.",
                Theme.label("Report:"), type, generate, export), BorderLayout.NORTH);
        add(Theme.scroll(output), BorderLayout.CENTER);

        generate.addActionListener(e -> ErrorHandler.execute(this, this::generate));
        export.addActionListener(e -> ErrorHandler.execute(this, () -> {
            if (output.getText().isEmpty()) {
                generate();
            }
            String name = ((String) type.getSelectedItem()).replace(' ', '_') + "_" + System.currentTimeMillis();
            String path = FileHandler.DOC_DIR + "/" + name + ".txt";
            FileHandler.writeText(path, output.getText());
            Dialogs.success(this, "Report saved to " + path);
        }));
    }

    private void generate() {
        output.setText(reports.generate((String) type.getSelectedItem()));
        output.setCaretPosition(0);
    }

    @Override
    public void refresh() {
        generate();
    }
}
