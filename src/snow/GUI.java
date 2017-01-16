package snow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * GUI that sets up the Window
 * @author Kevin
 */
public class GUI {
    private static final int SIZE = 500;

    private static final int MIN_SNOWFALL = 0,
                             MAX_SNOWFALL = 100;

    private JFrame mainWindow;
    private JPanel topPanel;

    private JLabel minLabel,
                   maxLabel;
    private JSlider snowfallSlider;
    private DisplayCanvas canvas;

    /**
     * Create the GUI
     * @param args command line arguments
     */
    public GUI(String[] args) {
        initializeWindow(SIZE, SIZE);
        registerListeners();
    }

    /**
     * Create Window Layout
     * @param width Canvas horizontal dimension
     * @param height Canvas vertical dimension
     */
    private void initializeWindow(int width, int height) {
        //Instantiation
        mainWindow = new JFrame("Snow");
        mainWindow.setVisible(true);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        topPanel = new JPanel();
        minLabel = new JLabel("Silent Night");
        maxLabel = new JLabel("Let it Snow");

        snowfallSlider = new JSlider(MIN_SNOWFALL, MAX_SNOWFALL, SwingConstants.HORIZONTAL);

        canvas = new DisplayCanvas(this, new Dimension(width, height));

        //Composition
        topPanel.add(minLabel);
        topPanel.add(snowfallSlider);
        topPanel.add(maxLabel);

        mainWindow.add(topPanel, BorderLayout.PAGE_START);
        mainWindow.add(canvas);

        //Ensure Correct Placement
        mainWindow.pack();
    }

    private void registerListeners() {
        snowfallSlider.addChangeListener((ChangeEvent e) -> {
            double percent = ((JSlider)e.getSource()).getValue() / 100.0;
            canvas.adjustSnowfall(percent);
        });
        CanvasMouseListener c = new  CanvasMouseListener();
        canvas.addMouseListener(c);
        canvas.addMouseMotionListener(c);
        canvas.addMouseWheelListener(c);
    }

    /**
     * Add mouse functionality to canvas
     */
    class CanvasMouseListener extends MouseInputAdapter {
        Point previousPos;
        Point spawningPos;
        Timer spawningTimer;
        Timer delayTimer;

        public CanvasMouseListener() {
            delayTimer = new Timer(500, ae -> spawningTimer.start());
            delayTimer.setRepeats(false);
        }

        /**
         * Callback for mouse presses
         * that creates a snowflake
         * @param e event context
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            canvas.spawnSnowflake(e.getPoint());
        }

        /**
         * Callback for mouse presses
         * that spawns many snowflakes
         * @param e event context
         */
        @Override
        public void mousePressed(MouseEvent e) {
            //calculate circle around point
            spawningPos = e.getPoint();
            spawningTimer = new Timer(60, ae -> canvas.spawnSnowflake(spawningPos));
            delayTimer.start();
        }

        /**
         * Callback for mouse releases
         * that stops the spawning of snowflakes
         * @param e
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            delayTimer.stop();
            spawningTimer.stop();
        }

        /**
         * Callback for mouse drags
         * that updates last position
         * @param e
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            spawningPos = e.getPoint();
        }

        /**
         * Callback for mouse movement
         * that breaks snowflakes
         * @param e event context
         */
        @Override
        public void mouseMoved(MouseEvent e) {
            if(previousPos == null) {
                previousPos = e.getPoint();
                return;
            }

            //Check snowflakes in line between previous and current

//            Point currentPos = e.getPoint();
//            canvas.snowflakesAlong(previousPos, currentPos);

            previousPos = e.getPoint();
        }
    }
}
