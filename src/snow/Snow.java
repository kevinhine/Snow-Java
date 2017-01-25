/**
 * TODO:
 * rotation
 * scaling
 * noise
 * snowflake generation (maybe using the noise generator iteratively?)
 */

package snow;

import javax.swing.SwingUtilities;

/**
 * Application that displays falling snowflakes
 * @author Kevin
 */
public class Snow {

    /**
     * Program Entry
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI mainWindow = new GUI(args);
        });
    }
}
