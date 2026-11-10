package chbcars.ui;

import chbcars.data.DataStore;
import chbcars.model.Car;
import chbcars.util.ErrorHandler;
import chbcars.util.Format;
import chbcars.util.ImageUtil;
import chbcars.util.ValidationException;
import chbcars.util.Validator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Add / edit a car listing (like posting an item on Facebook Marketplace) with image upload. */
public class CarFormDialog extends BaseDialog {

    private static final int IMG_W = 300;
    private static final int IMG_H = 200;

    private final Car existing;
    private final JComboBox<String> brand = Theme.combo(Car.BRANDS);
    private final JTextField model = Theme.textField(14);
    private final JTextField variant = Theme.textField(14);
    private final JTextField year = Theme.textField(14);
    private final JComboBox<String> bodyType = Theme.combo(Car.BODY_TYPES);
    private final JTextField color = Theme.textField(14);
    private final JComboBox<String> transmission = Theme.combo(Car.TRANSMISSIONS);
    private final JComboBox<String> fuel = Theme.combo(Car.FUEL_TYPES);
    private final JTextField engine = Theme.textField(14);
    private final JTextField mileage = Theme.textField(14);
    private final JComboBox<String> condition = Theme.combo(Car.CONDITIONS);
    private final JTextField seats = Theme.textField(14);
    private final JTextField chassis = Theme.textField(14);
    private final JTextField price = Theme.textField(14);
    private final JComboBox<String> status = Theme.combo(Car.AVAILABLE, Car.UNAVAILABLE);
    private final JTextArea description = Theme.textArea(4, 30);
    private final JLabel preview = new JLabel();
    private String imagePath = "";
    private File pendingImage;

    public CarFormDialog(Window owner, Car existing) {
        super(owner, existing == null ? "Add New Car" : "Edit Car",
                "Fill in the car details like a marketplace listing. Fields marked * are required.",
                existing == null ? "Post Car" : "Save Changes");
        this.existing = existing;
        brand.setEditable(true);
        brand.setToolTipText("Choose a brand or type a new one");
        model.setToolTipText("e.g. Vios");
        variant.setToolTipText("e.g. 1.3 XLE CVT");
        year.setToolTipText("Model year, e.g. 2023");
        engine.setToolTipText("e.g. 1.5L 4-cylinder");
        mileage.setToolTipText("Odometer reading in kilometers (0 for brand new)");
        chassis.setToolTipText("Unique chassis / VIN number of this unit");
        price.setToolTipText("Selling price in pesos");
        description.setToolTipText("Features, condition, inclusions, etc.");
        if (existing != null) {
            loadExisting(existing);
        } else {
            year.setText(String.valueOf(Format.currentYear()));
            mileage.setText("0");
        }
        preview.setIcon(ImageUtil.load(imagePath, IMG_W, IMG_H));
        init();
    }

    private void loadExisting(Car c) {
        brand.setSelectedItem(c.getBrand());
        model.setText(c.getModel());
        variant.setText(c.getVariant());
        year.setText(String.valueOf(c.getYear()));
        bodyType.setSelectedItem(c.getBodyType());
        color.setText(c.getColor());
        transmission.setSelectedItem(c.getTransmission());
        fuel.setSelectedItem(c.getFuelType());
        engine.setText(c.getEngine());
        mileage.setText(String.valueOf(c.getMileage()));
        condition.setSelectedItem(c.getCondition());
        seats.setText(String.valueOf(c.getSeats()));
        chassis.setText(c.getChassisNo());
        price.setText(String.format("%.2f", c.getPrice()));
        description.setText(c.getDescription());
        imagePath = c.getImagePath();
        if (Car.RESERVED.equals(c.getStatus()) || Car.RELEASED.equals(c.getStatus())) {
            status.removeAllItems();
            status.addItem(c.getStatus());
            status.setEnabled(false);
        } else {
            status.setSelectedItem(c.getStatus());
        }
    }

