package famTree;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class familytreeviewer extends JPanel {

    private Image backgroundImage; // kept but unused (null) so clampOffsets remains safe

    static class PersonNode {
        String name;
        String DOB;
        String DOD;   // null or "" = alive
        int age;      // age if alive, age at death if dead

        List<PersonNode> parents = new ArrayList<>();
        int x, y; // world coordinates

        PersonNode(String name, String DOB, String DOD, int age) {
            this.name = name;
            this.DOB = DOB;
            this.DOD = DOD;
            this.age = age;
        }

        void addParent(PersonNode parent) {
            parents.add(parent);
        }

        Rectangle getBounds() {
            return new Rectangle(x, y, 80, 40);
        }
    }

    private List<PersonNode> nodes = new ArrayList<>();
    private int offsetX = 0, offsetY = 0;
    private int lastMouseX, lastMouseY;
    private boolean dragging = false;

    public familytreeviewer() {

        // No image background; use null so existing clampOffsets logic stays safe
        backgroundImage = null;

        PersonNode child = new PersonNode("Alex", "2005-03-10", "", 19);

        PersonNode mom = new PersonNode("Mary", "1980-08-21", "", 44);
        PersonNode dad = new PersonNode("John", "1978-01-14", "", 46);

        PersonNode grandmaM = new PersonNode("Evelyn", "1952-06-01", "2019-12-10", 67);
        PersonNode grandpaM = new PersonNode("Robert", "1950-02-18", "", 74);

        PersonNode grandmaD = new PersonNode("Susan", "1956-11-30", "", 68);
        PersonNode grandpaD = new PersonNode("Charles", "1953-05-22", "2008-04-02", 55);

        // Parent relationships
        child.addParent(mom);
        child.addParent(dad);

        mom.addParent(grandmaM);
        mom.addParent(grandpaM);

        dad.addParent(grandmaD);
        dad.addParent(grandpaD);

        // Add all
        nodes.add(child);
        nodes.add(mom);
        nodes.add(dad);
        nodes.add(grandmaM);
        nodes.add(grandpaM);
        nodes.add(grandmaD);
        nodes.add(grandpaD);

        // Positions
        child.x = 0; child.y = 0;

        mom.x = -100; mom.y = -100;
        dad.x = 100;  dad.y = -100;

        grandmaM.x = -150; grandmaM.y = -200;
        grandpaM.x = -50;  grandpaM.y = -200;

        grandmaD.x = 50;   grandmaD.y = -200;
        grandpaD.x = 150;  grandpaD.y = -200;

        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        //Mouse controls
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;
                if (!dragging) {
                    if (Math.abs(dx) >= 2 || Math.abs(dy) >= 2) {
                        dragging = true;
                    } else {
                        return;
                    }
                }
                offsetX += dx;
                offsetY += dy;

                clampOffsets();

                lastMouseX = e.getX();
                lastMouseY = e.getY();
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    // Clicks
    private void handleClick(int mouseX, int mouseY) {

        // Convert screen → world coordinates
        int worldX = mouseX - (getWidth() / 2 + offsetX);
        int worldY = mouseY - (getHeight() / 2 + offsetY);

        for (PersonNode p : nodes) {
            if (p.getBounds().contains(worldX, worldY)) {

                String info =
                    "Name: " + p.name +
                    "\nDOB: " + p.DOB +
                    "\nDOD: " + (p.DOD.isEmpty() ? "Still Alive" : p.DOD) +
                    "\nAge: " + p.age;

                JOptionPane.showMessageDialog(
                        this,
                        info,
                        "Person Information",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }
        }
    }

    // Function makes sure user can't scroll too far off page
    private void clampOffsets() {
        if (backgroundImage == null) return;

        int imgW = backgroundImage.getWidth(null);
        int imgH = backgroundImage.getHeight(null);

        int halfW = getWidth() / 2;
        int halfH = getHeight() / 2;

        int minX = -imgW/2 + halfW;
        int maxX =  imgW/2 - halfW;
        int minY = -imgH/2 + halfH;
        int maxY =  imgH/2 - halfH;

        if (offsetX < minX) offsetX = minX;
        if (offsetX > maxX) offsetX = maxX;

        if (offsetY < minY) offsetY = minY;
        if (offsetY > maxY) offsetY = maxY;
    }

    // Drawing
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            // Base font and stroke for general text/lines
            g2.setFont(new Font("Arial", Font.ITALIC, 14));
            g2.setStroke(new BasicStroke(2));

            // --- Draw centered light gray area with a darker gray outline (affected by panning) ---
            int centerX = getWidth() / 2 + offsetX;
            int centerY = getHeight() / 2 + offsetY;

            // Make the centered area slightly smaller than the panel so border is visible
            int rectW = Math.max(200, getWidth() - 200);
            int rectH = Math.max(150, getHeight() - 200);

            int rectX = centerX - rectW / 2;
            int rectY = centerY - rectH / 2;

            // Light gray interior
            g2.setColor(new Color(230, 230, 230)); // light gray
            g2.fillRect(rectX, rectY, rectW, rectH);

            // Dark gray outline (not too thick)
            g2.setColor(new Color(100, 100, 100)); // dark gray
            g2.setStroke(new BasicStroke(3)); // thin but visible border
            g2.drawRect(rectX, rectY, rectW, rectH);

            // Translate to world origin (center + offsets)
            g2.translate(getWidth() / 2 + offsetX, getHeight() / 2 + offsetY);

            // connecting lines (keep them a bit thinner)
            g2.setStroke(new BasicStroke(2));
            for (PersonNode child : nodes) {
                for (PersonNode parent : child.parents) {
                    g2.setColor(Color.BLACK);
                    int cx = child.x + 40; // center x
                    int cy = child.y + 20; // center y
                    int px = parent.x + 40;
                    int py = parent.y + 20;
                    g2.drawLine(cx, cy, px, py);
                }
            }

            // draw boxes: beige interior, slightly thicker border
            Color beige = new Color(245, 245, 220);
            for (PersonNode p : nodes) {
                g2.setColor(beige);
                g2.fillRect(p.x, p.y, 80, 40);

                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(3)); // thicker box border
                g2.drawRect(p.x, p.y, 80, 40);

                // text (stroke reset not necessary for text)
                g2.setColor(Color.BLACK);
                int textWidth = g2.getFontMetrics().stringWidth(p.name);
                int centeredX = p.x + (80 - textWidth) / 2;
                g2.drawString(p.name, centeredX, p.y + 25);
            }
        } finally {
            g2.dispose();
        }
    }

    // Simple launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Family Tree Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            familytreeviewer panel = new familytreeviewer();
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
