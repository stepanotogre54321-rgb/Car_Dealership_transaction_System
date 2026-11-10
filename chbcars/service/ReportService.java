package chbcars.service;

import chbcars.data.DataStore;
import chbcars.data.Session;
import chbcars.model.ActivityLog;
import chbcars.model.Car;
import chbcars.model.Handover;
import chbcars.model.Order;
import chbcars.model.Payment;
import chbcars.model.User;
import chbcars.util.Format;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Dashboard numbers and the admin's reports. */
public class ReportService {

    public static final String[] REPORT_TYPES = {"Sales Report", "Inventory Report", "Order Report",
        "Payment Report", "Staff Activity Report"};

    public static final String[] CHART_MODES = {"Last 7 days", "Last 6 months", "Last 4 years"};

    private final DataStore db = DataStore.get();

    // ------------------------------------------------------ dashboard numbers
    public Map<String, String> adminStats() {
        List<Car> cars = db.cars.getAll();
        List<Order> orders = db.orders.getAll();
        int available = 0;
        for (Car c : cars) {
            if (c.isAvailable()) available++;
        }
        int active = 0;
        int completed = 0;
        int pendingPayments = 0;
        for (Order o : orders) {
            if (o.isActive()) {
                active++;
                if (!Order.PAID.equals(o.getPaymentStatus())) pendingPayments++;
            }
            if (Order.COMPLETED.equals(o.getOrderStatus())) completed++;
        }
        double revenue = 0;
        for (Payment p : db.payments.getAll()) {
            if (Payment.PAID.equals(p.getStatus())) revenue += p.getAmount();
        }
        Map<String, String> stats = new LinkedHashMap<>();
        stats.put("Total Cars", String.valueOf(cars.size()));
        stats.put("Available Vehicles", String.valueOf(available));
        stats.put("Active Orders", String.valueOf(active));
        stats.put("Completed Sales", String.valueOf(completed));
        stats.put("Pending Payments", String.valueOf(pendingPayments));
        stats.put("Confirmed Revenue", Format.money(revenue));
        return stats;
    }

    public Map<String, String> staffStats() {
        User me = Session.getUser();
        int available = 0;
        for (Car c : db.cars.getAll()) {
            if (c.isAvailable()) available++;
        }
        int mine = 0;
        int awaiting = 0;
        int needPayment = 0;
        int ready = 0;
        for (Order o : db.orders.getAll()) {
            if (me != null && o.getStaffUsername().equals(me.getUsername())) mine++;
            if (Order.UNDER_REVIEW.equals(o.getOrderStatus()) || Order.PENDING.equals(o.getOrderStatus())) awaiting++;
            if (Order.AWAITING_PAYMENT.equals(o.getOrderStatus())) needPayment++;
            if (Order.READY.equals(o.getOrderStatus())) ready++;
        }
        int toVerify = 0;
        for (Payment p : db.payments.getAll()) {
            if (Payment.PENDING_VERIFICATION.equals(p.getStatus())) toVerify++;
        }
        Map<String, String> stats = new LinkedHashMap<>();
        stats.put("Available Vehicles", String.valueOf(available));
        stats.put("My Orders", String.valueOf(mine));
        stats.put("Orders Awaiting Processing", String.valueOf(awaiting));
        stats.put("Pending Payments", String.valueOf(needPayment));
        stats.put("Payments To Verify", String.valueOf(toVerify));
        stats.put("Ready for Release", String.valueOf(ready));
        return stats;
    }

    // ------------------------------------------------------------ sales chart
    /**
     * Total value of completed sales for the admin's chart. The key is the label under the bar
     * (a day, a month or a year) and the value is the sum of the order totals released in it.
     */
    public Map<String, Double> salesChart(String mode) {
        Map<String, Handover> released = new HashMap<>();
        for (Handover h : db.handovers.getAll()) {
            released.put(h.getOrderId(), h);
        }
        Map<String, Double> totals = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("MMM d");
        DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("MMM yy");
        int slots = CHART_MODES[0].equals(mode) ? 7 : CHART_MODES[1].equals(mode) ? 6 : 4;
        for (int i = slots - 1; i >= 0; i--) {
            totals.put(chartLabel(mode, today, i, dayFormat, monthFormat), 0.0);
        }
        for (Order o : db.orders.getAll()) {
            if (!Order.COMPLETED.equals(o.getOrderStatus())) {
                continue;
            }
            Handover h = released.get(o.getId());
            String stamp = h != null ? h.getDateReleased() : o.getDateCreated();
            try {
                LocalDate date = LocalDate.parse(stamp.substring(0, 10));
                for (int i = 0; i < slots; i++) {
                    LocalDate slotDate = slotDate(mode, today, i);
                    if (sameSlot(mode, date, slotDate)) {
                        String label = chartLabel(mode, today, i, dayFormat, monthFormat);
                        totals.put(label, totals.get(label) + o.getTotal());
                    }
                }
            } catch (RuntimeException badDate) {
                // an order with an unreadable date is simply left out of the chart
            }
        }
        return totals;
    }

