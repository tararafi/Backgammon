 /*
 * [tablaMouseListener.java]
 * This file contains the mouse listener class and identifies which spike the mouse click occured on. 
 * Authour: Tara Rafi
 */
 
package Backgammon;

import java.awt.event.*;

public class tablaMouseListener extends MouseAdapter
{
        Backgammon parent;

        public tablaMouseListener(Backgammon p)
        {
                //Find where the main Backgammon class is so we can use methods from it
                parent = p;
        }

        public void mouseReleased(MouseEvent e)
        {
                //Adjust values as if the board was set in the top left corner at (0,0)
                int m_x = e.getX() - Backgammon.x_offset;
                int m_y = e.getY() - Backgammon.y_offset;
                
                //We only want to check clicks within the bounds of the playing board
                //   (0 <= x <= 190) OR (240 <= x <= 430) AND
                //   (0 <= y <= 160) OR (200 <= y <= 360)
                if( ( ((m_x>=0) && (m_x<=191)) || ((m_x>=238) && (m_x<=430)) ) &&
                                ( ((m_y>=0) && (m_y<=160)) || ((m_y>=200) && (m_y<=360)) ) &&
                                parent.b.rolled && !parent.status.observer )
                        parent.HandleSpike(parent.getSpikeNum(m_x,m_y));
        }
}
