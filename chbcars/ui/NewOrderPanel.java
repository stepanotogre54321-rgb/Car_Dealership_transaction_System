package chbcars.ui;

import chbcars.model.Car;
import chbcars.model.Customer;
import chbcars.model.Order;
import chbcars.model.Payment;
import chbcars.service.DocumentBuilder;
import chbcars.service.OrderService;
import chbcars.util.ErrorHandler;
import chbcars.util.Format;
import chbcars.util.ImageUtil;
import chbcars.util.ValidationException;
import chbcars.util.Validator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Staff: the customer ordering process in one screen (customer, car, payment method, review and save). */
public class NewOrderPanel extends BasePanel {

    private final OrderService orderService = new OrderService();
    private final Runnable onSaved;
    private boolean updating;

    private final JTextField searchCustomer = Theme.textField(14);
    private final JComboBox<String> customerBox = Theme.combo("-- New customer --");
    private final List<Customer> matches = new ArrayList<>();
    private final JTextField name = Theme.textField(20);
    private final JTextField phone = Theme.textField(20);
    private final JTextField email = Theme.textField(20);
    private final JTextField address = Theme.textField(20);
    private final JComboBox<String> idType = Theme.combo(Customer.ID_TYPES);
    private final JTextField idNo = Theme.textField(20);

    private final JComboBox<String> carBox = Theme.combo();
    private final List<Car> cars = new ArrayList<>();
    private final JLabel carImage = new JLabel();
    private final JLabel carInfo = new JLabel(" ");

    private final JComboBox<String> methodBox = Theme.combo(Payment.METHODS);
    private final JTextArea notes = Theme.textArea(3, 20);
    private final JLabel priceLbl = Theme.label("-");
    private final JLabel feeLbl = Theme.label("-");
    private final JLabel chargeLbl = Theme.label("-");
    private final JLabel totalLbl = Theme.heading("-");

    public NewOrderPanel(Runnable onSaved) {
        this.onSaved = onSaved;
        searchCustomer.setToolTipText("Type a name or phone number, then press Find");
        phone.setToolTipText("e.g. 09171234567");
        carBox.setToolTipText("Only available cars are listed");
        methodBox.setToolTipText("Cash, bank transfer/deposit, or financing");
        carImage.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 14, 14);
        c.gridx = 0;
        content.add(customerCard(), c);
        c.gridx = 1;
        c.insets = new Insets(0, 0, 14, 0);
        JPanel right = new JPanel(new BorderLayout(0, 14));
        right.setOpaque(false);
        right.add(vehicleCard(), BorderLayout.NORTH);
        right.add(paymentCard(), BorderLayout.CENTER);
        content.add(right, c);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JButton clear = Theme.neutral("Clear Form");
        clear.addActionListener(e -> {
            clearForm();
            refresh();
        });
        JButton save = Theme.primary("Review & Save Order");
        save.setToolTipText("Check the details and create the order");
        save.addActionListener(e -> saveOrder());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(clear);
        buttons.add(save);

        add(top("New Order", "Assist the customer: choose a car, enter details, pick a payment method and save."), BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        customerBox.addActionListener(e -> customerChosen());
        carBox.addActionListener(e -> carChosen());
        methodBox.addActionListener(e -> updateSummary());
    }

