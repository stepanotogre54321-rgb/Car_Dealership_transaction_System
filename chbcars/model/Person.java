package chbcars.model;

public abstract class Person extends Entity {

    private String fullName = "";
    private String contactNo = "";
    private String email = "";

    protected Person(String id) {
        super(id);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public abstract String getPersonType();
}
