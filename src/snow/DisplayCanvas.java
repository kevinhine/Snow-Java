package snow;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import javafx.geometry.Point3D;
import javax.swing.*;

/**
 * Panel for rendering
 *
 * @author Kevin
 */
public class DisplayCanvas extends JPanel {

    private static final int BORDER = 15; //Snowflake Size

    private static final double EPSILON = 0.000001; //Arbitrarily Small Number

    public static final int FRAME_RATE = 60; //frames per second
    public static final int FRAME_DELAY = 1000 / FRAME_RATE; //frame duration in ms
    public static final double FRAME_DELTA_TIME = 1.0 / FRAME_RATE; //frame frequency in seconds per frame

    private static final int MAX_SPAWN_DELAY = FRAME_RATE * 5, MIN_SPAWN_DELAY = FRAME_RATE / 2;

    private static final double MIN_GUST_STRENGTH = 1;
    private static final double MAX_GUST_STRENGTH = 10;

    private static final double GUST_MARGIN = 6;

    private final Dimension preferredSize;

    private final Timer snowSpawnTimer;
    private final Timer snowTickTimer;

    private double snowfallPercent = 0;
    private final ArrayList<Snowflake> snowflakes = new ArrayList<>(); //Switch to a pool

    /**
     * Create a Display Canvas
     *
     * @param d optimal canvas size
     */
    public DisplayCanvas(Dimension d) {
        preferredSize = d;
        snowTickTimer = new Timer(FRAME_DELAY, ae -> {
            snowTick();
            repaint();
        });
        snowSpawnTimer = new Timer(FRAME_DELAY, ae -> spawnSnowflake());
        snowTickTimer.start();
        initialize();
    }

    /**
     * Update snow spawn rate
     *
     * @param snowfallPercent ratio of maximum snowfall
     */
    public void adjustSnowfall(double snowfallPercent) {
        this.snowfallPercent = snowfallPercent;
        //Disable Timer when near off
        if (snowfallPercent < EPSILON) {
            snowSpawnTimer.stop();
        } else if (!snowSpawnTimer.isRunning()) {
            snowSpawnTimer.start();
        }
        calculateSnowSpawnrate();
    }

    /**
     * Determine spawn delay for snowflakes
     */
    private void calculateSnowSpawnrate() {
        //Adjust Speed Based on Slider
        double delay = Utility.lerp(MAX_SPAWN_DELAY, MIN_SPAWN_DELAY, snowfallPercent);
        //Adjust based on snowfall density
        delay *= preferredSize.getWidth() / getWidth();
        snowSpawnTimer.setDelay((int) delay);
    }

    /**
     * Set Optimal Dimensions
     *
     * @return Preferred canvas size
     */
    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    /**
     * Push snowflakes Around
     *
     * @param start beginning of the wind current
     * @param finish end of the wind current
     */
    public void gust(Point start, Point finish) {
        double strength = Utility.clamp(MIN_GUST_STRENGTH, MAX_GUST_STRENGTH, start.distance(finish));
        Line2D windCurrent = new Line2D.Double(start, finish);
        //Affect Snowflakes nearby the mouse
        for (Snowflake s : snowflakes) {
            Point3D position = s.getPosition();
            if (windCurrent.ptSegDist(position.getX(), position.getY()) < GUST_MARGIN * strength) {
                //Calculate wind direction
                double opposite = finish.getY() - start.getY();
                double adjacent = finish.getX() - start.getX();
                double windAngle = Math.atan2(opposite, adjacent);
                s.gust(windAngle, strength);
            }
        }
    }

    /**
     * Avoid Overloadable calls in the constructor
     */
    private void initialize() {
        setBackground(Color.BLACK);
        this.addComponentListener(new resizeListener());
    }

    /**
     * Render Snowflakes
     *
     * @param g graphics context
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (snowflakes != null) {
            for (Snowflake s : snowflakes) {
                s.paint(g);
            }
        }
        //Debug
//        g.fillRect(10, 8, 120, 16);
//        g.setColor(Color.WHITE);
//        g.drawString("Snowflakes: " + snowflakes.size(), 12, 20);
    }

    /**
     * Update the snowflake positions
     */
    private void snowTick() {
        for (int i = snowflakes.size() - 1; i >= 0; i--) {
            Snowflake s = snowflakes.get(i);
            Point3D pos = s.fall();
            //remove snowflakes that fall offscreen
            if (pos.getY() > getHeight() || pos.getX() < -BORDER
                    || pos.getX() > getWidth() + BORDER) {
                snowflakes.remove(s);
            }
        }
    }

    /**
     * Instantiate a snowflake
     *
     * @param xPos
     * @param yPos
     */
    public void spawnSnowflake(double xPos, double yPos) {
        snowflakes.add(new Snowflake(xPos, yPos));
    }

    /**
     * Instantiate a snowflake
     *
     * @param pos snowflake spawn position
     */
    public void spawnSnowflake(Point pos) {
        spawnSnowflake(pos.x, pos.y);
    }

    /**
     * Instantiate a snowflake at the top of the screen
     */
    public void spawnSnowflake() {
        spawnSnowflake(Math.random() * getWidth(), -BORDER);
    }

    /**
     * Allow the canvas to respond to resize events
     */
    class resizeListener extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            calculateSnowSpawnrate();
        }
    }
}
