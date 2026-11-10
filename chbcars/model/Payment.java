package chbcars.model;

import chbcars.util.FileHandler;
import chbcars.util.Format;
import chbcars.util.ValidationException;

/**
 * ABSTRACTION + POLYMORPHISM: a payment is Cash, Bank Transfer or Financing.
 * Each subclass has its own charges and its own verification rules.
 */
public abstract class Payment extends Entity {

    public static final String CASH = "Cash";
    public static final String BANK = "Bank Transfer";
    public static final String FINANCING = "Financing";
    public static final String[] METHODS = {CASH, BANK, FINANCING};

    public static final String PENDING_VERIFICATION = "Pending Verification";
    public static final String PAID = "Paid";
    public static final String FAILED = "Failed";
    public static final String REFUNDED = "Refunded";

    private String orderId = "";
    private double amount;
    private String status = PENDING_VERIFICATION;
    private String reference = "";
    private String receiptNo = "";
    private String recordedBy = "";
    private String dateRecorded = "";

    protected Payment() {
        super("");
    }

    // ---- rules each payment type must define ----
    public abstract String getMethodName();

    public abstract boolean requiresVerification();

    /** Extra charge for using this payment method. */
    public abstract double calculateCharge(double carPrice);

    /** Throws when the reference/proof entered by the staff is not acceptable. */
    public abstract void validateReference(String reference) throws ValidationException;

    /** Creates the correct payment object for a method name. */
    public static Payment forMethod(String method) {
        if (BANK.equals(method)) {
            return new BankTransferPayment();
        }
        if (FINANCING.equals(method)) {
            return new FinancingPayment();
        }
        return new CashPayment();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public String getDateRecorded() { return dateRecorded; }
    public void setDateRecorded(String dateRecorded) { this.dateRecorded = dateRecorded; }

    @Override
    public String getSummary() {
        return getId() + " | " + getMethodName() + " | " + Format.money(amount) + " | " + status;
    }

    @Override
    public String toFileLine() {
        return FileHandler.join(getId(), getMethodName(), orderId, amount, status, reference, receiptNo,
                recordedBy, dateRecorded);
    }

    public static Payment fromFields(String[] f) {
        Payment p = forMethod(f[1]);
        p.setId(f[0]);
        p.orderId = f[2];
        p.amount = Double.parseDouble(f[3]);
        p.status = f[4];
        p.reference = f[5];
        p.receiptNo = f[6];
        p.recordedBy = f[7];
        p.dateRecorded = f[8];
        return p;
    }
}
