package snow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * GUI that sets up the Window
 *
 * @author Kevin
 */
public class GUI {

    private static final Dimension SIZE = new Dimension(500, 500);

    private static final int HOLD_DELAY = 500;
    private static final int SPAWN_DELAY = 60;

    private static final double RANGE_TO_PERCENT = 0.01;

    private JFrame mainWindow;
    private JPanel topPanel;

    private JLabel minLabel;
    private JLabel maxLabel;
    private JSlider snowfallSlider;
    private DisplayCanvas canvas;

    /**
     * Create the GUI
     *
     * @param args command line arguments
     */
    public GUI(String[] args) {
        initializeWindow();
        registerListeners();
    }

    /**
     * Create Window Layout
     */
    private void initializeWindow() {
        //Instantiation
        mainWindow = new JFrame("Snow");
        mainWindow.setVisible(true);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        topPanel = new JPanel();
        minLabel = new JLabel("Silent Night");
        maxLabel = new JLabel("Let it Snow");

        snowfallSlider = new JSlider();
        snowfallSlider.setValue(0);

        canvas = new DisplayCanvas(SIZE);

        //Composition
        topPanel.add(minLabel);
        topPanel.add(snowfallSlider);
        topPanel.add(maxLabel);

        mainWindow.add(topPanel, BorderLayout.PAGE_START);
        mainWindow.add(canvas);

        //Ensure Correct Placement
        mainWindow.pack();
    }

    /**
     * Enable Interactivity
     */
    private void registerListeners() {
        snowfallSlider.addChangeListener((ChangeEvent e) -> {
            double percent = ((JSlider) e.getSource()).getValue() * RANGE_TO_PERCENT;
            canvas.adjustSnowfall(percent);
        });
        CanvasMouseListener c = new CanvasMouseListener();
        canvas.addMouseListener(c);
        canvas.addMouseMotionListener(c);
        canvas.addMouseWheelListener(c);
    }

    /**
     * Add mouse functionality to canvas
     */
    class CanvasMouseListener extends MouseInputAdapter {

        Timer delayTimer;
        Point previousPos;
        Point spawningPos;
        Timer spawningTimer;

        /**
         * Create a Mouse Listener for the Canvas
         */
        public CanvasMouseListener() {
            delayTimer = new Timer(HOLD_DELAY, ae -> spawningTimer.start());
            delayTimer.setRepeats(false);
            spawningTimer = new Timer(SPAWN_DELAY, ae -> canvas.spawnSnowflake(spawningPos));
        }

        /**
         * Callback for mouse presses that creates a snowflake
         *
         * @param e event context
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            canvas.spawnSnowflake(e.getPoint());
        }

        /**
         * Callback for mouse drags that updates last position
         *
         * @param e
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            spawningPos = e.getPoint();
        }

        /**
         * Callback for canvas exits
         *
         * @param e
         */
        @Override
        public void mouseExited(MouseEvent e) {
            previousPos = null;
        }

        /**
         * Callback for mouse movement that breaks snowflakes
         *
         * @param e event context
         */
        @Override
        public void mouseMoved(MouseEvent e) {
            if (previousPos == null) {
                previousPos = e.getPoint();
                return;
            }
            //Check snowflakes in line between previous and current
            Point currentPos = e.getPoint();
            canvas.gust(previousPos, currentPos);
            //Update Position
            previousPos = e.getPoint();
        }

        /**
         * Callback for mouse presses that spawns many snowflakes
         *
         * @param e event context
         */
        @Override
        public void mousePressed(MouseEvent e) {
            spawningPos = e.getPoint();
            delayTimer.start();
        }

        /**
         * Callback for mouse releases that stops the spawning of snowflakes
         *
         * @param e
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            delayTimer.stop();
            spawningTimer.stop();
            previousPos = e.getPoint();
        }
    }
}
