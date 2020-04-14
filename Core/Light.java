package byow.Core;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class Light {
    private int imageX;
    private int imageY;

    private BufferedImage image;

    private int x;
    private int y;
    private int radius;
    private int luminosity;

    public void setPosition(int a, int b) {
        imageX = a;
        imageY = b;
    }

    public Light(int a, int b, int r, int l) {
        x = a;
        y = b;
        radius = r;
        luminosity = l;

        image = new BufferedImage(radius * 2, radius * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();

        int step = 4;
        int num = radius / step;
        g.setColor(new Color(0, 0, 0, luminosity));
        for (int i = 0; i < num; i += 1) {
            g.fillOval(radius - i * step, radius - i * step, i * step * 2, i * step * 2);
        }

    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getR() {
        return radius;
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Point2D center = new Point2D.Float(x, y);
        float[] dist = {0.9f, 1.0f};
        Color[] color = {new Color(0.0f, 0.0f, 0.0f, 0.1f), new Color(0, 0, 0, 255)};
        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, color);
        g2d.setPaint(p);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .95f));
        g2d.fillRect(0, 0, radius * 2, radius * 2);
        g2d.dispose();
    }

}
