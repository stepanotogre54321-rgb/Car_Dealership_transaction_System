package chbcars.model;

import chbcars.util.FileHandler;

/** Record of a car being released to its owner. */
public class Handover extends Entity {

    private String orderId = "";
    private String carId = "";
    private String customerId = "";
    private String recipientName = "";
    private String staffUsername = "";
    private String dateReleased = "";
    private String notes = "";

    public Handover() {
        super("");
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getStaffUsername() { return staffUsername; }
    public void setStaffUsername(String staffUsername) { this.staffUsername = staffUsername; }
    public String getDateReleased() { return dateReleased; }
    public void setDateReleased(String dateReleased) { this.dateReleased = dateReleased; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String getSummary() {
        return getId() + " | Order " + orderId + " | released to " + recipientName;
    }

    @Override
    public String toFileLine() {
        return FileHandler.join(getId(), orderId, carId, customerId, recipientName, staffUsername, dateReleased, notes);
    }

    public static Handover fromFields(String[] f) {
        Handover h = new Handover();
        h.setId(f[0]);
        h.orderId = f[1];
        h.carId = f[2];
        h.customerId = f[3];
        h.recipientName = f[4];
        h.staffUsername = f[5];
        h.dateReleased = f[6];
        h.notes = f[7];
        return h;
    }
}
