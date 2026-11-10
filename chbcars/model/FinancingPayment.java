package chbcars.model;

import chbcars.util.ValidationException;

/** Financing is released by a (simulated) lender after approval and verification. */
public class FinancingPayment extends Payment {

    public static final double PROCESSING_RATE = 0.03;

    @Override
    public String getMethodName() {
        return FINANCING;
    }

    @Override
    public boolean requiresVerification() {
        return true;
    }

    @Override
    public double calculateCharge(double carPrice) {
        return Math.round(carPrice * PROCESSING_RATE * 100.0) / 100.0;
    }

    @Override
    public void validateReference(String reference) throws ValidationException {
        if (reference == null || !reference.trim().matches("[A-Za-z0-9-]{8,25}")) {
            throw new ValidationException("Enter the financing application / release number (8-25 letters/numbers).");
        }
    }
}
