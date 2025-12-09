package famTree;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class familytreeviewer extends JPanel {
	
	private Image backgroundImage;

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

    	backgroundImage = new ImageIcon("background.jpg").getImage();
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
                    
                    clampOffsets();
                    
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    repaint();
                }
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
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        g2.setFont(new Font("Arial", Font.ITALIC, 14));

        //drawGrid(g2);

        if (backgroundImage != null) {
            // Draw background centered + affected by panning
            g2.drawImage(
                backgroundImage,
                (getWidth() / 2 + offsetX) - backgroundImage.getWidth(null) / 2,
                (getHeight() / 2 + offsetY) - backgroundImage.getHeight(null) / 2,
                null
            );
        }

        g2.translate(getWidth() / 2 + offsetX, getHeight() / 2 + offsetY);

        
        // connecting lines
        for (PersonNode child : nodes) {
            for (PersonNode parent : child.parents) {
                g2.setColor(Color.BLACK);
                g2.drawLine(child.x + 40, child.y, parent.x + 40, parent.y + 40);
            }
        }

        // draw boxes
        for (PersonNode p : nodes) {
            g2.setColor(Color.ORANGE);
            g2.fillRect(p.x, p.y, 80, 40);
            g2.setColor(Color.BLACK);
            g2.drawRect(p.x, p.y, 80, 40);
            int textWidth = g2.getFontMetrics().stringWidth(p.name);
            int centeredX = p.x + (80 - textWidth) / 2;
            g2.drawString(p.name, centeredX, p.y + 25);
        }
    }
/*
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
    */
}
