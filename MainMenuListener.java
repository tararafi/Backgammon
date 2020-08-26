 /*
 * [MainMenuListener.java]
 * This file constructs the main class and allows the JButtons to function with the use of ActionListener
 * Authour: Tara Rafi
 */

package Backgammon;

import java.awt.event.*;
import javax.swing.*;

public class MainMenuListener implements ActionListener
{
        JFrame parent;
        boolean networked;

        public MainMenuListener(JFrame p, boolean n)
        {
                parent = p;
                networked = n;
        }

        public void actionPerformed(ActionEvent e)
        {
                //Construct the main class
                Backgammon app = new Backgammon(networked);

                //Allow the window to be closed
                app.addWindowListener(
                        new WindowAdapter()
                        {
                                public void windowClosing(WindowEvent e)
                                {
                                        System.exit(0);
                                }
                        }
                );
                parent.setVisible(false);
        }

}

