import javax.swing.*;
import java.awt.*;

/**
 * Created by sebastian on 10/24/17.
 */
public class Main {
    public static void main(String[] args) {
        try {
            // create and set up the applet
            Tron applet = new Tron();
            applet.d = new Dimension(800, 600);
            //applet.setPreferredSize(new Dimension(800, 600));
            applet.init();

            // create a frame to host the applet, which is just another type of Swing Component
            JFrame mainFrame = new JFrame();
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // add the applet to the frame and show it
            mainFrame.getContentPane().add(applet);
            mainFrame.pack();
            mainFrame.setVisible(true);

            // start the applet
            applet.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
