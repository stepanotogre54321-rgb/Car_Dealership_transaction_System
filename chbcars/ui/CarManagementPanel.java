package chbcars.ui;

import chbcars.model.Car;
import chbcars.util.ErrorHandler;
import chbcars.util.Format;
import chbcars.util.ImageUtil;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Admin: car inventory (stocks.txt) with full CRUD, photos and search. */
public class CarManagementPanel extends BasePanel {

    private final DefaultTableModel model = Theme.readOnlyModel("Photo", "Car ID", "Vehicle", "Body", "Transmission",
            "Fuel", "Mileage", "Price", "Status", "Date Added");
    private final JTable table = Theme.table(model);
    private final JTextField search = Theme.textField(18);
    private final JComboBox<String> statusFilter = Theme.combo("All Statuses", Car.AVAILABLE, Car.RESERVED,
            Car.UNAVAILABLE, Car.RELEASED);
    private final List<Car> shown = new ArrayList<>();

    public CarManagementPanel() {
        Theme.widths(table, 90,80,200,80,100,80,80,120,90,120);
        table.setRowHeight(62);
        table.getColumnModel().getColumn(0).setMaxWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);

        search.setToolTipText("Search by ID, brand, model, color or chassis number");
        JButton add = Theme.primary("+ Add Car");
        add.setToolTipText("Post a new car to the stock");
        JButton edit = Theme.secondary("Edit");
        edit.setToolTipText("Edit the selected car");
        JButton archive = Theme.secondary("Archive / Restore");
        archive.setToolTipText("Hide an unsold car from sale, or put it back");
        JButton delete = Theme.button("Delete", Theme.GRAY);
        delete.setToolTipText("Delete the selected car (admin password required)");

        add(top("Car Inventory", "Manage the stock of cars for sale (saved in stocks.txt).",
                Theme.label("Search:"), search, statusFilter, add, edit, archive, delete), BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);

        liveSearch(search, this::refresh);
        statusFilter.addActionListener(e -> refresh());
        add.addActionListener(e -> {
            if (new CarFormDialog(owner(), null).showDialog()) {
                refresh();
            }
        });
        edit.addActionListener(e -> editSelected());
        archive.addActionListener(e -> archiveSelected());
        delete.addActionListener(e -> deleteSelected());
    }

    @Override
    public void refresh() {
        shown.clear();
        model.setRowCount(0);
        String key = search.getText().trim().toLowerCase();
        String status = (String) statusFilter.getSelectedItem();
        for (Car c : db.cars.getAll()) {
            if (!"All Statuses".equals(status) && !c.getStatus().equals(status)) {
                continue;
            }
            String text = (c.getId() + " " + c.getTitle() + " " + c.getColor() + " " + c.getChassisNo()).toLowerCase();
            if (!key.isEmpty() && !text.contains(key)) {
                continue;
            }
            shown.add(c);
            model.addRow(new Object[]{ImageUtil.load(c.getImagePath(), 84, 56), c.getId(), c.getTitle(), c.getBodyType(),
                c.getTransmission(), c.getFuelType(), String.format("%,d km", c.getMileage()),
                Format.money(c.getPrice()), c.getStatus(), c.getDateAdded()});
        }
    }

    private Car selected() {
        int row = Theme.selectedModelRow(table);
        if (row < 0) {
            Dialogs.warn(this, "Please select a car in the table first.");
            return null;
        }
        return shown.get(row);
    }

    private void editSelected() {
        Car car = selected();
        if (car == null) {
            return;
        }
        if (Car.RELEASED.equals(car.getStatus())) {
            Dialogs.warn(this, "Released vehicles are locked and cannot be edited.");
            return;
        }
        if (new CarFormDialog(owner(), db.cars.findById(car.getId())).showDialog()) {
            refresh();
        }
    }

    private void archiveSelected() {
        Car car = selected();
        if (car == null) {
            return;
        }
        if (Car.AVAILABLE.equals(car.getStatus())) {
            car.setStatus(Car.UNAVAILABLE);
        } else if (Car.UNAVAILABLE.equals(car.getStatus())) {
            car.setStatus(Car.AVAILABLE);
        } else {
            Dialogs.warn(this, "Only Available or Unavailable cars can be archived or restored.");
            return;
        }
        ErrorHandler.execute(this, () -> {
            db.cars.update(car);
            db.log("Car " + car.getStatus(), car.getId());
        });
        refresh();
    }

    private void deleteSelected() {
        Car car = selected();
        if (car == null) {
            return;
        }
        if (!Car.AVAILABLE.equals(car.getStatus()) && !Car.UNAVAILABLE.equals(car.getStatus())) {
            Dialogs.warn(this, "Reserved or released cars cannot be deleted.");
            return;
        }
        if (db.orders.carHasOrders(car.getId())) {
            Dialogs.warn(this, "This car has order records. Archive it instead of deleting it.");
            return;
        }
        if (!Dialogs.confirm(this, "Delete " + car.getTitle() + " (" + car.getId() + ")?")
                || !Dialogs.confirmAdminPassword(this, "delete this car")) {
            return;
        }
        ErrorHandler.execute(this, () -> {
            db.cars.delete(car.getId());
            db.log("Car Deleted", car.getId() + " - " + car.getTitle());
        });
        refresh();
    }
}