    private LocalDate slotDate(String mode, LocalDate today, int back) {
        if (CHART_MODES[0].equals(mode)) {
            return today.minusDays(back);
        }
        return CHART_MODES[1].equals(mode) ? today.minusMonths(back) : today.minusYears(back);
    }

    private boolean sameSlot(String mode, LocalDate date, LocalDate slot) {
        if (CHART_MODES[0].equals(mode)) {
            return date.equals(slot);
        }
        if (CHART_MODES[1].equals(mode)) {
            return date.getYear() == slot.getYear() && date.getMonth() == slot.getMonth();
        }
        return date.getYear() == slot.getYear();
    }

    private String chartLabel(String mode, LocalDate today, int back, DateTimeFormatter day, DateTimeFormatter month) {
        LocalDate d = slotDate(mode, today, back);
        if (CHART_MODES[0].equals(mode)) {
            return d.format(day);
        }
        return CHART_MODES[1].equals(mode) ? d.format(month) : String.valueOf(d.getYear());
    }

    // ---------------------------------------------------------------- reports
    public String generate(String type) {
        StringBuilder sb = new StringBuilder();
        User me = Session.getUser();
        String name = me == null ? "Administrator" : me.getFullName();
        sb.append("=".repeat(96)).append("\n");
        sb.append(centered("chb cars.ph")).append(centered("Drive home your dream car."));
        sb.append("-".repeat(96)).append("\n").append(centered(type.toUpperCase()));
        sb.append("=".repeat(96)).append("\n");
        sb.append("Generated : ").append(Format.now()).append("\n");
        sb.append("Prepared by: ").append(name).append(me == null ? "" : " (" + me.getUsername() + ")").append("\n\n");
        sb.append("Good day, ").append(name).append("!\n\n").append(introFor(type)).append("\n\n");
        sb.append("-".repeat(96)).append("\n");
        switch (type) {
            case "Sales Report": salesReport(sb); break;
            case "Inventory Report": inventoryReport(sb); break;
            case "Order Report": orderReport(sb); break;
            case "Payment Report": paymentReport(sb); break;
            default: staffReport(sb); break;
        }
        sb.append("\n").append("=".repeat(96)).append("\n");
        sb.append("Thank you for reviewing this report. It was generated automatically from the latest saved records\n");
        sb.append("on ").append(Format.now()).append(", so please double-check important figures before sharing them.\n");
        sb.append("We appreciate your hard work in keeping chb cars.ph running smoothly - keep the wheels turning!\n\n");
        sb.append("Respectfully yours,\nThe chb cars.ph Management System\n\n");
        sb.append(centered("*** END OF REPORT ***"));
        return sb.toString();
    }

    private String centered(String text) {
        return " ".repeat(Math.max(0, (96 - text.length()) / 2)) + text + "\n";
    }

    /** One friendly sentence or two explaining what the report contains. */
    private String introFor(String type) {
        switch (type) {
            case "Sales Report":
                return "Here is your sales report. It lists every completed sale together with the customer, the\n"
                        + "vehicle sold and the total value, followed by the overall sales figures.";
            case "Inventory Report":
                return "Here is your inventory report. It shows how many cars are available, reserved, unavailable or\n"
                        + "released, the value of the available stock and the full list of vehicles.";
            case "Order Report":
                return "Here is your order report. It counts the orders in every status and lists each order with its\n"
                        + "customer, vehicle, payment status and total.";
            case "Payment Report":
                return "Here is your payment report. It lists every recorded payment and summarizes what was confirmed,\n"
                        + "what is still waiting for verification and what was refunded.";
            default:
                return "Here is your staff activity report. It shows what each account has handled and the most recent\n"
                        + "actions recorded in the system.";
        }
    }

    private void salesReport(StringBuilder sb) {
        sb.append(Format.pad("ORDER", 10)).append(Format.pad("DATE", 18)).append(Format.pad("CUSTOMER", 24))
                .append(Format.pad("VEHICLE", 30)).append("TOTAL\n");
        double total = 0;
        int count = 0;
        for (Order o : db.orders.getAll()) {
            if (Order.COMPLETED.equals(o.getOrderStatus())) {
                sb.append(Format.pad(o.getId(), 10)).append(Format.pad(o.getDateCreated(), 18))
                        .append(Format.pad(db.customerName(o.getCustomerId()), 24))
                        .append(Format.pad(db.carTitle(o.getCarId()), 30)).append(Format.money(o.getTotal())).append("\n");
                total += o.getTotal();
                count++;
            }
        }
        if (count == 0) sb.append("No completed sales yet.\n");
        sb.append("-".repeat(96)).append("\nCompleted sales: ").append(count).append("\nTotal sales value: ")
                .append(Format.money(total)).append("\n");
    }

