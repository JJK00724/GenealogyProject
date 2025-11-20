package famTree;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class familytreeviewer extends JPanel {

    static class PersonNode {
        String name;
        List<PersonNode> parents = new ArrayList<>();
        int x, y; // world coordinates (not screen)

        PersonNode(String name) {
            this.name = name;
        }

        void addParent(PersonNode parent) {
            parents.add(parent);
        }
    }

    private List<PersonNode> nodes = new ArrayList<>();
    private int offsetX = 0, offsetY = 0;
    private int lastMouseX, lastMouseY;
    private boolean dragging = false;

    public familytreeviewer() {
        // --- People ---
        PersonNode child = new PersonNode("Alex");

        // Parents
        PersonNode mom = new PersonNode("Mary");
        PersonNode dad = new PersonNode("John");
        child.addParent(mom);
        child.addParent(dad);

        // Maternal grandparents
        PersonNode grandmaM = new PersonNode("Evelyn");
        PersonNode grandpaM = new PersonNode("Robert");
        mom.addParent(grandmaM);
        mom.addParent(grandpaM);

        // Paternal grandparents
        PersonNode grandmaD = new PersonNode("Susan");
        PersonNode grandpaD = new PersonNode("Charles");
        dad.addParent(grandmaD);
        dad.addParent(grandpaD);

        // Add all to list
        nodes.add(child);
        nodes.add(mom);
        nodes.add(dad);
        nodes.add(grandmaM);
        nodes.add(grandpaM);
        nodes.add(grandmaD);
        nodes.add(grandpaD);

        // --- Coordinates (relative positions) ---
        child.x = 0; child.y = 0;

        mom.x = -100; mom.y = -100;
        dad.x = 100;  dad.y = -100;

        grandmaM.x = -150; grandmaM.y = -200;
        grandpaM.x = -50;  grandpaM.y = -200;

        grandmaD.x = 50;   grandmaD.y = -200;
        grandpaD.x = 150;  grandpaD.y = -200;

        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        // --- Mouse controls for panning ---
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragging = true;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    int dx = e.getX() - lastMouseX;
                    int dy = e.getY() - lastMouseY;
                    offsetX += dx;
                    offsetY += dy;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    repaint();
                }
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Draw faint grid
        drawGrid(g2);

        // Apply panning offset (translate)
        g2.translate(getWidth() / 2 + offsetX, getHeight() / 2 + offsetY);

        // Draw connecting lines
        for (PersonNode child : nodes) {
            for (PersonNode parent : child.parents) {
                g2.setColor(Color.GRAY);
                g2.drawLine(child.x + 40, child.y, parent.x + 40, parent.y + 40);
            }
        }

        // Draw person boxes
        for (PersonNode p : nodes) {
            g2.setColor(new Color(255, 230, 180));
            g2.fillRect(p.x, p.y, 80, 40);
            g2.setColor(Color.BLACK);
            g2.drawRect(p.x, p.y, 80, 40);
            g2.drawString(p.name, p.x + 10, p.y + 25);
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(230, 230, 230));
        int gridSize = 50;

        int width = getWidth();
        int height = getHeight();

        int startX = offsetX % gridSize;
        int startY = offsetY % gridSize;

        for (int x = startX; x < width; x += gridSize) {
            g2.drawLine(x, 0, x, height);
        }
        for (int y = startY; y < height; y += gridSize) {
            g2.drawLine(0, y, width, y);
        }
    }
}
