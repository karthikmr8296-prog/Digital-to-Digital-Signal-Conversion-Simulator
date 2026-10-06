import javax.swing.*;
import java.awt.*;

public class LineCodingSimulator extends JFrame {

    JTextField input;
    JComboBox<String> scheme;
    DrawPanel panel;

    LineCodingSimulator() {
        setTitle("Digital-to-Digital Signal Simulator");
        setSize(950, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel top = new JPanel();

        input = new JTextField("1011001", 12);
        scheme = new JComboBox<>(new String[]{
                "Unipolar NRZ", "NRZ-L", "NRZ-I", "RZ",
                "Manchester", "Differential Manchester", "AMI"
        });

        JButton button = new JButton("Generate");

        top.add(new JLabel("Bits:"));
        top.add(input);
        top.add(scheme);
        top.add(button);

        add(top, BorderLayout.NORTH);

        panel = new DrawPanel();
        add(panel);

        button.addActionListener(e -> {
            panel.bits = input.getText().trim();
            panel.type = (String) scheme.getSelectedItem();
            panel.repaint();
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    class DrawPanel extends JPanel {

        String bits = "";
        String type = "";

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (bits == null || bits.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2));

            int x = 60;
            int mid = 220;
            int high = 140;
            int low = 300;
            int zero = mid;

            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(type + " (" + bits + ")", 60, 40);

            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(Color.GRAY);

            g2.drawString("+V", 20, high + 5);
            g2.drawString("0V", 20, zero + 5);
            g2.drawString("-V", 20, low + 5);

            g2.drawLine(45, high, 60 + bits.length() * 50, high);
            g2.drawLine(45, zero, 60 + bits.length() * 50, zero);
            g2.drawLine(45, low, 60 + bits.length() * 50, low);

            g2.setColor(Color.BLACK);

            int prevYEnd = -1;
            int currentLevel = low;
            int amiPolarity = high;
            int diffLevel = low;

            for (int i = 0; i < bits.length(); i++) {
                char b = bits.charAt(i);
                if (b != '0' && b != '1') continue;

                int yStart = zero, yEnd = zero;
                boolean hasMidTransition = false;

                if (type.equals("Unipolar NRZ")) {
                    yStart = yEnd = (b == '1') ? high : zero;

                } else if (type.equals("NRZ-L")) {
                    yStart = yEnd = (b == '1') ? high : low;

                } else if (type.equals("NRZ-I")) {
                    if (b == '1') {
                        currentLevel = (currentLevel == high) ? low : high;
                    }
                    yStart = yEnd = currentLevel;

                } else if (type.equals("RZ")) {
                    hasMidTransition = true;
                    yStart = (b == '1') ? high : low;
                    yEnd = zero;

                } else if (type.equals("Manchester")) {
                    hasMidTransition = true;
                    yStart = (b == '1') ? high : low;
                    yEnd = (b == '1') ? low : high;

                } else if (type.equals("Differential Manchester")) {
                    hasMidTransition = true;
                    if (b == '0') {
                        yStart = (diffLevel == high) ? low : high;
                    } else {
                        yStart = diffLevel;
                    }
                    yEnd = (yStart == high) ? low : high;
                    diffLevel = yEnd;

                } else if (type.equals("AMI")) {
                    if (b == '0') {
                        yStart = yEnd = zero;
                    } else {
                        yStart = yEnd = amiPolarity;
                        amiPolarity = (amiPolarity == high) ? low : high;
                    }
                }

                if (i > 0 && prevYEnd != yStart) {
                    g2.drawLine(x, prevYEnd, x, yStart);
                }

                if (!hasMidTransition) {
                    g2.drawLine(x, yStart, x + 50, yEnd);
                } else {
                    g2.drawLine(x, yStart, x + 25, yStart);
                    g2.drawLine(x + 25, yStart, x + 25, yEnd);
                    g2.drawLine(x + 25, yEnd, x + 50, yEnd);
                }
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawLine(x, mid - 100, x, mid + 100);
                g2.setColor(Color.BLACK);

                g2.setFont(new Font("Arial", Font.BOLD, 14));
                g2.drawString("" + b, x + 20, 360);

                prevYEnd = yEnd;
                x += 50;
            }

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(x, mid - 100, x, mid + 100);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LineCodingSimulator());
    }
}
