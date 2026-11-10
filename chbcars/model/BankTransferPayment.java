package chbcars.model;

import chbcars.util.ValidationException;

/** Bank transfers need a reference number and a (simulated) verification. */
public class BankTransferPayment extends Payment {

    public static final double BANK_HANDLING_FEE = 500.0;

    @Override
    public String getMethodName() {
        return BANK;
    }

    @Override
    public boolean requiresVerification() {
        return true;
    }

    @Override
    public double calculateCharge(double carPrice) {
        return BANK_HANDLING_FEE;
    }

    @Override
    public void validateReference(String reference) throws ValidationException {
        if (reference == null || !reference.trim().matches("[A-Za-z0-9-]{8,25}")) {
            throw new ValidationException("Enter the bank reference number (8-25 letters/numbers).");
        }
    }
}