    private JPanel customerCard() {
        JButton find = Theme.secondary("Find");
        find.setToolTipText("Search existing customers");
        find.addActionListener(e -> findCustomers());
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(searchCustomer, BorderLayout.CENTER);
        searchRow.add(find, BorderLayout.EAST);

        JPanel form = Theme.formPanel();
        Theme.addWide(form, 0, "Find existing", searchRow);
        Theme.addWide(form, 1, "Customer", customerBox);
        Theme.addWide(form, 2, "Full name *", name);
        Theme.addWide(form, 3, "Contact no. *", phone);
        Theme.addWide(form, 4, "Email", email);
        Theme.addWide(form, 5, "Address *", address);
        Theme.addWide(form, 6, "Valid ID type *", idType);
        Theme.addWide(form, 7, "ID number *", idNo);

        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 8));
        card.add(Theme.heading("1. Customer Information"), BorderLayout.NORTH);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(form, BorderLayout.NORTH);
        card.add(wrap, BorderLayout.CENTER);
        return card;
    }

    private JPanel vehicleCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 8));
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(Theme.heading("2. Select Car"), BorderLayout.NORTH);
        top.add(carBox, BorderLayout.CENTER);
        card.add(top, BorderLayout.NORTH);
        card.add(carImage, BorderLayout.CENTER);
        card.add(carInfo, BorderLayout.SOUTH);
        return card;
    }

    private JPanel paymentCard() {
        JPanel summary = new JPanel(new GridBagLayout());
        summary.setOpaque(false);
        addSummaryRow(summary, 0, "Car price", priceLbl);
        addSummaryRow(summary, 1, "Documentation fee", feeLbl);
        addSummaryRow(summary, 2, "Payment method charge", chargeLbl);
        addSummaryRow(summary, 3, "TOTAL", totalLbl);

        JPanel form = Theme.formPanel();
        Theme.addWide(form, 0, "Payment method", methodBox);
        Theme.addWide(form, 1, "Notes", Theme.scroll(notes));

        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 8));
        card.add(Theme.heading("3. Payment & Summary"), BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(summary, BorderLayout.SOUTH);
        return card;
    }

    private void addSummaryRow(JPanel panel, int row, String label, JLabel value) {
        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 0;
        l.gridy = row;
        l.weightx = 1;
        l.anchor = GridBagConstraints.WEST;
        l.insets = new Insets(3, 4, 3, 4);
        panel.add(Theme.label(label), l);
        GridBagConstraints v = new GridBagConstraints();
        v.gridx = 1;
        v.gridy = row;
        v.anchor = GridBagConstraints.EAST;
        v.insets = new Insets(3, 4, 3, 4);
        panel.add(value, v);
    }

    // ------------------------------------------------------------- behaviour
    @Override
    public void refresh() {
        loadCars(null);
    }

    /** Called by the Browse page: shows this page with the chosen car already selected. */
    public void setCar(Car car) {
        loadCars(car.getId());
    }

    private void loadCars(String selectId) {
        String keep = selectId;
        if (keep == null && carBox.getSelectedIndex() >= 0 && carBox.getSelectedIndex() < cars.size()) {
            keep = cars.get(carBox.getSelectedIndex()).getId();
        }
        updating = true;
        cars.clear();
        carBox.removeAllItems();
        int index = -1;
        for (Car c : db.cars.findByStatus(Car.AVAILABLE)) {
            cars.add(c);
            carBox.addItem(c.getId() + " - " + c.getTitle() + " - " + Format.money(c.getPrice()));
            if (c.getId().equals(keep)) {
                index = cars.size() - 1;
            }
        }
        if (index >= 0) {
            carBox.setSelectedIndex(index);
        }
        updating = false;
        carChosen();
    }

    private Car selectedCar() {
        int i = carBox.getSelectedIndex();
        return i < 0 || i >= cars.size() ? null : cars.get(i);
    }

    private void carChosen() {
        if (updating) {
            return;
        }
        Car c = selectedCar();
        carImage.setIcon(ImageUtil.load(c == null ? "" : c.getImagePath(), 360, 170));
        carInfo.setText(c == null ? "<html>No cars are available right now.</html>"
                : "<html><b>" + c.getTitle() + "</b><br>" + c.getColor() + " \u2022 " + c.getTransmission() + " \u2022 "
                + c.getFuelType() + " \u2022 " + String.format("%,d km", c.getMileage()) + "</html>");
        updateSummary();
    }

    private void updateSummary() {
        Car c = selectedCar();
        if (c == null) {
            priceLbl.setText("-");
            feeLbl.setText("-");
            chargeLbl.setText("-");
            totalLbl.setText("-");
            return;
        }
        String method = (String) methodBox.getSelectedItem();
        double methodCharge = Payment.forMethod(method).calculateCharge(c.getPrice());
        priceLbl.setText(Format.money(c.getPrice()));
        feeLbl.setText(Format.money(Order.DOCUMENTATION_FEE));
        chargeLbl.setText(Format.money(methodCharge));
        totalLbl.setText(Format.money(c.getPrice() + Order.DOCUMENTATION_FEE + methodCharge));
        totalLbl.setForeground(Theme.RED);
    }

    private void findCustomers() {
        ErrorHandler.execute(this, () -> {
            matches.clear();
            matches.addAll(db.customers.search(Validator.requireText(searchCustomer.getText(), "Search text")));
            updating = true;
            customerBox.removeAllItems();
            customerBox.addItem("-- New customer --");
            for (Customer c : matches) {
                customerBox.addItem(c.getFullName() + " (" + c.getContactNo() + ")");
            }
            updating = false;
            if (matches.isEmpty()) {
                Dialogs.info(this, "No customer found. Fill in the form to register a new customer.");
            } else {
                customerBox.setSelectedIndex(1);
            }
        });
    }

    private void customerChosen() {
        if (updating) {
            return;
        }
        int i = customerBox.getSelectedIndex();
        boolean existing = i > 0 && i <= matches.size();
        if (existing) {
            Customer c = matches.get(i - 1);
            name.setText(c.getFullName());
            phone.setText(c.getContactNo());
            email.setText(c.getEmail());
            address.setText(c.getAddress());
            idType.setSelectedItem(c.getIdType());
            idNo.setText(c.getIdNumber());
        }
        for (javax.swing.JComponent f : new javax.swing.JComponent[]{name, phone, email, address, idType, idNo}) {
            f.setEnabled(!existing);
        }
    }

    private Customer buildCustomer() throws ValidationException {
        int i = customerBox.getSelectedIndex();
        if (i > 0 && i <= matches.size()) {
            Customer saved = db.customers.findById(matches.get(i - 1).getId());
            if (saved == null) {
                throw new ValidationException("The selected customer no longer exists.");
            }
            return saved;
        }
        Customer c = new Customer();
        CustomerFormDialog.fillFrom(c, name.getText(), phone.getText(), email.getText(), address.getText(),
                (String) idType.getSelectedItem(), idNo.getText());
        if (!db.customers.search(c.getContactNo()).isEmpty()) {
            for (Customer other : db.customers.search(c.getContactNo())) {
                if (other.getContactNo().equals(c.getContactNo())) {
                    throw new ValidationException("A customer with this contact number already exists.\nUse \"Find\" to select him/her.");
                }
            }
        }
        c.setDateRegistered(Format.now());
        return c;
    }

    private void saveOrder() {
        ErrorHandler.execute(this, () -> {
            Car car = selectedCar();
            Validator.requireSelected(car, "a car");
            Customer customer = buildCustomer();
            String method = (String) methodBox.getSelectedItem();
            double total = car.getPrice() + OrderService.calculateCharges(car, method);
            boolean ok = Dialogs.confirm(this, "Create this order?\n\nCustomer: " + customer.getFullName()
                    + "\nCar: " + car.getTitle() + "\nPayment: " + method + "\nTOTAL: " + Format.money(total));
            if (!ok) {
                return;
            }
            Order order = orderService.createOrder(customer, car, method, notes.getText());
            clearForm();
            refresh();
            new SlipDialog(owner(), "Order Confirmation Slip", DocumentBuilder.orderSlip(order), order.getId() + "_slip")
                    .setVisible(true);
            if (onSaved != null) {
                onSaved.run();
            }
        });
    }

    private void clearForm() {
        updating = true;
        matches.clear();
        customerBox.removeAllItems();
        customerBox.addItem("-- New customer --");
        updating = false;
        searchCustomer.setText("");
        for (JTextField f : new JTextField[]{name, phone, email, address, idNo}) {
            f.setText("");
        }
        idType.setSelectedIndex(0);
        methodBox.setSelectedIndex(0);
        notes.setText("");
        customerChosen();
    }
}
