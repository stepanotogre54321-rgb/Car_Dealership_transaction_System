package chbcars.ui;

import chbcars.model.Order;
import chbcars.service.DocumentBuilder;
import chbcars.service.OrderService;
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

/** Orders list. Staff: transaction history. Admin: also approve, hold, resume and cancel. */
public class OrdersPanel extends BasePanel {

    private final boolean admin;
    private final OrderService service = new OrderService();
    private final DefaultTableModel model = Theme.readOnlyModel("Order ID", "Date", "Customer", "Vehicle", "Payment Method",
            "Total", "Order Status", "Payment Status", "Staff");
    private final JTable table = Theme.table(model);
    private final JTextField search = Theme.textField(18);
    private final JComboBox<String> statusFilter = Theme.combo("All Statuses", Order.PENDING, Order.UNDER_REVIEW,
            Order.AWAITING_PAYMENT, Order.READY, Order.COMPLETED, Order.CANCELLED, Order.ON_HOLD);
    private final List<Order> shown = new ArrayList<>();

    public OrdersPanel(boolean admin) {
        Theme.widths(table, 80,120,140,150,100,115,125,125,70);
        this.admin = admin;
        search.setToolTipText("Search by order ID, customer or car");
        JButton view = Theme.primary("View Details");
        view.setToolTipText("Order slip, payments, receipts and handover");

        List<JComponent> tools = new ArrayList<>();
        tools.add(Theme.label("Search:"));
        tools.add(search);
        tools.add(statusFilter);
        tools.add(view);
        if (admin) {
            JButton approve = Theme.button("Approve", Theme.SUCCESS);
            approve.setToolTipText("Approve an order that is under review (e.g. financing)");
            JButton hold = Theme.button("Hold", Theme.WARNING);
            JButton resume = Theme.secondary("Resume");
            JButton cancel = Theme.button("Cancel Order", Theme.GRAY);
            cancel.setToolTipText("Cancel the order, free the car and refund payments (simulated)");
            approve.addActionListener(e -> act(o -> service.approve(o), "approve"));
            hold.addActionListener(e -> act(o -> service.hold(o), "put on hold"));
            resume.addActionListener(e -> act(o -> service.resume(o), "resume"));
            cancel.addActionListener(e -> cancelSelected());
            tools.add(approve);
            tools.add(hold);
            tools.add(resume);
            tools.add(cancel);
        }

        add(top(admin ? "Order Management" : "Transaction History",
                admin ? "Review all orders and handle approvals, holds and cancellations."
                        : "Search customer orders, payments, receipts and handover confirmations.",
                tools.toArray(new JComponent[0])), BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);

        liveSearch(search, this::refresh);
        statusFilter.addActionListener(e -> refresh());
        view.addActionListener(e -> {
            Order o = selected();
            if (o != null) {
                new SlipDialog(owner(), "Transaction Details - " + o.getId(), DocumentBuilder.transactionDetails(o),
                        o.getId() + "_details").setVisible(true);
            }
        });
    }

    /** One admin action on the selected order. */
    private interface OrderAction {
        void run(Order order) throws Exception;
    }

    @Override
    public void refresh() {
        shown.clear();
        model.setRowCount(0);
        String key = search.getText().trim().toLowerCase();
        String status = (String) statusFilter.getSelectedItem();
        List<Order> all = db.orders.getAll();
        for (int i = all.size() - 1; i >= 0; i--) {
            Order o = all.get(i);
            if (!"All Statuses".equals(status) && !o.getOrderStatus().equals(status)) {
                continue;
            }
            String customer = db.customerName(o.getCustomerId());
            String car = db.carTitle(o.getCarId());
            if (!key.isEmpty() && !(o.getId() + " " + customer + " " + car).toLowerCase().contains(key)) {
                continue;
            }
            shown.add(o);
            model.addRow(new Object[]{o.getId(), o.getDateCreated(), customer, car, o.getPaymentMethod(),
                Format.money(o.getTotal()), o.getOrderStatus(), o.getPaymentStatus(), o.getStaffUsername()});
        }
    }

    private Order selected() {
        int row = Theme.selectedModelRow(table);
        if (row < 0) {
            Dialogs.warn(this, "Please select an order in the table first.");
            return null;
        }
        return shown.get(row);
    }

    private void act(OrderAction action, String verb) {
        Order order = selected();
        if (order == null) {
            return;
        }
        ErrorHandler.execute(this, () -> {
            action.run(order);
            Dialogs.success(this, "Order " + order.getId() + ": " + verb + " done.");
        });
        refresh();
    }

    private void cancelSelected() {
        Order order = selected();
        if (order != null && Dialogs.confirm(this, "Cancel order " + order.getId() + "?\nThe car becomes available again and "
                + "any confirmed payment is refunded (simulated).")) {
            ErrorHandler.execute(this, () -> service.cancel(order));
            refresh();
        }
    }
}
