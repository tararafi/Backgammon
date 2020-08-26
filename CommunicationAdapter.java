 /*
 * [CommunicationAdapter.java]
 * This file contains the communication adapter for the backgammon-specific events.
 * Authour: Tara Rafi
 */
 

package Backgammon;

public interface CommunicationAdapter
{
        public void receiverolls(int i, int j); //Gets called when dice rolls are received
        public void receivemove(int oldpos, int newpos); //Gets called when a move is received
        public void receiveResetReq(); //Gets called when the other player clicks "New Game"
        public void receiveResetResp(int resp); //Gets called when the other player responds to "New Game"
        public void receivemessage(String message); //Gets called when a text message is received
        public void connected(); //Gets called when a connection is established
        public void turnfinished(); //Gets called when the other player's turn is over
        public void receivebar(int spike); //Sends a man to the bar
        public void disconnected(); //Gets called when a socket error occurs
        public void receivelose(); //Gets called when the player loses the game
        public void connectionrefused(); //Gets called when a connection fails to be established
}
