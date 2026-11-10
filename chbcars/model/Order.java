package chbcars.model;

import chbcars.util.FileHandler;
import chbcars.util.Format;

/** Links a customer to a car. Order status and payment status are stored separately. */
public class Order extends Entity {

    public static final double DOCUMENTATION_FEE = 15000.0;

    // Order statuses
    public static final String PENDING = "Pending";
    public static final String UNDER_REVIEW = "Under Review";
    public static final String AWAITING_PAYMENT = "Awaiting Payment";
    public static final String READY = "Ready for Release";
    public static final String COMPLETED = "Completed";
    public static final String CANCELLED = "Cancelled";
    public static final String ON_HOLD = "On Hold";

    // Payment statuses
    public static final String UNPAID = "Unpaid";
    public static final String PAY_PENDING = "Pending Verification";
    public static final String PARTIALLY_PAID = "Partially Paid";
    public static final String PAID = "Paid";
    public static final String FAILED = "Failed";
    public static final String REFUNDED = "Refunded";

    private String customerId = "";
    private String carId = "";
    private String staffUsername = "";
    private String paymentMethod = "";
    private double carPrice;
    private double charges;
    private double total;
    private String orderStatus = AWAITING_PAYMENT;
    private String paymentStatus = UNPAID;
    private String dateCreated = "";
    private String notes = "";

    public Order() {
        super("");
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public String getStaffUsername() { return staffUsername; }
    public void setStaffUsername(String staffUsername) { this.staffUsername = staffUsername; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public double getCarPrice() { return carPrice; }
    public void setCarPrice(double carPrice) { this.carPrice = carPrice; }
    public double getCharges() { return charges; }
    public void setCharges(double charges) { this.charges = charges; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getDateCreated() { return dateCreated; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /** An order is "active" while it can still change (not finished or cancelled). */
    public boolean isActive() {
        return !COMPLETED.equals(orderStatus) && !CANCELLED.equals(orderStatus);
    }

    @Override
    public String getSummary() {
        return getId() + " | " + Format.money(total) + " | " + orderStatus;
    }

    @Override
    public String toFileLine() {
        return FileHandler.join(getId(), customerId, carId, staffUsername, paymentMethod, carPrice, charges,
                total, orderStatus, paymentStatus, dateCreated, notes);
    }

    public static Order fromFields(String[] f) {
        Order o = new Order();
        o.setId(f[0]);
        o.customerId = f[1];
        o.carId = f[2];
        o.staffUsername = f[3];
        o.paymentMethod = f[4];
        o.carPrice = Double.parseDouble(f[5]);
        o.charges = Double.parseDouble(f[6]);
        o.total = Double.parseDouble(f[7]);
        o.orderStatus = f[8];
        o.paymentStatus = f[9];
        o.dateCreated = f[10];
        o.notes = f[11];
        return o;
    }
}
