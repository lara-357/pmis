package pmis;

import javax.swing.JComponent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.CubicCurve2D;

public class CurvedLineChart extends JComponent {
    private final List<SalesData> salesData;
    private int hoveredIndex = -1;

    public CurvedLineChart(List<SalesData> salesData) {
        this.salesData = salesData;
        setBackground(Color.BLACK);

        // Detect mouse movement
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoveredIndex = getNearestDataIndex(e.getX());
                repaint();
            }
        });
    }

    private int getNearestDataIndex(int mouseX) {
        int n = salesData.size();
        int closestIndex = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            double xPos = 50 + (i / (double) (n - 1)) * (getWidth() - 2 * 50);
            int distance = Math.abs((int) xPos - mouseX);
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }
        return closestIndex;
    }


@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    int width = getWidth();
    int height = getHeight();
    int padding = 50;

    if (width == 0 || height == 0 || salesData.isEmpty()) return;

    double maxValue = salesData.stream().mapToDouble(s -> s.totalSales).max().orElse(1);

    List<Integer> xPositions = new ArrayList<>();
    List<Integer> yPositions = new ArrayList<>();
    List<String> dates = new ArrayList<>();

    int n = salesData.size();

    // Calculate X and Y positions for points and store corresponding dates
    for (int i = 0; i < n; i++) {
        double x = padding + (i / (double) (n - 1)) * (width - 2 * padding);
        double y = height - padding - (salesData.get(i).totalSales / maxValue) * (height - 2 * padding);

        xPositions.add((int) x);
        yPositions.add((int) y);
        dates.add(salesData.get(i).date);
    }

    Graphics2D g2 = (Graphics2D) g;

    // Set rendering properties
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Draw the curved line in blue
    g2.setColor(Color.BLUE);
    g2.setStroke(new BasicStroke(3));

    for (int i = 0; i < xPositions.size() - 1; i++) {
        int x1 = xPositions.get(i);
        int y1 = yPositions.get(i);
        int x2 = xPositions.get(i + 1);
        int y2 = yPositions.get(i + 1);

        int ctrlX1 = (x1 + x2) / 2;
        int ctrlY1 = y1 - 50;

        int ctrlX2 = (x1 + x2) / 2;
        int ctrlY2 = y2 + 50;

        CubicCurve2D cubic = new CubicCurve2D.Float(x1, y1, ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2);
        g2.draw(cubic);
    }

    // Draw horizontal grid lines aligned with 10 intervals on Y-axis
    g2.setStroke(new BasicStroke(1));
    g2.setColor(new Color(255, 255, 255, 128));  // Light white for grid lines
    int numGridLines = 10;
    for (int i = 0; i <= numGridLines; i++) {
        int yPos = height - padding - (i * (height - 2 * padding) / numGridLines);
        g2.drawLine(padding, yPos, width - padding, yPos);
    }

    // Draw Y-axis labels in white (only 10 labels)
    g2.setColor(Color.WHITE);
    int yLabelStep = (int) (maxValue / 10);
    for (int i = 0; i <= 10; i++) {
        int yPos = height - padding - (i * (height - 2 * padding) / 10);
        g2.drawString(String.valueOf(i * yLabelStep), 10, yPos);
    }

    // Draw X-axis labels in white (only 7 evenly spaced labels)
    int totalDataPoints = xPositions.size();
    int xStep = Math.max(1, totalDataPoints / 7);

    for (int i = 0; i < totalDataPoints; i += xStep) {
        int xPos = xPositions.get(i);
        g2.drawString(dates.get(i), xPos - 20, height - padding + 20);
    }

    // Draw hover effect
    if (hoveredIndex >= 0 && hoveredIndex < xPositions.size()) {
        g2.setColor(Color.WHITE);
        int hoverX = xPositions.get(hoveredIndex);
        int hoverY = yPositions.get(hoveredIndex);

        // Draw a circle at the hovered point
        g2.fillOval(hoverX - 5, hoverY - 5, 10, 10);

        // Display the hovered data info
        String info = "Sales: " + salesData.get(hoveredIndex).totalSales;

        // Get label dimensions
        FontMetrics fm = g2.getFontMetrics();
        int labelWidth = fm.stringWidth(info);
        int labelHeight = fm.getHeight();

        // Adjust label position to keep it within bounds
        int infoX = hoverX + 10; // Default to right of the point
        int infoY = hoverY - 10; // Default to above the point

        if (infoX + labelWidth > getWidth()) {
            infoX = hoverX - labelWidth - 10; // Move left if overflow
        }
        if (infoY - labelHeight < 0) {
            infoY = hoverY + labelHeight + 10; // Move below if overflow
        }

        // Draw the label
        g2.drawString(info, infoX, infoY);
    }
}




}
