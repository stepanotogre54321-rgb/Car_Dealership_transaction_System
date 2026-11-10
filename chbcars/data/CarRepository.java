package chbcars.data;

import chbcars.model.Car;
import java.util.ArrayList;
import java.util.List;

/** Car stock saved in data/stocks.txt. */
public class CarRepository extends Repository<Car> {

    public CarRepository() {
        super("stocks.txt", "CAR");
    }

    @Override
    protected Car parse(String[] fields) {
        return Car.fromFields(fields);
    }

    public List<Car> findByStatus(String status) {
        List<Car> result = new ArrayList<>();
        for (Car c : getAll()) {
            if (c.getStatus().equals(status)) {
                result.add(c);
            }
        }
        return result;
    }

    public boolean chassisExists(String chassisNo, String exceptCarId) {
        for (Car c : getAll()) {
            if (c.getChassisNo().equalsIgnoreCase(chassisNo) && !c.getId().equals(exceptCarId)) {
                return true;
            }
        }
        return false;
    }
}
