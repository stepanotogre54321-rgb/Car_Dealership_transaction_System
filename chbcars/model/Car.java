package chbcars.model;

import chbcars.util.FileHandler;
import chbcars.util.Format;

/** One car unit in the stock (saved in data/stocks.txt). */
public class Car extends Entity {

    public static final String AVAILABLE = "Available";
    public static final String RESERVED = "Reserved";
    public static final String UNAVAILABLE = "Unavailable";
    public static final String RELEASED = "Released";

    public static final String[] BRANDS = {"Toyota", "Honda", "Mitsubishi", "Nissan", "Ford", "Suzuki", "Hyundai",
        "Kia", "Mazda", "Isuzu", "Subaru", "Chevrolet", "Geely", "MG", "Chery", "BMW", "Mercedes-Benz", "Volkswagen"};
    public static final String[] BODY_TYPES = {"Sedan", "Hatchback", "SUV", "Crossover", "Pickup", "Van", "MPV", "Coupe", "Wagon"};
    public static final String[] TRANSMISSIONS = {"Automatic", "Manual", "CVT", "Dual-Clutch"};
    public static final String[] FUEL_TYPES = {"Gasoline", "Diesel", "Hybrid", "Electric", "Plug-in Hybrid"};
    public static final String[] CONDITIONS = {"Brand New", "Used - Like New", "Used - Good", "Used - Fair"};

    private String brand = "";
    private String model = "";
    private String variant = "";
    private int year;
    private String bodyType = BODY_TYPES[0];
    private String color = "";
    private String transmission = TRANSMISSIONS[0];
    private String fuelType = FUEL_TYPES[0];
    private String engine = "";
    private int mileage;
    private String condition = CONDITIONS[0];
    private int seats = 5;
    private String chassisNo = "";
    private double price;
    private String status = AVAILABLE;
    private String description = "";
    private String imagePath = "";
    private String dateAdded = "";

    public Car() {
        super("");
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }
    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public String getChassisNo() { return chassisNo; }
    public void setChassisNo(String chassisNo) { this.chassisNo = chassisNo; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getDateAdded() { return dateAdded; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }

    public boolean isAvailable() {
        return AVAILABLE.equals(status);
    }

    /** Example: "2023 Toyota Vios 1.3 XLE". */
    public String getTitle() {
        return year + " " + brand + " " + model + (variant.isEmpty() ? "" : " " + variant);
    }

    @Override
    public String getSummary() {
        return getId() + " | " + getTitle() + " | " + Format.money(price);
    }

    @Override
    public String toFileLine() {
        return FileHandler.join(getId(), brand, model, variant, year, bodyType, color, transmission, fuelType,
                engine, mileage, condition, seats, chassisNo, price, status, description, imagePath, dateAdded);
    }

    public static Car fromFields(String[] f) {
        Car c = new Car();
        c.setId(f[0]);
        c.brand = f[1];
        c.model = f[2];
        c.variant = f[3];
        c.year = Integer.parseInt(f[4]);
        c.bodyType = f[5];
        c.color = f[6];
        c.transmission = f[7];
        c.fuelType = f[8];
        c.engine = f[9];
        c.mileage = Integer.parseInt(f[10]);
        c.condition = f[11];
        c.seats = Integer.parseInt(f[12]);
        c.chassisNo = f[13];
        c.price = Double.parseDouble(f[14]);
        c.status = f[15];
        c.description = f[16];
        c.imagePath = f[17];
        c.dateAdded = f[18];
        return c;
    }
}
