package chbcars.ui;

import chbcars.model.Handover;
import chbcars.model.Order;
import chbcars.service.DocumentBuilder;
import chbcars.util.Format;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Staff: release fully paid cars to their owners and keep the handover records. */
public class HandoverPanel extends BasePanel {

    private final DefaultTableModel readyModel = Theme.readOnlyModel("Order ID", "Customer", "Vehicle", "Total",
            "Payment Status", "Order Status");
    private final JTable readyTable = Theme.table(readyModel);
    private final DefaultTableModel doneModel = Theme.readOnlyModel("Handover ID", "Order", "Customer", "Vehicle",
            "Released To", "Released By", "Date");
    private final JTable doneTable = Theme.table(doneModel);
    private final List<Order> ready = new ArrayList<>();
    private final List<Handover> done = new ArrayList<>();

    public HandoverPanel() {
        Theme.widths(readyTable, 90,150,200,120,130,130);
        Theme.widths(doneTable, 100,90,150,180,150,100,130);
        JButton release = Theme.primary("Record Handover");
        release.setToolTipText("Release the selected car to its owner");
        JButton form = Theme.secondary("View Handover Form");
        add(top("Vehicle Release / Handover", "Only fully paid orders that are Ready for Release can be handed over.",
                release, form), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, section("Ready for Release", readyTable),
                section("Handover History", doneTable));
        split.setResizeWeight(0.5);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        release.addActionListener(e -> releaseSelected());
        form.addActionListener(e -> {
            int row = Theme.selectedModelRow(doneTable);
            if (row < 0) {
                Dialogs.warn(this, "Please select a handover record in the history table.");
            } else {
                Handover h = done.get(row);
                new SlipDialog(owner(), "Handover Form", DocumentBuilder.handoverForm(h), h.getId()).setVisible(true);
            }
        });
    }

    private JPanel section(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(Theme.heading(title), BorderLayout.NORTH);
        p.add(Theme.scroll(table), BorderLayout.CENTER);
        return p;
    }

    @Override
    public void refresh() {
        ready.clear();
        readyModel.setRowCount(0);
        for (Order o : db.orders.findByOrderStatus(Order.READY)) {
            ready.add(o);
            readyModel.addRow(new Object[]{o.getId(), db.customerName(o.getCustomerId()), db.carTitle(o.getCarId()),
                Format.money(o.getTotal()), o.getPaymentStatus(), o.getOrderStatus()});
        }
        done.clear();
        doneModel.setRowCount(0);
        List<Handover> all = db.handovers.getAll();
        for (int i = all.size() - 1; i >= 0; i--) {
            Handover h = all.get(i);
            done.add(h);
            doneModel.addRow(new Object[]{h.getId(), h.getOrderId(), db.customerName(h.getCustomerId()),
                db.carTitle(h.getCarId()), h.getRecipientName(), h.getStaffUsername(), h.getDateReleased()});
        }
    }

    private void releaseSelected() {
        int row = Theme.selectedModelRow(readyTable);
        if (row < 0) {
            Dialogs.warn(this, "Please select an order that is Ready for Release.");
            return;
        }
        HandoverDialog dialog = new HandoverDialog(owner(), ready.get(row));
        if (dialog.showDialog()) {
            refresh();
            Handover h = dialog.getCreatedHandover();
            new SlipDialog(owner(), "Handover Form", DocumentBuilder.handoverForm(h), h.getId()).setVisible(true);
        }
    }
}
