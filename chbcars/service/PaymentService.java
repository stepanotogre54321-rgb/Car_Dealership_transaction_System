package chbcars.service;

import chbcars.data.DataStore;
import chbcars.data.Session;
import chbcars.model.Order;
import chbcars.model.Payment;
import chbcars.model.User;
import chbcars.util.Format;
import chbcars.util.ValidationException;
import java.util.List;

/** Recording, verifying and refunding payments; keeps the order's payment status correct. */
public class PaymentService {

    private final DataStore db = DataStore.get();

    public double confirmedAmount(String orderId) {
        return sum(orderId, Payment.PAID);
    }

    public double pendingAmount(String orderId) {
        return sum(orderId, Payment.PENDING_VERIFICATION);
    }

    private double sum(String orderId, String status) {
        double total = 0;
        for (Payment p : db.payments.findByOrder(orderId)) {
            if (p.getStatus().equals(status)) {
                total += p.getAmount();
            }
        }
        return total;
    }

    /** How much can still be paid on this order. */
    public double amountDue(Order order) {
        return Math.max(0, order.getTotal() - confirmedAmount(order.getId()) - pendingAmount(order.getId()));
    }

    public Payment record(Order selected, double amount, String reference) throws ValidationException {
        User staff = Session.getUser();
        if (staff == null) {
            throw new ValidationException("Please log in first.");
        }
        Order order = db.orders.findById(selected.getId());
        checkOrderCanReceivePayment(order);

        double due = amountDue(order);
        if (amount <= 0) {
            throw new ValidationException("Amount must be greater than 0.");
        }
        if (amount > due + 0.005) {
            throw new ValidationException("Amount is higher than the amount due (" + Format.money(due) + ").");
        }
        Payment payment = Payment.forMethod(order.getPaymentMethod());
        String ref = reference == null ? "" : reference.trim();
        payment.validateReference(ref);
        payment.setOrderId(order.getId());
        payment.setAmount(amount);
        payment.setReference(ref);
        payment.setRecordedBy(staff.getUsername());
        payment.setDateRecorded(Format.now());
        payment.setStatus(Payment.PENDING_VERIFICATION);
        db.payments.add(payment);

        if (!payment.requiresVerification()) {
            confirm(payment);
        }
        recompute(order);
        db.log("Payment Recorded", payment.getId() + " for " + order.getId() + " (" + Format.money(amount) + ")");
        return payment;
    }

    private void checkOrderCanReceivePayment(Order order) throws ValidationException {
        if (order == null) {
            throw new ValidationException("The order could not be found.");
        }
        switch (order.getOrderStatus()) {
            case Order.AWAITING_PAYMENT:
                return;
            case Order.UNDER_REVIEW:
            case Order.PENDING:
                throw new ValidationException("This order is still under review. The admin must approve it first.");
            case Order.ON_HOLD:
                throw new ValidationException("This order is on hold.");
            case Order.READY:
                throw new ValidationException("This order is already fully paid.");
            default:
                throw new ValidationException("This order is " + order.getOrderStatus().toLowerCase() + ".");
        }
    }

    /** Simulated verification of a bank transfer / financing release. Returns true when confirmed. */
    public boolean verify(Payment selected, boolean approve) throws ValidationException {
        Payment payment = db.payments.findById(selected.getId());
        if (payment == null || !Payment.PENDING_VERIFICATION.equals(payment.getStatus())) {
            throw new ValidationException("Only payments that are pending verification can be verified.");
        }
        boolean ok = approve && !payment.getReference().toUpperCase().contains("FAIL");
        if (ok) {
            confirm(payment);
        } else {
            payment.setStatus(Payment.FAILED);
            db.payments.update(payment);
        }
        Order order = db.orders.findById(payment.getOrderId());
        if (order != null) {
            recompute(order);
        }
        db.log(ok ? "Payment Verified" : "Payment Failed", payment.getId());
        return ok;
    }

    public void refund(Payment selected) throws ValidationException {
        User me = Session.getUser();
        if (me == null || !me.canApproveOrders()) {
            throw new ValidationException("Only the admin can refund a payment.");
        }
        Payment payment = db.payments.findById(selected.getId());
        if (payment == null || !Payment.PAID.equals(payment.getStatus())) {
            throw new ValidationException("Only confirmed (Paid) payments can be refunded.");
        }
        Order order = db.orders.findById(payment.getOrderId());
        if (order != null && Order.COMPLETED.equals(order.getOrderStatus())) {
            throw new ValidationException("The car was already released, so this payment cannot be refunded.");
        }
        payment.setStatus(Payment.REFUNDED);
        db.payments.update(payment);
        if (order != null) {
            recompute(order);
        }
        db.log("Payment Refunded", payment.getId() + " (simulated)");
    }

    private void confirm(Payment payment) {
        payment.setStatus(Payment.PAID);
        payment.setReceiptNo(payment.getId().replace("PAY", "RCT"));
        db.payments.update(payment);
    }

    /** Recalculates the order's payment status (and moves the order to Ready for Release when fully paid). */
    public void recompute(Order order) {
        double confirmed = 0;
        double pending = 0;
        int refunded = 0;
        int failed = 0;
        List<Payment> list = db.payments.findByOrder(order.getId());
        for (Payment p : list) {
            switch (p.getStatus()) {
                case Payment.PAID: confirmed += p.getAmount(); break;
                case Payment.PENDING_VERIFICATION: pending += p.getAmount(); break;
                case Payment.REFUNDED: refunded++; break;
                case Payment.FAILED: failed++; break;
                default: break;
            }
        }
        String status;
        if (confirmed >= order.getTotal() - 0.005) {
            status = Order.PAID;
        } else if (confirmed > 0) {
            status = Order.PARTIALLY_PAID;
        } else if (pending > 0) {
            status = Order.PAY_PENDING;
        } else if (refunded > 0) {
            status = Order.REFUNDED;
        } else if (failed > 0) {
            status = Order.FAILED;
        } else {
            status = Order.UNPAID;
        }
        order.setPaymentStatus(status);
        String current = order.getOrderStatus();
        if (Order.AWAITING_PAYMENT.equals(current) || Order.READY.equals(current)) {
            order.setOrderStatus(Order.PAID.equals(status) ? Order.READY : Order.AWAITING_PAYMENT);
        }
        db.orders.update(order);
    }
}
