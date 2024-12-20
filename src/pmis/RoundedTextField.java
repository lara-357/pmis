package pmis;
import javax.swing.*;
import java.awt.*;

public class RoundedTextField extends JTextField {
    private int cornerRadius; // Radius for corners

    public RoundedTextField(int radius) {
        this.cornerRadius = radius;
        setOpaque(false); // Make the text field transparent
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background color
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Draw text
        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Border color
        g2.setColor(getForeground());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        g2.dispose();
    }

    @Override
    public Insets getInsets() {
        int padding = cornerRadius / 2;
        return new Insets(padding, padding, padding, padding); // Add padding inside the text field
    }
}
