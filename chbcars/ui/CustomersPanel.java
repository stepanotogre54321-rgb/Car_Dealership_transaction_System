package chbcars.ui;

import chbcars.model.Customer;
import chbcars.service.DocumentBuilder;
import chbcars.util.ErrorHandler;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Customer records. Staff can register and search; the admin can also edit and delete. */
public class CustomersPanel extends BasePanel {

    private final boolean admin;
    private final DefaultTableModel model = Theme.readOnlyModel("Customer ID", "Name", "Contact", "Email", "Address",
            "ID Type", "Registered", "Orders");
    private final JTable table = Theme.table(model);
    private final JTextField search = Theme.textField(18);
    private final List<Customer> shown = new ArrayList<>();

    public CustomersPanel(boolean admin) {
        Theme.widths(table, 90,150,110,160,200,120,130,60);
        this.admin = admin;
        search.setToolTipText("Search by name, contact number, e-mail or ID");
        JButton add = Theme.primary("+ Register Customer");
        add.setToolTipText("Add a new customer record");
        JButton history = Theme.secondary("Purchase History");
        history.setToolTipText("Show this customer's orders");

        List<JComponent> tools = new ArrayList<>();
        tools.add(Theme.label("Search:"));
        tools.add(search);
        tools.add(add);
        if (admin) {
            JButton edit = Theme.secondary("Edit");
            JButton delete = Theme.button("Delete", Theme.GRAY);
            delete.setToolTipText("Delete a customer without orders (admin password required)");
            edit.addActionListener(e -> editSelected());
            delete.addActionListener(e -> deleteSelected());
            tools.add(edit);
            tools.add(delete);
        }
        tools.add(history);

        add(top("Customers", admin ? "View, correct and manage customer records." : "Register new customers and search existing ones.",
                tools.toArray(new JComponent[0])), BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);

        liveSearch(search, this::refresh);
        add.addActionListener(e -> {
            if (new CustomerFormDialog(owner(), null).showDialog()) {
                refresh();
            }
        });
        history.addActionListener(e -> {
            Customer c = selected();
            if (c != null) {
                new SlipDialog(owner(), "Purchase History", DocumentBuilder.customerHistory(c), c.getId() + "_history")
                        .setVisible(true);
            }
        });
    }

    @Override
    public void refresh() {
        shown.clear();
        model.setRowCount(0);
        for (Customer c : db.customers.search(search.getText())) {
            shown.add(c);
            model.addRow(new Object[]{c.getId(), c.getFullName(), c.getContactNo(), c.getEmail(), c.getAddress(),
                c.getIdType(), c.getDateRegistered(), db.orders.findByCustomer(c.getId()).size()});
        }
    }

    private Customer selected() {
        int row = Theme.selectedModelRow(table);
        if (row < 0) {
            Dialogs.warn(this, "Please select a customer in the table first.");
            return null;
        }
        return shown.get(row);
    }

    private void editSelected() {
        Customer c = selected();
        if (c != null && new CustomerFormDialog(owner(), db.customers.findById(c.getId())).showDialog()) {
            refresh();
        }
    }

    private void deleteSelected() {
        Customer c = selected();
        if (c == null) {
            return;
        }
        if (!db.orders.findByCustomer(c.getId()).isEmpty()) {
            Dialogs.warn(this, "This customer has orders, so the record cannot be deleted.");
            return;
        }
        if (!Dialogs.confirm(this, "Delete customer " + c.getFullName() + "?")
                || !Dialogs.confirmAdminPassword(this, "delete this customer")) {
            return;
        }
        ErrorHandler.execute(this, () -> {
            db.customers.delete(c.getId());
            db.log("Customer Deleted", c.getId());
        });
        refresh();
    }
}
