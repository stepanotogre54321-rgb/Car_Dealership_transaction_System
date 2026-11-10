package chbcars.service;

import chbcars.data.DataStore;
import chbcars.data.Session;
import chbcars.model.Car;
import chbcars.model.Customer;
import chbcars.model.Order;
import chbcars.model.Payment;
import chbcars.model.User;
import chbcars.util.Format;
import chbcars.util.ValidationException;

/** Creating orders and the admin's approval, hold and cancellation powers. */
public class OrderService {

    private final DataStore db = DataStore.get();
    private final PaymentService paymentService = new PaymentService();

    /** Documentation fee + the payment method's own charge. */
    public static double calculateCharges(Car car, String method) {
        return Order.DOCUMENTATION_FEE + Payment.forMethod(method).calculateCharge(car.getPrice());
    }

    public Order createOrder(Customer customer, Car selectedCar, String method, String notes)
            throws ValidationException {
        User staff = Session.getUser();
        if (staff == null) {
            throw new ValidationException("Please log in first.");
        }
        if (customer == null) {
            throw new ValidationException("Customer information is required.");
        }
        if (selectedCar == null) {
            throw new ValidationException("Please select a car for the customer.");
        }
        Car car = db.cars.findById(selectedCar.getId());
        if (car == null || !car.isAvailable()) {
            throw new ValidationException("This car is no longer available. Please choose another one.");
        }
        if (customer.getId() == null || customer.getId().isEmpty()) {
            db.customers.add(customer);
        }
        Order order = new Order();
        order.setCustomerId(customer.getId());
        order.setCarId(car.getId());
        order.setStaffUsername(staff.getUsername());
        order.setPaymentMethod(method);
        order.setCarPrice(car.getPrice());
        order.setCharges(calculateCharges(car, method));
        order.setTotal(order.getCarPrice() + order.getCharges());
        order.setOrderStatus(Payment.FINANCING.equals(method) ? Order.UNDER_REVIEW : Order.AWAITING_PAYMENT);
        order.setPaymentStatus(Order.UNPAID);
        order.setDateCreated(Format.now());
        order.setNotes(notes == null ? "" : notes.trim());
        db.orders.add(order);

        car.setStatus(Car.RESERVED);
        db.cars.update(car);
        db.log("Order Created", order.getId() + " - " + car.getTitle() + " for " + customer.getFullName());
        return order;
    }

    // ---------------------------------------------------------- admin powers
    public void approve(Order selected) throws ValidationException {
        requireApprover();
        Order order = fresh(selected);
        if (!Order.UNDER_REVIEW.equals(order.getOrderStatus()) && !Order.PENDING.equals(order.getOrderStatus())) {
            throw new ValidationException("Only orders that are under review can be approved.");
        }
        order.setOrderStatus(Order.AWAITING_PAYMENT);
        paymentService.recompute(order);
        db.log("Order Approved", order.getId());
    }

    public void hold(Order selected) throws ValidationException {
        requireApprover();
        Order order = fresh(selected);
        if (!order.isActive() || Order.ON_HOLD.equals(order.getOrderStatus())) {
            throw new ValidationException("This order cannot be put on hold.");
        }
        order.setOrderStatus(Order.ON_HOLD);
        db.orders.update(order);
        db.log("Order On Hold", order.getId());
    }

    public void resume(Order selected) throws ValidationException {
        requireApprover();
        Order order = fresh(selected);
        if (!Order.ON_HOLD.equals(order.getOrderStatus())) {
            throw new ValidationException("Only orders that are on hold can be resumed.");
        }
        order.setOrderStatus(Order.AWAITING_PAYMENT);
        paymentService.recompute(order);
        db.log("Order Resumed", order.getId());
    }

    /** Cancels the order, frees the car and (simulated) refunds confirmed payments. */
    public void cancel(Order selected) throws ValidationException {
        requireApprover();
        Order order = fresh(selected);
        if (!order.isActive()) {
            throw new ValidationException("This order is already " + order.getOrderStatus().toLowerCase() + ".");
        }
        for (Payment p : db.payments.findByOrder(order.getId())) {
            if (Payment.PAID.equals(p.getStatus())) {
                p.setStatus(Payment.REFUNDED);
                db.payments.update(p);
            } else if (Payment.PENDING_VERIFICATION.equals(p.getStatus())) {
                p.setStatus(Payment.FAILED);
                db.payments.update(p);
            }
        }
        order.setOrderStatus(Order.CANCELLED);
        paymentService.recompute(order);

        Car car = db.cars.findById(order.getCarId());
        if (car != null && Car.RESERVED.equals(car.getStatus())) {
            car.setStatus(Car.AVAILABLE);
            db.cars.update(car);
        }
        db.log("Order Cancelled", order.getId());
    }

    private Order fresh(Order selected) throws ValidationException {
        Order order = selected == null ? null : db.orders.findById(selected.getId());
        if (order == null) {
            throw new ValidationException("Please select an order first.");
        }
        return order;
    }

    private void requireApprover() throws ValidationException {
        User me = Session.getUser();
        if (me == null || !me.canApproveOrders()) {
            throw new ValidationException("Only the admin can approve, hold or cancel orders.");
        }
    }
}
