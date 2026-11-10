package chbcars.ui;

import chbcars.data.DataStore;
import chbcars.model.Customer;
import chbcars.util.Format;
import chbcars.util.ValidationException;
import chbcars.util.Validator;
import java.awt.Window;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Add or edit a customer record. */
public class CustomerFormDialog extends BaseDialog {

    private final Customer existing;
    private final JTextField fullName = Theme.textField(24);
    private final JTextField contact = Theme.textField(24);
    private final JTextField email = Theme.textField(24);
    private final JTextField address = Theme.textField(24);
    private final JComboBox<String> idType = Theme.combo(Customer.ID_TYPES);
    private final JTextField idNumber = Theme.textField(24);

    public CustomerFormDialog(Window owner, Customer existing) {
        super(owner, existing == null ? "Register Customer" : "Edit Customer",
                "Fields marked * are required.", "Save Customer");
        this.existing = existing;
        contact.setToolTipText("e.g. 09171234567");
        if (existing != null) {
            fullName.setText(existing.getFullName());
            contact.setText(existing.getContactNo());
            email.setText(existing.getEmail());
            address.setText(existing.getAddress());
            idType.setSelectedItem(existing.getIdType());
            idNumber.setText(existing.getIdNumber());
        }
        init();
    }

    @Override
    protected JComponent buildForm() {
        JPanel form = Theme.formPanel();
        Theme.addWide(form, 0, "Full name *", fullName);
        Theme.addWide(form, 1, "Contact no. *", contact);
        Theme.addWide(form, 2, "Email", email);
        Theme.addWide(form, 3, "Address *", address);
        Theme.addWide(form, 4, "Valid ID type *", idType);
        Theme.addWide(form, 5, "ID number *", idNumber);
        return form;
    }

    @Override
    protected void onSubmit() throws Exception {
        Customer c = existing == null ? new Customer() : existing;
        fillFrom(c, fullName.getText(), contact.getText(), email.getText(), address.getText(),
                (String) idType.getSelectedItem(), idNumber.getText());
        if (existing == null) {
            c.setDateRegistered(Format.now());
            DataStore.get().customers.add(c);
            DataStore.get().log("Customer Registered", c.getFullName());
        } else {
            DataStore.get().customers.update(c);
            DataStore.get().log("Customer Edited", c.getId());
        }
    }

    /** Validates the raw text and copies it into the customer (also used by the New Order page). */
    public static void fillFrom(Customer c, String name, String phone, String mail, String addr,
            String idKind, String idNo) throws ValidationException {
        c.setFullName(Validator.requireName(name, "Full name"));
        c.setContactNo(Validator.requirePhone(phone));
        c.setEmail(Validator.optionalEmail(mail));
        c.setAddress(Validator.requireText(addr, "Address", 5, 120));
        c.setIdType(idKind);
        c.setIdNumber(Validator.requireText(idNo, "ID number", 5, 30));
    }
}
