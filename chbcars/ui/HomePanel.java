package chbcars.ui;

import chbcars.data.Session;
import chbcars.model.ActivityLog;
import chbcars.model.Order;
import chbcars.model.User;
import chbcars.service.ReportService;
import chbcars.util.Format;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Dashboard home: black number cards, then (for the admin) a sales chart beside the recent
 * activity, or (for staff) the latest orders. Used by both roles.
 */
public class HomePanel extends BasePanel {

    private static final Color[] ACCENTS = {Theme.RED, Theme.WARNING, Theme.RED, Theme.WARNING, Theme.RED, Theme.WARNING};

    private final boolean admin;
    private final ReportService reports = new ReportService();
    private final JPanel cards = new JPanel(new GridLayout(0, 3, 14, 14));
    private final JLabel subtitle = Theme.muted(" ");
    private final DefaultTableModel model;
    private final JLabel tableTitle = Theme.heading(" ");
    private final SalesChartPanel chart = new SalesChartPanel(Theme.RED);
    private final JComboBox<String> chartMode = Theme.combo(ReportService.CHART_MODES);
    private final JLabel chartTotal = Theme.muted(" ");

    public HomePanel(boolean admin) {
        this.admin = admin;
        model = admin ? Theme.readOnlyModel("Time", "User", "Action", "Details")
                : Theme.readOnlyModel("Order ID", "Customer", "Vehicle", "Total", "Order Status", "Payment");

        JPanel heading = new JPanel(new BorderLayout(0, 2));
        heading.setOpaque(false);
        heading.add(Theme.title("Dashboard"), BorderLayout.NORTH);
        heading.add(subtitle, BorderLayout.CENTER);

        cards.setOpaque(false);
        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.add(heading, BorderLayout.NORTH);
        north.add(cards, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        JPanel tableCard = Theme.card();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(tableTitle, BorderLayout.NORTH);
        javax.swing.JTable recent = Theme.table(model);
        Theme.widths(recent, admin ? new int[]{120, 80, 110, 230} : new int[]{90, 150, 200, 120, 130, 130});
        tableCard.add(Theme.scroll(recent), BorderLayout.CENTER);

        if (admin) {
            add(buildAdminCenter(tableCard), BorderLayout.CENTER);
        } else {
            add(tableCard, BorderLayout.CENTER);
        }
    }

    /** Admin only: the sales chart on the left and the recent activity on the right. */
    private JPanel buildAdminCenter(JPanel tableCard) {
        chartMode.setToolTipText("Choose the period shown in the chart");
        chartMode.setSelectedIndex(1);
        chartMode.addActionListener(e -> refreshChart());

        JPanel chartHead = new JPanel(new BorderLayout(10, 0));
        chartHead.setOpaque(false);
        JPanel titles = new JPanel(new BorderLayout(0, 2));
        titles.setOpaque(false);
        titles.add(Theme.heading("Sales Chart"), BorderLayout.NORTH);
        titles.add(chartTotal, BorderLayout.CENTER);
        chartHead.add(titles, BorderLayout.CENTER);
        chartHead.add(chartMode, BorderLayout.EAST);

        JPanel chartCard = Theme.card();
        chartCard.setLayout(new BorderLayout(0, 8));
        chartCard.add(chartHead, BorderLayout.NORTH);
        chartCard.add(chart, BorderLayout.CENTER);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0;
        c.weightx = 0.52;
        c.insets = new java.awt.Insets(0, 0, 0, 7);
        center.add(chartCard, c);
        c.gridx = 1;
        c.weightx = 0.48;
        c.insets = new java.awt.Insets(0, 7, 0, 0);
        center.add(tableCard, c);
        return center;
    }

    private void refreshChart() {
        Map<String, Double> totals = reports.salesChart((String) chartMode.getSelectedItem());
        double sum = 0;
        for (double v : totals.values()) {
            sum += v;
        }
        chart.setData(totals);
        chartTotal.setText("Completed sales in this period: " + Format.money(sum));
    }

    @Override
    public void refresh() {
        User me = Session.getUser();
        subtitle.setText("Welcome back, " + (me == null ? "" : me.getFullName()) + "  \u2022  " + Format.today());
        cards.removeAll();
        Map<String, String> stats = admin ? reports.adminStats() : reports.staffStats();
        int i = 0;
        for (Map.Entry<String, String> e : stats.entrySet()) {
            cards.add(statCard(e.getKey(), e.getValue(), ACCENTS[i++ % ACCENTS.length]));
        }
        cards.revalidate();
        cards.repaint();

        model.setRowCount(0);
        if (admin) {
            refreshChart();
            tableTitle.setText("Recent Activity");
            for (ActivityLog log : db.logs.recent(12)) {
                model.addRow(new Object[]{log.getTimestamp(), log.getUsername(), log.getAction(), log.getDetails()});
            }
        } else {
            tableTitle.setText("Latest Orders");
            List<Order> orders = db.orders.getAll();
            Collections.reverse(orders);
            for (Order o : orders.subList(0, Math.min(12, orders.size()))) {
                model.addRow(new Object[]{o.getId(), db.customerName(o.getCustomerId()), db.carTitle(o.getCarId()),
                    Format.money(o.getTotal()), o.getOrderStatus(), o.getPaymentStatus()});
            }
        }
    }

    private JPanel statCard(String label, String value, Color accent) {
        return new StatCard(label, value, accent);
    }

    /** One black number card: small label on top, big white number, colored strip at the bottom. */
    private static class StatCard extends JPanel {

        StatCard(String label, String value, Color accent) {
            super(new BorderLayout(0, 4));
            setBackground(Theme.BLACK);
            setBorder(javax.swing.BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 4, 0, accent),
                    new EmptyBorder(12, 18, 10, 18)));
            JLabel name = new JLabel(label.toUpperCase());
            name.setFont(Theme.SMALL);
            name.setForeground(new Color(0xB4, 0xB4, 0xBA));
            JLabel number = new JLabel(value);
            number.setFont(Theme.TITLE.deriveFont(28f));
            number.setForeground(Color.WHITE);
            add(name, BorderLayout.NORTH);
            add(number, BorderLayout.CENTER);
        }
    }
}
