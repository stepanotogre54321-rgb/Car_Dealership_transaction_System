package chbcars.data;

import chbcars.model.Handover;

public class HandoverRepository extends Repository<Handover> {

    public HandoverRepository() {
        super("handovers.txt", "HOV");
    }

    @Override
    protected Handover parse(String[] fields) {
        return Handover.fromFields(fields);
    }

    public Handover findByOrder(String orderId) {
        for (Handover h : getAll()) {
            if (h.getOrderId().equals(orderId)) {
                return h;
            }
        }
        return null;
    }
}
