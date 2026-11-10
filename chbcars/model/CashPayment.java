package chbcars.model;

import chbcars.util.ValidationException;

/** Cash is received at the counter, so it is confirmed immediately. */
public class CashPayment extends Payment {

    @Override
    public String getMethodName() {
        return CASH;
    }

    @Override
    public boolean requiresVerification() {
        return false;
    }

    @Override
    public double calculateCharge(double carPrice) {
        return 0;
    }

    @Override
    public void validateReference(String reference) throws ValidationException {
        if (reference != null && reference.length() > 30) {
            throw new ValidationException("Official receipt / note must not exceed 30 characters.");
        }
    }
}
