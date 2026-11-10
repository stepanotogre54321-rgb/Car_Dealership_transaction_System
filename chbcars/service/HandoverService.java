package chbcars.service;

import chbcars.data.DataStore;
import chbcars.data.Session;
import chbcars.model.Car;
import chbcars.model.Handover;
import chbcars.model.Order;
import chbcars.model.User;
import chbcars.util.Format;
import chbcars.util.ValidationException;
import chbcars.util.Validator;

/** Vehicle release. Staff cannot release a car before every condition is met. */
public class HandoverService {

    private final DataStore db = DataStore.get();

    public Handover release(Order selected, String recipient, String notes,
            boolean idChecked, boolean carChecked) throws ValidationException {
        User staff = Session.getUser();
        if (staff == null) {
            throw new ValidationException("Please log in first.");
        }
        Order order = db.orders.findById(selected.getId());
        if (order == null) {
            throw new ValidationException("The order could not be found.");
        }
        checkReleaseConditions(order);
        if (!idChecked || !carChecked) {
            throw new ValidationException("Please complete the whole release checklist first.");
        }
        Car car = db.cars.findById(order.getCarId());
        if (car == null || !Car.RESERVED.equals(car.getStatus())) {
            throw new ValidationException("The reserved car for this order could not be found.");
        }
        Handover handover = new Handover();
        handover.setOrderId(order.getId());
        handover.setCarId(car.getId());
        handover.setCustomerId(order.getCustomerId());
        handover.setRecipientName(Validator.requireName(recipient, "Recipient name"));
        handover.setStaffUsername(staff.getUsername());
        handover.setDateReleased(Format.now());
        handover.setNotes(Validator.optionalText(notes, "Notes", 200));
        db.handovers.add(handover);

        car.setStatus(Car.RELEASED);
        db.cars.update(car);
        order.setOrderStatus(Order.COMPLETED);
        db.orders.update(order);
        db.log("Vehicle Released", order.getId() + " - " + car.getTitle() + " to " + handover.getRecipientName());
        return handover;
    }

    private void checkReleaseConditions(Order order) throws ValidationException {
        if (!Order.READY.equals(order.getOrderStatus())) {
            throw new ValidationException("This order is not ready for release (status: " + order.getOrderStatus() + ").");
        }
        if (!Order.PAID.equals(order.getPaymentStatus())) {
            throw new ValidationException("The vehicle cannot be released until the payment is fully confirmed.");
        }
        if (db.handovers.findByOrder(order.getId()) != null) {
            throw new ValidationException("This order already has a handover record.");
        }
    }
}
