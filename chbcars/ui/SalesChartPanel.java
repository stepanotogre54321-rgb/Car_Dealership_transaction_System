package chbcars.ui;

import chbcars.util.Format;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

/**
 * A bar chart drawn with Java2D (no extra library). The admin dashboard gives it a label -> amount
 * map and it draws red bars, grid lines and the peso value above each bar. Hover a bar for details.
 */
public class SalesChartPanel extends JPanel {

    private final Color barColor;
    private final Map<String, Double> data = new LinkedHashMap<>();
    private final List<RoundRectangle2D> barShapes = new ArrayList<>();
    private int hovered = -1;

    public SalesChartPanel(Color barColor) {
        this.barColor = barColor;
        setOpaque(false);
        setPreferredSize(new Dimension(420, 260));
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int found = -1;
                for (int i = 0; i < barShapes.size(); i++) {
                    // hovering anywhere above or below a bar counts, so thin bars are easy to hit
                    if (e.getX() >= barShapes.get(i).getMinX() && e.getX() <= barShapes.get(i).getMaxX()) {
                        found = i;
                    }
                }
                if (found != hovered) {
                    hovered = found;
                    int index = 0;
                    String tip = null;
                    for (Map.Entry<String, Double> entry : data.entrySet()) {
                        if (index++ == found) {
                            tip = entry.getKey() + ": " + Format.money(entry.getValue());
                        }
                    }
                    setToolTipText(tip);
                    repaint();
                }
            }
        });
    }

    /** Replaces the numbers shown (label -> total sales). */
    public void setData(Map<String, Double> values) {
        data.clear();
        data.putAll(values);
        hovered = -1;
        repaint();
    }

    /** Short money text for the chart, e.g. 1.2M or 350K. */
    private static String compact(double amount) {
        if (amount >= 1_000_000) {
            return "\u20B1" + trim(amount / 1_000_000) + "M";
        }
        if (amount >= 1_000) {
            return "\u20B1" + trim(amount / 1_000) + "K";
        }
        return "\u20B1" + (long) amount;
    }

    private static String trim(double value) {
        String text = String.format("%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        barShapes.clear();

        double max = 0;
        for (double v : data.values()) {
            max = Math.max(max, v);
        }
        g2.setFont(Theme.SMALL);
        FontMetrics fm = g2.getFontMetrics();
        if (data.isEmpty() || max <= 0) {
            g2.setColor(Theme.MUTED);
            String msg = "No completed sales in this period yet.";
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
            g2.dispose();
            return;
        }

        // round the top of the scale to a "nice" number so the grid lines are readable
        double step = Math.pow(10, Math.floor(Math.log10(max)));
        double top = Math.ceil(max / step) * step;
        int left = fm.stringWidth(compact(top)) + 14;
        int right = 10;
        int topPad = 22;
        int bottomPad = 28;
        int plotW = getWidth() - left - right;
        int plotH = getHeight() - topPad - bottomPad;

        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= 4; i++) {
            int y = topPad + plotH - plotH * i / 4;
            g2.setColor(i == 0 ? Theme.BLACK : new Color(0xE8, 0xE8, 0xEA));
            g2.drawLine(left, y, left + plotW, y);
            g2.setColor(Theme.MUTED);
            String label = compact(top * i / 4);
            g2.drawString(label, left - 8 - fm.stringWidth(label), y + fm.getAscent() / 2 - 1);
        }

        int count = data.size();
        double slot = (double) plotW / count;
        double barW = Math.min(56, slot * 0.55);
        int index = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double value = entry.getValue();
            double h = plotH * value / top;
            double x = left + slot * index + (slot - barW) / 2;
            double y = topPad + plotH - h;
            RoundRectangle2D bar = new RoundRectangle2D.Double(x, y, barW, Math.max(h, 0), 8, 8);
            barShapes.add(bar);
            if (value > 0) {
                Color base = index == hovered ? Theme.DARK_RED : barColor;
                g2.setPaint(new GradientPaint(0, (float) y, base, 0, (float) (y + h), Theme.DARK_RED));
                g2.fill(bar);
                g2.setPaint(null);
                g2.setColor(Theme.BLACK);
                String label = compact(value);
                g2.drawString(label, (int) (x + (barW - fm.stringWidth(label)) / 2), (int) y - 6);
            }
            g2.setColor(Theme.MUTED);
            String name = entry.getKey();
            g2.drawString(name, (int) (left + slot * index + (slot - fm.stringWidth(name)) / 2), getHeight() - 8);
            index++;
        }
        g2.dispose();
    }
}
