package chbcars.ui;

import chbcars.data.DataStore;
import chbcars.model.Order;
import chbcars.model.Payment;
import chbcars.service.PaymentService;
import chbcars.util.Format;
import chbcars.util.Validator;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Staff records a payment against an order that is waiting for payment. */
public class PaymentDialog extends BaseDialog {

    private final DataStore db = DataStore.get();
    private final PaymentService service = new PaymentService();
    private final List<Order> orders = new ArrayList<>();
    private final JComboBox<String> orderBox = Theme.combo();
    private final JLabel customer = Theme.label("-");
    private final JLabel method = Theme.label("-");
    private final JLabel total = Theme.label("-");
    private final JLabel paid = Theme.label("-");
    private final JLabel due = Theme.label("-");
    private final JTextField amount = Theme.textField(16);
    private final JTextField reference = Theme.textField(16);
    private Payment created;

    public PaymentDialog(Window owner, Order preselected) {
        super(owner, "Record Payment", "Cash is confirmed immediately. Bank transfers and financing must be verified.",
                "Record Payment");
        for (Order o : db.orders.findByOrderStatus(Order.AWAITING_PAYMENT)) {
            if (service.amountDue(o) > 0.005) {
                orders.add(o);
                orderBox.addItem(o.getId() + " - " + db.customerName(o.getCustomerId()));
            }
        }
        orderBox.addActionListener(e -> showOrder());
        amount.setToolTipText("Amount received now");
        reference.setToolTipText("Bank / financing reference number (optional for cash)");
        if (preselected != null) {
            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).getId().equals(preselected.getId())) {
                    orderBox.setSelectedIndex(i);
                }
            }
        }
        showOrder();
        init();
    }

    private Order selected() {
        int i = orderBox.getSelectedIndex();
        return i < 0 ? null : orders.get(i);
    }

    private void showOrder() {
        Order o = selected();
        if (o == null) {
            return;
        }
        customer.setText(db.customerName(o.getCustomerId()) + "  |  " + db.carTitle(o.getCarId()));
        method.setText(o.getPaymentMethod());
        total.setText(Format.money(o.getTotal()));
        paid.setText(Format.money(service.confirmedAmount(o.getId())) + "  (pending: "
                + Format.money(service.pendingAmount(o.getId())) + ")");
        double balance = service.amountDue(o);
        due.setText(Format.money(balance));
        amount.setText(String.format("%.2f", balance));
    }

    @Override
    protected JComponent buildForm() {
        JPanel form = Theme.formPanel();
        Theme.addWide(form, 0, "Order *", orderBox);
        Theme.addWide(form, 1, "Customer / Car", customer);
        Theme.addWide(form, 2, "Payment method", method);
        Theme.addWide(form, 3, "Order total", total);
        Theme.addWide(form, 4, "Already paid", paid);
        Theme.addWide(form, 5, "Amount due", due);
        Theme.addWide(form, 6, "Amount received (\u20B1) *", amount);
        Theme.addWide(form, 7, "Reference no.", reference);
        return form;
    }

    @Override
    protected void onSubmit() throws Exception {
        Validator.requireSelected(selected(), "an order (none is waiting for payment)");
        double value = Validator.parseMoney(amount.getText(), "Amount");
        created = service.record(selected(), value, reference.getText());
    }

    public Payment getCreatedPayment() {
        return created;
    }
}
