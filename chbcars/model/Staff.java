package chbcars.model;

/** Sales agent / staff: can serve customers but has restricted permissions. */
public class Staff extends User {

    public Staff(String id) {
        super(id);
    }

    @Override
    public String getRole() {
        return "Staff";
    }

    @Override
    public boolean canManageCars() {
        return false;
    }

    @Override
    public boolean canManageStaff() {
        return false;
    }

    @Override
    public boolean canViewReports() {
        return false;
    }

    @Override
    public boolean canChangePrices() {
        return false;
    }

    @Override
    public boolean canApproveOrders() {
        return false;
    }

    @Override
    public boolean canBeBlocked() {
        return true;
    }
}
