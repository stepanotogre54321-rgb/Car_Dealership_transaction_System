package chbcars.data;

import chbcars.model.Order;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository extends Repository<Order> {

    public OrderRepository() {
        super("orders.txt", "ORD");
    }

    @Override
    protected Order parse(String[] fields) {
        return Order.fromFields(fields);
    }

    public List<Order> findByCustomer(String customerId) {
        List<Order> result = new ArrayList<>();
        for (Order o : getAll()) {
            if (o.getCustomerId().equals(customerId)) {
                result.add(o);
            }
        }
        return result;
    }

    public List<Order> findByOrderStatus(String status) {
        List<Order> result = new ArrayList<>();
        for (Order o : getAll()) {
            if (o.getOrderStatus().equals(status)) {
                result.add(o);
            }
        }
        return result;
    }

    public boolean carHasOrders(String carId) {
        for (Order o : getAll()) {
            if (o.getCarId().equals(carId)) {
                return true;
            }
        }
        return false;
    }
}
