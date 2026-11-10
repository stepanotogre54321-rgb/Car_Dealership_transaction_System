package chbcars.ui;

import chbcars.model.Car;
import chbcars.util.Format;
import chbcars.util.ImageUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** Staff: browse available cars and show full details (with photo) to the customer. */
public class VehicleBrowsePanel extends BasePanel {

    private final DefaultTableModel model = Theme.readOnlyModel("Photo", "Car ID", "Vehicle", "Body", "Color", "Mileage", "Price");
    private final JTable table = Theme.table(model);
    private final JTextField search = Theme.textField(18);
    private final JComboBox<String> bodyFilter;
    private final List<Car> shown = new ArrayList<>();
    private final JLabel image = new JLabel();
    private final JEditorPane details = new JEditorPane("text/html", "");
    private final JButton startOrder = Theme.primary("Start Order with this Car");
    private final Consumer<Car> onStartOrder;

    public VehicleBrowsePanel(Consumer<Car> onStartOrder) {
        Theme.widths(table, 84,80,170,70,60,80,110);
        this.onStartOrder = onStartOrder;
        String[] bodies = new String[Car.BODY_TYPES.length + 1];
        bodies[0] = "All Body Types";
        System.arraycopy(Car.BODY_TYPES, 0, bodies, 1, Car.BODY_TYPES.length);
        bodyFilter = Theme.combo(bodies);
        search.setToolTipText("Search by brand, model, color...");
        table.setRowHeight(62);
        table.getColumnModel().getColumn(0).setMaxWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);

        add(top("Browse Vehicles", "Cars that are currently available for sale.", Theme.label("Search:"), search, bodyFilter),
                BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);

        image.setHorizontalAlignment(SwingConstants.CENTER);
        details.setEditable(false);
        details.setOpaque(false);
        startOrder.setToolTipText("Go to New Order with this car already selected");
        JPanel side = Theme.card();
        side.setLayout(new BorderLayout(0, 10));
        side.setPreferredSize(new Dimension(380, 0));
        side.add(image, BorderLayout.NORTH);
        side.add(Theme.scroll(details), BorderLayout.CENTER);
        side.add(startOrder, BorderLayout.SOUTH);
        add(side, BorderLayout.EAST);

        liveSearch(search, this::refresh);
        bodyFilter.addActionListener(e -> refresh());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDetails();
            }
        });
        startOrder.addActionListener(e -> {
            int row = Theme.selectedModelRow(table);
            if (row < 0) {
                Dialogs.warn(this, "Please select a car first.");
            } else {
                onStartOrder.accept(shown.get(row));
            }
        });
        showDetails();
    }

    @Override
    public void refresh() {
        shown.clear();
        model.setRowCount(0);
        String key = search.getText().trim().toLowerCase();
        String body = (String) bodyFilter.getSelectedItem();
        for (Car c : db.cars.findByStatus(Car.AVAILABLE)) {
            if (!"All Body Types".equals(body) && !c.getBodyType().equals(body)) {
                continue;
            }
            if (!key.isEmpty() && !(c.getTitle() + " " + c.getColor()).toLowerCase().contains(key)) {
                continue;
            }
            shown.add(c);
            model.addRow(new Object[]{ImageUtil.load(c.getImagePath(), 84, 56), c.getId(), c.getTitle(), c.getBodyType(),
                c.getColor(), String.format("%,d km", c.getMileage()), Format.money(c.getPrice())});
        }
        showDetails();
    }

    private void showDetails() {
        int row = Theme.selectedModelRow(table);
        startOrder.setEnabled(row >= 0);
        if (row < 0 || row >= shown.size()) {
            image.setIcon(ImageUtil.load("", 340, 210));
            details.setText("<html><body style='font-family:Segoe UI,sans-serif;font-size:11px;color:#6B7280'>"
                    + "Select a car from the list to see its full details.</body></html>");
            return;
        }
        Car c = shown.get(row);
        image.setIcon(ImageUtil.load(c.getImagePath(), 340, 210));
        String description = c.getDescription().isEmpty() ? "No description." : c.getDescription().replace("\n", "<br>");
        details.setText("<html><body style='font-family:Segoe UI,sans-serif;font-size:11px;color:#1F2937'>"
                + "<div style='font-size:15px;font-weight:bold;color:#0B1F3A'>" + c.getTitle() + "</div>"
                + "<div style='font-size:17px;font-weight:bold;color:#D62828'>" + Format.money(c.getPrice()) + "</div><br>"
                + "<b>Condition:</b> " + c.getCondition() + "<br><b>Body type:</b> " + c.getBodyType()
                + "<br><b>Color:</b> " + c.getColor() + "<br><b>Transmission:</b> " + c.getTransmission()
                + "<br><b>Fuel:</b> " + c.getFuelType() + "<br><b>Engine:</b> " + (c.getEngine().isEmpty() ? "-" : c.getEngine())
                + "<br><b>Mileage:</b> " + String.format("%,d km", c.getMileage()) + "<br><b>Seats:</b> " + c.getSeats()
                + "<br><b>Chassis / VIN:</b> " + c.getChassisNo() + "<br><br><b>Description</b><br>" + description
                + "</body></html>");
        details.setCaretPosition(0);
    }
}
