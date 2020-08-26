 /*
 * [FixedButton.java]
 * This file contains the communication adapter for the backgammon-specific events.
 * Authour: Tara Rafi
 */

package Backgammon;

import javax.swing.*;
import java.awt.*;

public class FixedButton extends JButton
{
        Container content;
        Backgammon parent;

        public FixedButton(Container c, Backgammon p)
        {
                content = c;
                parent = p;
                content.setLayout(null);
                content.add(this);
        }

        public void drawOnSpike(int spike)
        {
                Insets in = parent.getInsets();

                if (spike > 12)
                        setBounds(parent.findX(spike) - in.left, parent.findY(spike) - in.top, 28, 10);
                else
                        setBounds(parent.findX(spike) - in.left, parent.findY(spike) - 10 - in.top, 28, 10);

                setVisible(true);
                parent.repaint();
        }
}
