package chbcars.data;

import chbcars.model.Payment;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository extends Repository<Payment> {

    public PaymentRepository() {
        super("payments.txt", "PAY");
    }

    @Override
    protected Payment parse(String[] fields) {
        return Payment.fromFields(fields);
    }

    public List<Payment> findByOrder(String orderId) {
        List<Payment> result = new ArrayList<>();
        for (Payment p : getAll()) {
            if (p.getOrderId().equals(orderId)) {
                result.add(p);
            }
        }
        return result;
    }
}
