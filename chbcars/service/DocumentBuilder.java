package chbcars.service;

import chbcars.data.DataStore;
import chbcars.data.Session;
import chbcars.model.Car;
import chbcars.model.Customer;
import chbcars.model.Handover;
import chbcars.model.Order;
import chbcars.model.Payment;
import chbcars.model.User;
import chbcars.util.Format;
import java.util.List;

/** Builds the printable text for slips, receipts, handover forms and histories. */
public final class DocumentBuilder {

    private static final String BAR = "==========================================================\n";
    private static final String LINE = "----------------------------------------------------------\n";

    private DocumentBuilder() {
    }

    private static final int WIDTH = 58;

    private static String row(String label, String value) {
        return String.format("%-18s: %s\n", label, value);
    }

    private static String center(String text) {
        return " ".repeat(Math.max(0, (WIDTH - text.length()) / 2)) + text + "\n";
    }

    private static String heading(String title) {
        return BAR + center("chb cars.ph") + center("Drive home your dream car.") + center("Car Dealership Management")
                + LINE + center(title) + BAR;
    }

    /** Word-wraps a paragraph to the width of the document and ends it with a blank line. */
    private static String paragraph(String text) {
        StringBuilder out = new StringBuilder();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() + word.length() + 1 > WIDTH && line.length() > 0) {
                out.append(line).append("\n");
                line.setLength(0);
            }
            line.append(line.length() == 0 ? "" : " ").append(word);
        }
        return out.append(line).append("\n\n").toString();
    }

    /** Opening greeting: "Dear NAME," followed by a short welcome paragraph. */
    private static String intro(String name, String message) {
        return "\nDear " + name + ",\n\n" + paragraph(message);
    }

    /** Closing message, a friendly sign-off and the name of the person who prepared the document. */
    private static String outro(String message, String preparedBy) {
        return LINE + "\n" + paragraph(message) + "Warm regards,\n" + preparedBy + "\nchb cars.ph - Drive home your dream car.\n"
                + BAR;
    }

    private static String currentUserName() {
        User me = Session.getUser();
        return me == null ? "The chb cars.ph Team" : me.getFullName();
    }

    /** The order details shared by the confirmation slip and the full transaction record. */
    private static String orderBody(Order o) {
        DataStore db = DataStore.get();
        Customer c = db.customers.findById(o.getCustomerId());
        Car car = db.cars.findById(o.getCarId());
        StringBuilder sb = new StringBuilder();
        sb.append(row("Order ID", o.getId()));
        sb.append(row("Date", o.getDateCreated()));
        sb.append(row("Prepared by", o.getStaffUsername()));
        sb.append(LINE).append("CUSTOMER\n");
        if (c != null) {
            sb.append(row("Name", c.getFullName()));
            sb.append(row("Contact", c.getContactNo()));
            sb.append(row("Address", c.getAddress()));
            sb.append(row(c.getIdType(), c.getIdNumber()));
        }
        sb.append(LINE).append("VEHICLE\n");
        if (car != null) {
            sb.append(row("Car", car.getTitle()));
            sb.append(row("Color / Body", car.getColor() + " / " + car.getBodyType()));
            sb.append(row("Chassis No.", car.getChassisNo()));
        }
        sb.append(LINE).append("PRICE\n");
        sb.append(row("Car price", Format.money(o.getCarPrice())));
        sb.append(row("Charges & fees", Format.money(o.getCharges())));
        sb.append(row("TOTAL", Format.money(o.getTotal())));
        sb.append(LINE);
        sb.append(row("Payment method", o.getPaymentMethod()));
        sb.append(row("Order status", o.getOrderStatus()));
        sb.append(row("Payment status", o.getPaymentStatus()));
        sb.append(LINE);
        return sb.toString();
    }

    public static String orderSlip(Order o) {
        DataStore db = DataStore.get();
        String customer = db.customerName(o.getCustomerId());
        StringBuilder sb = new StringBuilder(heading("ORDER CONFIRMATION SLIP"));
        sb.append(intro(customer, "Thank you for choosing chb cars.ph! We are delighted to help you take home your "
                + db.carTitle(o.getCarId()) + ". This slip confirms that your order was created. Please keep it "
                + "for your next visit."));
        sb.append(orderBody(o));
        sb.append("NOTE: This slip only confirms that an order was created.\n");
        sb.append("It is NOT proof of payment. Financing orders remain\n");
        sb.append("subject to approval and payment conditions.\n\n");
        sb.append(outro("Please bring this slip and a valid ID whenever you pay or claim your vehicle. If you have "
                + "any questions, our agents will gladly assist you. We look forward to seeing you behind the wheel!",
                o.getStaffUsername()));
        return sb.toString();
    }

    public static String receipt(Payment p) {
        DataStore db = DataStore.get();
        Order o = db.orders.findById(p.getOrderId());
        StringBuilder sb = new StringBuilder(heading("OFFICIAL PAYMENT RECEIPT"));
        sb.append(intro(o == null ? "Valued Customer" : db.customerName(o.getCustomerId()),
                "Thank you for your payment! We are happy to confirm that the amount below was received and "
                + "recorded in our system."));
        sb.append(row("Receipt No.", p.getReceiptNo()));
        sb.append(row("Payment ID", p.getId()));
        sb.append(row("Date", p.getDateRecorded()));
        sb.append(row("Order ID", p.getOrderId()));
        if (o != null) {
            sb.append(row("Customer", db.customerName(o.getCustomerId())));
            sb.append(row("Vehicle", db.carTitle(o.getCarId())));
        }
        sb.append(LINE);
        sb.append(row("Method", p.getMethodName()));
        sb.append(row("Reference", p.getReference().isEmpty() ? "-" : p.getReference()));
        sb.append(row("AMOUNT PAID", Format.money(p.getAmount())));
        sb.append(row("Received by", p.getRecordedBy()));
        if (o != null) {
            double paid = new PaymentService().confirmedAmount(o.getId());
            sb.append(LINE);
            sb.append(row("Order total", Format.money(o.getTotal())));
            sb.append(row("Total paid so far", Format.money(paid)));
            sb.append(row("Balance", Format.money(Math.max(0, o.getTotal() - paid))));
        }
        sb.append(outro("Please keep this official receipt as your proof of payment. Thank you for trusting "
                + "chb cars.ph - we truly appreciate your business and wish you safe travels ahead!", p.getRecordedBy()));
        return sb.toString();
    }

    public static String handoverForm(Handover h) {
        DataStore db = DataStore.get();
        StringBuilder sb = new StringBuilder(heading("VEHICLE HANDOVER FORM"));
        sb.append(intro(h.getRecipientName(), "Congratulations on your new vehicle! It was a pleasure serving you. "
                + "This form records that your car was released to you in good order."));
        sb.append(row("Handover ID", h.getId()));
        sb.append(row("Date released", h.getDateReleased()));
        sb.append(row("Order ID", h.getOrderId()));
        sb.append(row("Vehicle", db.carTitle(h.getCarId())));
        Car car = db.cars.findById(h.getCarId());
        if (car != null) {
            sb.append(row("Chassis No.", car.getChassisNo()));
        }
        sb.append(row("Customer", db.customerName(h.getCustomerId())));
        sb.append(row("Released to", h.getRecipientName()));
        sb.append(row("Released by", h.getStaffUsername()));
        sb.append(row("Notes", h.getNotes().isEmpty() ? "-" : h.getNotes()));
        sb.append(LINE).append("\n\n_____________________        _____________________\n");
        sb.append("  Recipient signature           Staff signature\n\n");
        sb.append(outro("Please check the vehicle and its documents before you drive away. Thank you for choosing "
                + "chb cars.ph - drive safe and enjoy every mile!", h.getStaffUsername()));
        return sb.toString();
    }

    /** Everything about one order: slip, payments, receipts and the handover. */
    public static String transactionDetails(Order o) {
        DataStore db = DataStore.get();
        StringBuilder sb = new StringBuilder(heading("TRANSACTION RECORD"));
        sb.append(intro(currentUserName(), "Here is the complete record of this order: the order details, every "
                + "payment made and the vehicle handover."));
        sb.append(orderBody(o));
        sb.append("PAYMENT RECORDS\n").append(LINE);
        List<Payment> payments = db.payments.findByOrder(o.getId());
        if (payments.isEmpty()) {
            sb.append("No payments recorded yet.\n");
        }
        for (Payment p : payments) {
            sb.append(String.format("%s | %-13s | %12s | %-20s | %s\n", p.getId(), p.getMethodName(),
                    Format.money(p.getAmount()), p.getStatus(), p.getReceiptNo().isEmpty() ? "no receipt" : p.getReceiptNo()));
        }
        Handover h = db.handovers.findByOrder(o.getId());
        sb.append("\nHANDOVER\n").append(LINE);
        sb.append(h == null ? "Not yet released.\n" : h.getId() + " | released to " + h.getRecipientName()
                + " on " + h.getDateReleased() + " by " + h.getStaffUsername() + "\n");
        sb.append("\n").append(outro("This is the end of the record. Generated on " + Format.now() + ". Thank you for "
                + "keeping our records accurate and our customers happy!", currentUserName()));
        return sb.toString();
    }

    public static String customerHistory(Customer c) {
        DataStore db = DataStore.get();
        StringBuilder sb = new StringBuilder(heading("CUSTOMER PURCHASE HISTORY"));
        sb.append(intro(currentUserName(), "Here is the purchase history of " + c.getFullName()
                + ", a valued chb cars.ph customer since " + c.getDateRegistered() + "."));
        sb.append(row("Customer ID", c.getId()));
        sb.append(row("Name", c.getFullName()));
        sb.append(row("Contact", c.getContactNo()));
        sb.append(row("Email", c.getEmail().isEmpty() ? "-" : c.getEmail()));
        sb.append(row("Address", c.getAddress()));
        sb.append(row(c.getIdType(), c.getIdNumber()));
        sb.append(row("Registered", c.getDateRegistered()));
        sb.append(LINE).append("ORDERS\n");
        List<Order> orders = db.orders.findByCustomer(c.getId());
        if (orders.isEmpty()) {
            sb.append("No orders yet.\n");
        }
        for (Order o : orders) {
            sb.append(String.format("%s | %-28s | %13s | %-17s | %s\n", o.getId(), db.carTitle(o.getCarId()),
                    Format.money(o.getTotal()), o.getOrderStatus(), o.getPaymentStatus()));
        }
        User me = Session.getUser();
        sb.append(LINE).append("Generated by: ").append(me == null ? "-" : me.getUsername()).append(" on ")
                .append(Format.now()).append("\n\n");
        sb.append(outro("Thank you for helping us take great care of our customers. Every happy driver starts "
                + "with a great conversation!", currentUserName()));
        return sb.toString();
    }
}
