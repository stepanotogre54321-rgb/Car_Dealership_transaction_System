package chbcars.ui;

import chbcars.model.User;

/** The Admin point of view: inventory, accounts, orders, payments, customers, reports and logs. */
public class AdminDashboard extends BaseDashboard {

    public AdminDashboard(User user) {
        super(user);
        buildPages();
        showFirstPage();
    }

    @Override
    protected void buildPages() {
        addPage("home", "Dashboard", "Overview of the dealership", new HomePanel(true));
        addPage("cars", "Car Inventory", "Add, edit, archive or delete cars", new CarManagementPanel());
        addPage("staff", "Staff Accounts", "Create and manage accounts", new StaffManagementPanel());
        addPage("orders", "Orders", "Review and approve orders", new OrdersPanel(true));
        addPage("payments", "Payments", "Monitor payments and refunds", new PaymentsPanel(true));
        addPage("customers", "Customers", "Customer records and history", new CustomersPanel(true));
        addPage("reports", "Reports", "Generate reports", new ReportsPanel());
        addPage("logs", "Activity Logs", "Review system actions", new ActivityLogPanel());
    }
}
