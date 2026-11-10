package chbcars.ui;

import chbcars.model.User;

/** The Agent/Staff point of view: serves customers from browsing to vehicle handover. */
public class StaffDashboard extends BaseDashboard {

    private NewOrderPanel orderPanel;

    public StaffDashboard(User user) {
        super(user);
        buildPages();
        showFirstPage();
    }

    @Override
    protected void buildPages() {
        orderPanel = new NewOrderPanel(() -> showPage("payments"));
        addPage("home", "Dashboard", "Your work at a glance", new HomePanel(false));
        addPage("browse", "Browse Vehicles", "Show available cars to the customer",
                new VehicleBrowsePanel(car -> {
                    orderPanel.setCar(car);
                    showPage("order");
                }));
        addPage("order", "New Order", "Create an order for a customer", orderPanel);
        addPage("customers", "Customers", "Register and search customers", new CustomersPanel(false));
        addPage("payments", "Payments", "Record and verify payments", new PaymentsPanel(false));
        addPage("handover", "Vehicle Handover", "Release fully paid cars", new HandoverPanel());
        addPage("history", "Transaction History", "Search orders, receipts and handovers", new OrdersPanel(false));
    }
}
