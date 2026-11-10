package chbcars.data;

import chbcars.model.Customer;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository extends Repository<Customer> {

    public CustomerRepository() {
        super("customers.txt", "CUS");
    }

    @Override
    protected Customer parse(String[] fields) {
        return Customer.fromFields(fields);
    }

    /** Matches name, contact number, ID or e-mail. */
    public List<Customer> search(String keyword) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Customer> result = new ArrayList<>();
        for (Customer c : getAll()) {
            String text = (c.getId() + " " + c.getFullName() + " " + c.getContactNo() + " " + c.getEmail()).toLowerCase();
            if (key.isEmpty() || text.contains(key)) {
                result.add(c);
            }
        }
        return result;
    }
}
