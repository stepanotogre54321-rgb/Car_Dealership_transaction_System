package chbcars.data;

import chbcars.model.ActivityLog;
import chbcars.model.Admin;
import chbcars.model.Car;
import chbcars.model.Customer;
import chbcars.model.User;
import chbcars.util.ErrorHandler;
import chbcars.util.FileHandler;
import chbcars.util.Format;
import chbcars.util.PasswordUtil;
import java.io.File;

/** Single access point to every repository (one object shared by the whole program). */
public final class DataStore {

    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "202600000";

    private static final DataStore INSTANCE = new DataStore();

    public final UserRepository users = new UserRepository();
    public final CarRepository cars = new CarRepository();
    public final CustomerRepository customers = new CustomerRepository();
    public final OrderRepository orders = new OrderRepository();
    public final PaymentRepository payments = new PaymentRepository();
    public final HandoverRepository handovers = new HandoverRepository();
    public final LogRepository logs = new LogRepository();
    public final AppSettings settings = new AppSettings("settings.txt");

    private DataStore() {
    }

    public static DataStore get() {
        return INSTANCE;
    }

    /** Creates the data folders and .txt files at runtime. Returns true on the very first run. */
    public boolean initialize() {
        new File(FileHandler.DATA_DIR).mkdirs();
        new File(FileHandler.IMAGE_DIR).mkdirs();
        new File(FileHandler.DOC_DIR).mkdirs();
        users.ensureStorage();
        cars.ensureStorage();
        customers.ensureStorage();
        orders.ensureStorage();
        payments.ensureStorage();
        handovers.ensureStorage();
        logs.ensureStorage();
        settings.load();

        if (users.getAll().isEmpty()) {
            Admin admin = new Admin("");
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setFullName("System Administrator");
            admin.setContactNo("09170000000");
            admin.setPasswordHash(PasswordUtil.hash(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD));
            admin.setStatus(User.ACTIVE);
            admin.setMustChangePassword(true);
            admin.setDateCreated(Format.now());
            users.add(admin);
            return true;
        }
        return false;
    }

    /** Saves an activity-log entry for the current user. Never stops the real action if it fails. */
    public void log(String action, String details) {
        try {
            User user = Session.getUser();
            String name = user == null ? "system" : user.getUsername();
            String role = user == null ? "-" : user.getRole();
            logs.add(new ActivityLog(Format.now(), name, role, action, details));
        } catch (RuntimeException e) {
            ErrorHandler.logError(e);
        }
    }

    // ---- small lookups used by many screens ----
    public String customerName(String customerId) {
        Customer c = customers.findById(customerId);
        return c == null ? "-" : c.getFullName();
    }

    public String carTitle(String carId) {
        Car c = cars.findById(carId);
        return c == null ? "-" : c.getTitle();
    }
}
