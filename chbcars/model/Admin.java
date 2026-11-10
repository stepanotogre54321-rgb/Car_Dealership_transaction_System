package chbcars.model;

/** The dealership owner/manager: full access to every function. */
public class Admin extends User {

    public Admin(String id) {
        super(id);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public boolean canManageCars() {
        return true;
    }

    @Override
    public boolean canManageStaff() {
        return true;
    }

    @Override
    public boolean canViewReports() {
        return true;
    }

    @Override
    public boolean canChangePrices() {
        return true;
    }

    @Override
    public boolean canApproveOrders() {
        return true;
    }

    @Override
    public boolean canBeBlocked() {
        return false;
    }
}
