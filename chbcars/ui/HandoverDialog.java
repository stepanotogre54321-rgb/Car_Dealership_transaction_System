package chbcars.ui;

import chbcars.data.DataStore;
import chbcars.model.Handover;
import chbcars.model.Order;
import chbcars.service.HandoverService;
import java.awt.Window;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** Staff checks the release conditions and records the handover. */
public class HandoverDialog extends BaseDialog {

    private final Order order;
    private final HandoverService service = new HandoverService();
    private final JCheckBox paymentOk = new JCheckBox("Payment is fully confirmed");
    private final JCheckBox idOk = new JCheckBox("Recipient's valid ID was checked");
    private final JCheckBox carOk = new JCheckBox("Car and chassis number match the order");
    private final JTextField recipient = Theme.textField(24);
    private final JTextArea notes = Theme.textArea(3, 24);
    private Handover created;

    public HandoverDialog(Window owner, Order order) {
        super(owner, "Vehicle Handover", "Order " + order.getId() + " - complete the checklist before releasing the car.",
                "Release Vehicle");
        this.order = order;
        DataStore db = DataStore.get();
        paymentOk.setSelected(Order.PAID.equals(order.getPaymentStatus()));
        paymentOk.setEnabled(false);
        recipient.setText(db.customerName(order.getCustomerId()));
        recipient.setToolTipText("Name of the person receiving the car");
        for (JCheckBox box : new JCheckBox[]{paymentOk, idOk, carOk}) {
            box.setOpaque(false);
            box.setFont(Theme.FONT);
        }
        init();
    }

    @Override
    protected JComponent buildForm() {
        DataStore db = DataStore.get();
        JPanel form = Theme.formPanel();
        Theme.addWide(form, 0, "Customer", Theme.label(db.customerName(order.getCustomerId())));
        Theme.addWide(form, 1, "Vehicle", Theme.label(db.carTitle(order.getCarId())));
        Theme.addWide(form, 2, "Checklist", paymentOk);
        Theme.addWide(form, 3, "", idOk);
        Theme.addWide(form, 4, "", carOk);
        Theme.addWide(form, 5, "Released to *", recipient);
        Theme.addWide(form, 6, "Notes", Theme.scroll(notes));
        return form;
    }

    @Override
    protected void onSubmit() throws Exception {
        created = service.release(order, recipient.getText(), notes.getText(), idOk.isSelected(), carOk.isSelected());
    }

    public Handover getCreatedHandover() {
        return created;
    }
}