    @Override
    protected JComponent buildForm() {
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setBackground(Color.WHITE);
        preview.setHorizontalAlignment(SwingConstants.CENTER);
        preview.setBorder(new LineBorder(Theme.BORDER));
        JButton upload = Theme.secondary("Upload Photo");
        upload.setToolTipText("Choose a JPG, PNG, GIF or BMP picture (max 5 MB)");
        upload.addActionListener(e -> chooseImage());
        left.add(Theme.heading("Car Photo"), BorderLayout.NORTH);
        left.add(preview, BorderLayout.CENTER);
        left.add(upload, BorderLayout.SOUTH);

        JPanel grid = Theme.formPanel();
        Theme.addField(grid, 0, 0, "Brand *", brand);
        Theme.addField(grid, 0, 1, "Model *", model);
        Theme.addField(grid, 1, 0, "Variant", variant);
        Theme.addField(grid, 1, 1, "Year *", year);
        Theme.addField(grid, 2, 0, "Body type", bodyType);
        Theme.addField(grid, 2, 1, "Color *", color);
        Theme.addField(grid, 3, 0, "Transmission", transmission);
        Theme.addField(grid, 3, 1, "Fuel type", fuel);
        Theme.addField(grid, 4, 0, "Engine", engine);
        Theme.addField(grid, 4, 1, "Mileage (km) *", mileage);
        Theme.addField(grid, 5, 0, "Condition", condition);
        Theme.addField(grid, 5, 1, "Seats *", seats);
        Theme.addField(grid, 6, 0, "Chassis / VIN *", chassis);
        Theme.addField(grid, 6, 1, "Price (\u20B1) *", price);
        Theme.addField(grid, 7, 0, "Listing status", status);
        Theme.addWide(grid, 8, "Description", Theme.scroll(description));

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Color.WHITE);
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.NORTHWEST;
        lc.insets = new Insets(0, 0, 0, 12);
        root.add(left, lc);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTH;
        root.add(grid, gc);
        root.setBorder(new EmptyBorder(0, 0, 0, 0));
        return root;
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a car photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, GIF, BMP)", "jpg", "jpeg", "png", "gif", "bmp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            ErrorHandler.execute(this, () -> {
                if (file.length() > 5L * 1024 * 1024) {
                    throw new ValidationException("The image must be 5 MB or smaller.");
                }
                preview.setIcon(ImageUtil.load(file.getPath(), IMG_W, IMG_H));
                pendingImage = file;
            });
        }
    }

    @Override
    protected void onSubmit() throws Exception {
        DataStore db = DataStore.get();
        Car car = existing == null ? new Car() : existing;
        car.setBrand(Validator.requireText(String.valueOf(brand.getSelectedItem()), "Brand", 2, 30));
        car.setModel(Validator.requireText(model.getText(), "Model", 1, 30));
        car.setVariant(Validator.optionalText(variant.getText(), "Variant", 30));
        car.setYear(Validator.parseInt(year.getText(), "Year", 1980, Format.currentYear() + 1));
        car.setBodyType((String) bodyType.getSelectedItem());
        car.setColor(Validator.requireName(color.getText(), "Color"));
        car.setTransmission((String) transmission.getSelectedItem());
        car.setFuelType((String) fuel.getSelectedItem());
        car.setEngine(Validator.optionalText(engine.getText(), "Engine", 30));
        car.setMileage(Validator.parseInt(mileage.getText().isEmpty() ? "0" : mileage.getText(), "Mileage", 0, 1_000_000));
        car.setCondition((String) condition.getSelectedItem());
        car.setSeats(Validator.parseInt(seats.getText(), "Seats", 2, 15));
        String chassisNo = Validator.requireText(chassis.getText(), "Chassis / VIN", 5, 25).toUpperCase();
        if (!chassisNo.matches("[A-Z0-9-]+")) {
            throw new ValidationException("Chassis / VIN may only contain letters, numbers and hyphens.");
        }
        if (db.cars.chassisExists(chassisNo, car.getId())) {
            throw new ValidationException("A car with this chassis / VIN number is already in the stock.");
        }
        car.setChassisNo(chassisNo);
        car.setPrice(Validator.parseMoney(price.getText(), "Price"));
        car.setDescription(Validator.optionalText(description.getText(), "Description", 800));
        if (status.isEnabled()) {
            car.setStatus((String) status.getSelectedItem());
        }
        if (pendingImage != null) {
            car.setImagePath(ImageUtil.importImage(pendingImage));
        } else {
            car.setImagePath(imagePath);
        }
        if (existing == null) {
            car.setDateAdded(Format.now());
            db.cars.add(car);
            db.log("Car Added", car.getId() + " - " + car.getTitle());
        } else {
            db.cars.update(car);
            db.log("Car Updated", car.getId() + " - " + car.getTitle());
        }
    }
}
