package chbcars.model;

import chbcars.util.FileHandler;

public class Customer extends Person {

    public static final String[] ID_TYPES = {"Driver's License", "Passport", "UMID", "PhilSys ID", "Postal ID", "Other Government ID"};

    private String address = "";
    private String idType = ID_TYPES[0];
    private String idNumber = "";
    private String dateRegistered = "";

    public Customer() {
        super("");
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(String dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    @Override
    public String getPersonType() {
        return "Customer";
    }

    @Override
    public String getSummary() {
        return getFullName() + " (" + getContactNo() + ")";
    }

    @Override
    public String toFileLine() {
        return FileHandler.join(getId(), getFullName(), getContactNo(), getEmail(), address, idType, idNumber, dateRegistered);
    }

    public static Customer fromFields(String[] f) {
        Customer c = new Customer();
        c.setId(f[0]);
        c.setFullName(f[1]);
        c.setContactNo(f[2]);
        c.setEmail(f[3]);
        c.setAddress(f[4]);
        c.setIdType(f[5]);
        c.setIdNumber(f[6]);
        c.setDateRegistered(f[7]);
        return c;
    }
}