    private void inventoryReport(StringBuilder sb) {
        List<Car> cars = db.cars.getAll();
        int[] counts = new int[4];
        String[] names = {Car.AVAILABLE, Car.RESERVED, Car.UNAVAILABLE, Car.RELEASED};
        double stockValue = 0;
        for (Car c : cars) {
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(c.getStatus())) counts[i]++;
            }
            if (c.isAvailable()) stockValue += c.getPrice();
        }
        for (int i = 0; i < names.length; i++) {
            sb.append(Format.pad(names[i], 14)).append(": ").append(counts[i]).append("\n");
        }
        sb.append("Value of available stock: ").append(Format.money(stockValue)).append("\n").append("-".repeat(96)).append("\n");
        sb.append(Format.pad("CAR ID", 10)).append(Format.pad("VEHICLE", 34)).append(Format.pad("CONDITION", 18))
                .append(Format.pad("STATUS", 14)).append("PRICE\n");
        for (Car c : cars) {
            sb.append(Format.pad(c.getId(), 10)).append(Format.pad(c.getTitle(), 34)).append(Format.pad(c.getCondition(), 18))
                    .append(Format.pad(c.getStatus(), 14)).append(Format.money(c.getPrice())).append("\n");
        }
    }

    private void orderReport(StringBuilder sb) {
        String[] statuses = {Order.PENDING, Order.UNDER_REVIEW, Order.AWAITING_PAYMENT, Order.READY,
            Order.COMPLETED, Order.CANCELLED, Order.ON_HOLD};
        List<Order> orders = db.orders.getAll();
        for (String status : statuses) {
            int n = 0;
            for (Order o : orders) {
                if (o.getOrderStatus().equals(status)) n++;
            }
            sb.append(Format.pad(status, 20)).append(": ").append(n).append("\n");
        }
        sb.append("-".repeat(96)).append("\n");
        sb.append(Format.pad("ORDER", 10)).append(Format.pad("CUSTOMER", 24)).append(Format.pad("VEHICLE", 28))
                .append(Format.pad("STATUS", 19)).append(Format.pad("PAYMENT", 22)).append("TOTAL\n");
        for (Order o : orders) {
            sb.append(Format.pad(o.getId(), 10)).append(Format.pad(db.customerName(o.getCustomerId()), 24))
                    .append(Format.pad(db.carTitle(o.getCarId()), 28)).append(Format.pad(o.getOrderStatus(), 19))
                    .append(Format.pad(o.getPaymentStatus(), 22)).append(Format.money(o.getTotal())).append("\n");
        }
    }

    private void paymentReport(StringBuilder sb) {
        double paid = 0;
        double pending = 0;
        double refunded = 0;
        sb.append(Format.pad("PAYMENT", 10)).append(Format.pad("ORDER", 10)).append(Format.pad("METHOD", 15))
                .append(Format.pad("STATUS", 22)).append(Format.pad("RECEIPT", 10)).append("AMOUNT\n");
        for (Payment p : db.payments.getAll()) {
            sb.append(Format.pad(p.getId(), 10)).append(Format.pad(p.getOrderId(), 10)).append(Format.pad(p.getMethodName(), 15))
                    .append(Format.pad(p.getStatus(), 22)).append(Format.pad(p.getReceiptNo(), 10))
                    .append(Format.money(p.getAmount())).append("\n");
            if (Payment.PAID.equals(p.getStatus())) paid += p.getAmount();
            if (Payment.PENDING_VERIFICATION.equals(p.getStatus())) pending += p.getAmount();
            if (Payment.REFUNDED.equals(p.getStatus())) refunded += p.getAmount();
        }
        sb.append("-".repeat(96)).append("\nConfirmed (Paid): ").append(Format.money(paid))
                .append("\nPending verification: ").append(Format.money(pending))
                .append("\nRefunded: ").append(Format.money(refunded)).append("\n");
    }

    private void staffReport(StringBuilder sb) {
        sb.append(Format.pad("USERNAME", 16)).append(Format.pad("NAME", 24)).append(Format.pad("ROLE", 8))
                .append(Format.pad("STATUS", 18)).append(Format.pad("ORDERS", 8)).append(Format.pad("PAYMENTS", 10))
                .append("RELEASES\n");
        List<Order> orders = db.orders.getAll();
        List<Payment> payments = db.payments.getAll();
        for (User u : db.users.getAll()) {
            int o = 0;
            int p = 0;
            int h = 0;
            for (Order x : orders) if (x.getStaffUsername().equals(u.getUsername())) o++;
            for (Payment x : payments) if (x.getRecordedBy().equals(u.getUsername())) p++;
            for (chbcars.model.Handover x : db.handovers.getAll()) if (x.getStaffUsername().equals(u.getUsername())) h++;
            sb.append(Format.pad(u.getUsername(), 16)).append(Format.pad(u.getFullName(), 24)).append(Format.pad(u.getRole(), 8))
                    .append(Format.pad(u.getStatus(), 18)).append(Format.pad(String.valueOf(o), 8))
                    .append(Format.pad(String.valueOf(p), 10)).append(h).append("\n");
        }
        sb.append("-".repeat(96)).append("\nLATEST ACTIVITY\n");
        for (ActivityLog log : db.logs.recent(15)) {
            sb.append(log.getTimestamp()).append(" | ").append(Format.pad(log.getUsername(), 12)).append(" | ")
                    .append(log.getAction()).append(" - ").append(log.getDetails()).append("\n");
        }
    }
}
