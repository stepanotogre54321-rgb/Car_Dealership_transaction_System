package chbcars.ui;

import chbcars.model.Order;
import chbcars.model.Payment;
import chbcars.service.DocumentBuilder;
import chbcars.service.PaymentService;
import chbcars.util.ErrorHandler;
import chbcars.util.Format;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Payment records. Staff record and verify; the admin monitors, corrects and refunds. */
public class PaymentsPanel extends BasePanel {

    private final boolean admin;
    private final PaymentService service = new PaymentService();
    private final DefaultTableModel model = Theme.readOnlyModel("Payment ID", "Order", "Customer", "Method", "Amount",
            "Reference", "Status", "Receipt No.", "Recorded By", "Date");
    private final JTable table = Theme.table(model);
    private final JTextField search = Theme.textField(16);
    private final JComboBox<String> statusFilter = Theme.combo("All Statuses", Payment.PENDING_VERIFICATION,
            Payment.PAID, Payment.FAILED, Payment.REFUNDED);
    private final List<Payment> shown = new ArrayList<>();

    public PaymentsPanel(boolean admin) {
        Theme.widths(table, 80,75,130,105,115,110,125,80,75,110);
        this.admin = admin;
        search.setToolTipText("Search by payment ID, order, customer or reference");
        JButton verify = Theme.button("Verify Payment", Theme.SUCCESS);
        verify.setToolTipText("Simulated verification of a bank transfer / financing release");
        JButton receipt = Theme.secondary("View Receipt");

        List<JComponent> tools = new ArrayList<>();
        tools.add(Theme.label("Search:"));
        tools.add(search);
        tools.add(statusFilter);
        if (!admin) {
            JButton record = Theme.primary("+ Record Payment");
            record.setToolTipText("Record a payment received from a customer");
            record.addActionListener(e -> recordPayment());
            tools.add(record);
        }
        tools.add(verify);
        if (admin) {
            JButton fail = Theme.button("Mark Failed", Theme.WARNING);
            JButton refund = Theme.button("Refund", Theme.GRAY);
            refund.setToolTipText("Refund a confirmed payment (simulated)");
            fail.addActionListener(e -> verifySelected(false));
            refund.addActionListener(e -> refundSelected());
            tools.add(fail);
            tools.add(refund);
        }
        tools.add(receipt);

        add(top(admin ? "Payment Monitoring" : "Payments",
                admin ? "Review confirmed and pending payments and make authorized corrections."
                        : "Record cash payments and verify bank transfers or financing releases.",
                tools.toArray(new JComponent[0])), BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);

        liveSearch(search, this::refresh);
        statusFilter.addActionListener(e -> refresh());
        verify.addActionListener(e -> verifySelected(true));
        receipt.addActionListener(e -> showReceipt(selected()));
    }

    @Override
    public void refresh() {
        shown.clear();
        model.setRowCount(0);
        String key = search.getText().trim().toLowerCase();
        String status = (String) statusFilter.getSelectedItem();
        List<Payment> all = db.payments.getAll();
        for (int i = all.size() - 1; i >= 0; i--) {
            Payment p = all.get(i);
            if (!"All Statuses".equals(status) && !p.getStatus().equals(status)) {
                continue;
            }
            Order o = db.orders.findById(p.getOrderId());
            String customer = o == null ? "-" : db.customerName(o.getCustomerId());
            if (!key.isEmpty() && !(p.getId() + " " + p.getOrderId() + " " + customer + " " + p.getReference())
                    .toLowerCase().contains(key)) {
                continue;
            }
            shown.add(p);
            model.addRow(new Object[]{p.getId(), p.getOrderId(), customer, p.getMethodName(), Format.money(p.getAmount()),
                p.getReference().isEmpty() ? "-" : p.getReference(), p.getStatus(),
                p.getReceiptNo().isEmpty() ? "-" : p.getReceiptNo(), p.getRecordedBy(), p.getDateRecorded()});
        }
    }

    private Payment selected() {
        int row = Theme.selectedModelRow(table);
        if (row < 0) {
            Dialogs.warn(this, "Please select a payment in the table first.");
            return null;
        }
        return shown.get(row);
    }

    private void recordPayment() {
        PaymentDialog dialog = new PaymentDialog(owner(), null);
        if (dialog.showDialog()) {
            refresh();
            Payment p = dialog.getCreatedPayment();
            if (p != null && Payment.PAID.equals(p.getStatus())) {
                showReceipt(p);
            } else {
                Dialogs.success(this, "Payment recorded. It is now pending verification.");
            }
        }
    }

    private void verifySelected(boolean approve) {
        Payment p = selected();
        if (p == null) {
            return;
        }
        String question = approve
                ? "Run the (simulated) verification for " + p.getId() + " - reference " + p.getReference() + "?"
                : "Mark payment " + p.getId() + " as FAILED?";
        if (!Dialogs.confirm(this, question)) {
            return;
        }
        ErrorHandler.execute(this, () -> {
            boolean ok = service.verify(p, approve);
            Payment updated = db.payments.findById(p.getId());
            if (ok) {
                Dialogs.success(this, "Payment verified. Receipt " + updated.getReceiptNo() + " was generated.");
                showReceipt(updated);
            } else {
                Dialogs.warn(this, "The payment was marked as FAILED.");
            }
        });
        refresh();
    }

    private void refundSelected() {
        Payment p = selected();
        if (p != null && Dialogs.confirm(this, "Refund " + Format.money(p.getAmount()) + " for " + p.getId() + " (simulated)?")) {
            ErrorHandler.execute(this, () -> service.refund(p));
            refresh();
        }
    }

    private void showReceipt(Payment p) {
        if (p == null) {
            return;
        }
        if (!Payment.PAID.equals(p.getStatus()) && !Payment.REFUNDED.equals(p.getStatus())) {
            Dialogs.warn(this, "Receipts are only generated for confirmed payments.");
            return;
        }
        new SlipDialog(owner(), "Payment Receipt", DocumentBuilder.receipt(p), p.getReceiptNo()).setVisible(true);
    }
}
