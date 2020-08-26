/*
 * [Backgammon.java]
 * Main method program in which the methods and classes are created to make the game function
 * Authour: Tara Rafi
 */


package Backgammon;

//All needed imports from Java
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.io.*;
import sun.audio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Random;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Color;
public class Backgammon extends JFrame
                        implements ActionListener, CommunicationAdapter
{
  
  //Background Music using File I/O
  
    public static void music(){
  AudioPlayer MGP = AudioPlayer.player;
  AudioStream BGM;
  AudioData MD;
  ContinuousAudioDataStream loop = null;
  try{
   BGM = new AudioStream(new FileInputStream("C:\\Backgammon\\PatakasWorld.wav"));
   MD = BGM.getData();
   loop = new ContinuousAudioDataStream(MD);
  }catch(IOException error){
   System.out.print("file not found");
  }
  
  MGP.start(loop);
 }

 static final String VERSION = "1";
 
 static final int neutral = 0;
        static final int white = 1;
        static final int black = 2;

        //Color to be used when drawing a white checker
        static final Color clr_white = new Color(200, 200, 200);
        //Color to be used when drawing a black checker
        static final Color clr_black = new Color(50, 50, 50);

        //Color to be used when drawing a white spike
        static final Color spike_black = new Color(130, 70, 0);
        //Color to be used when drawing a black spike
        static final Color spike_white = new Color(240, 215, 100);

        //Buffers used for double buffering
        BufferedImage b_bimage;
        Graphics2D g_buffer;
        static final int x_offset = 20;
        static final int y_offset = 60;
        board b;

        //When moving, the original position of the checker
        private int old_spike;

        //The current player
        private int current_player = white;

        private int used_move = 0;
        /* used_move == 1 means first dice has been used
         * used_move == 2 means second dice has been used
         * used_move == 0 means no die have been used yet
         */

        //The move possible with each dice
        //Positions:
        // 1 - 24 = spikes, 1 being on the beginning of the black quarter
        //-1 = bar
        // 0 = black bear off
        // 25 = white bear off
        private int potmove1, potmove2;


        //If there are doublets, how many doublet moves remain
        int doublet_moves;

        //This contains some booleans about the status of the game
        Status status;

        //Class that performs the network operations
        Communication comm = null;

        //Textfield used for typing messages
        JTextField msg_input;

        //TextArea used to display messages between the players
        JTextArea msg_display;

        //Scroll pane to provide scrolling capabilities for messages
        JScrollPane msg_scrollpane;

        //The buttons the gui uses for various purposes
        FixedButton FButton[] = new FixedButton[8];
        /* FButton Number             Purpose
        * ------------------------------------------------------------
        * 0                          Cancel Move
        * 1                          Roll Dice
        * 2                          Bear Off
        * 3                          Potential Move 1
        * 4                          Potential Move 2
        * 5                          Connect (only if networked)
        * 6                          Send Message (only if networked)
        * 7                          New Game */

        //Button labels
        static final String CANCEL = "Cancel Move";
        static final String ROLL_DICE = "Roll Dice";
        static final String BEAR_OFF = "Bear Off";
        static final String MOVE1 = "M1";
        static final String MOVE2 = "M2";
        static final String CONNECT = "Connect";
        static final String SEND_MSG = "Send Message";
        static final String NEW_GAME = "New Game";


        /*=================================================
         * Game-related Methods 
         * ================================================*/

        private void debug_msg(String dmsg)
        {
                System.out.println("----------------");
                System.out.println("Breakpoint " +  dmsg);
                System.out.println("status.spike_selected = " + status.spike_selected + "   old_spike = " + old_spike);
                System.out.println("current_player = " + current_player + "   used_move = " + used_move);
                System.out.println("potmove1 = " + potmove1 + "   potmove2 = " + potmove2);
                System.out.println("doublet_moves = " + doublet_moves + " doublets = " + status.doublets);
                System.out.println("networked = " + status.networked + "   observer = " + status.observer);
                System.out.println("Number of black = " + b.getBlack());
                System.out.println("Number of white = " + b.getWhite());
                System.out.println("----------------");
                System.out.println();
        }

        //Selects a spike and shows the possible moves
        public void HandleSpike(int spike)
        {

                debug_msg("HandleSpike() is beginning");
                debug_data("HandleSpike called with spike=",spike);
                //The player cannot move the other's pieces
                if(b.getColor(spike)==current_player && !status.spike_selected)
                {
                        //Get the possible moves from that spike
                        if(current_player==white)
                        {
                                potmove1 = spike+b.getDice1();
                                potmove2 = spike+b.getDice2();

                                //If the player can make no other moves, allow him
                                //to bear off with rolls larger than what is needed to bear off
                                if (NeedsInexactRolls())
                                {
                                        if (potmove1 > 25)
                                                potmove1 = 25;
                                        if (potmove2 > 25)
                                                potmove2 = 25;
                                }
                        }
                        else if(current_player==black)
                        {
                                potmove1 = spike-b.getDice1();
                                potmove2 = spike-b.getDice2();

                                //If the player can make no other moves, allow him
                                //to bear off with rolls larger than what is needed to bear off
                                if (NeedsInexactRolls())
                                {
                                        if (potmove1 < 0)
                                                potmove1 = 0;
                                        if (potmove2 < 0)
                                                potmove2 = 0;
                                }
                        }

                        //If a move is valid, enable the button to move to it
                        if(CheckFair(potmove1) && used_move!=1)
                        {
                                if((potmove1<25) && (potmove1>0))
                                {
                                        FButton[0].setEnabled(true);
                                        FButton[3].drawOnSpike(potmove1);
                                        status.spike_selected = true;
                                }

                                //The possible move leads to bearing off
                                else
                                {
                                        FButton[0].setEnabled(true);
                                        FButton[2].setEnabled(true);
                                        status.spike_selected = true;
                                }
                        }
                        if(CheckFair(potmove2) && used_move!=2)
                        {
                                if((potmove2<25) && (potmove2>0))
                                {
                                        FButton[0].setEnabled(true);
                                        FButton[4].drawOnSpike(potmove2);
                                        status.spike_selected = true;
                                }

                                //The possible move leads to bearing off
                                else
                                {
                                        FButton[0].setEnabled(true);
                                        FButton[2].setEnabled(true);
                                        status.spike_selected = true;
                                }
                        }
                        old_spike = spike;
                }
                debug_msg("HandleSpike() is ending");
        }

        //Handle moving from one spike to another
        //new_move - the new position to move to
        //move - which dice is being used, the first one or the second one
        private void SuperMove(int new_move, int move)
        {
                /* In networked mode:
                 * 25 = to bar
                 * 26 = bear off */
                debug_msg("SuperMove()");

                boolean switchedplayers = true;

                //If the new space is empty, make the move
                //Else send the opponent on the bar first
                if((b.getColor(new_move)==current_player) || (b.getColor(new_move)==neutral))
                {
                        move(current_player, old_spike, new_move);
                        if(status.networked&&!status.observer)
                                comm.sendmove(old_spike, new_move);
                }
                else
                {
                        b.moveToBar(new_move);

                        move(current_player, old_spike, new_move);

                        if(status.networked)
                        {
                                comm.sendonbar(new_move);
                                comm.sendmove(old_spike, new_move);
                        }
                }

                if(!status.doublets)
                {
                        //If a move has been made previously,
                        //this is the second move, end the player's turn
                        if(used_move==1 || used_move==2)
                                EndTurn();
                        else
                        {
                                switchedplayers = false;
                                used_move = move;
                        }
                }
                else if(status.doublets)
                {
                        doublet_moves--;
                        if(doublet_moves==0)
                                EndTurn();
                        else
                                switchedplayers = false;
                }

                //Turn off focus on this spike
                EndMove();
                repaint();
                
                //If this wasn't the player's last move,
                //check if they are still on the bar or if he can make more moves
                if(!switchedplayers)
                {
                        if(b.onBar(current_player))
                                HandleBar();

                        if (!CanMove())
                                Forfeit();
                }
        }

        //Bear off a checker from the current spike
        private void BearOff()
        {
                //Remove a checker from the old spike
                b.setColumn(old_spike, b.getCount(old_spike)-1, current_player);
                if(current_player==white)
                        b.white_bear++;
                else
                        b.black_bear++;

                if(status.networked)
                        comm.sendmove(old_spike, 26);

                FButton[2].setEnabled(false);

                boolean won = false; //did someone win
                if(!status.networked)
                        won = CheckWin(current_player);
                if(status.networked&&(status.observer==false))
                        won = CheckWin(white);
                if (won)
                {
                        EndMove();//Disable buttons
                        return;//Do nothing if there's a winner
                }

                //Remove the dice we used
                if(!status.doublets)
                {
                        //Since a previous move has already occured, we are done
                        if(used_move==1 || used_move==2)
                                EndTurn();
                        else
                        {
                                //if you can bear off with both, use smaller dice
                                if (((potmove1==25)||(potmove1==0)) && ((potmove2==25)||(potmove2==0)))
                                {
                                        if (b.getDice1() > b.getDice2())
                                                used_move = 2;
                                        else
                                                used_move = 1;
                                }
                                else if((potmove1==25)||(potmove1==0))
                                        used_move = 1;
                                else if((potmove2==25)||(potmove2==0))
                                        used_move = 2;
                        }
                }
                else if(status.doublets)
                {
                        doublet_moves--;
                        if(doublet_moves==0)
                                EndTurn();
                }

                //Turn off focus on this spike
                EndMove();
                repaint();

                if (!CanMove())
                        Forfeit();
        }

        //Handle someone being on the bar
        //Mark possible escapes and forfeit if there are none
        private void HandleBar()
        {
                int escape1;
                int escape2;

                if(current_player==white)
                {
                        escape1 = b.getDice1();
                        escape2 = b.getDice2();
                }

                else
                {
                        escape1 = 25-b.getDice1();
                        escape2 = 25-b.getDice2();
                }


                //Can they escape?
                if( (used_move!=1) && CheckFair(escape1) )
                {
                        FButton[3].drawOnSpike(escape1);
                        FButton[3].setVisible(true);
                        potmove1 = escape1;
                        old_spike = -1;
                        status.spike_selected = true;
                }
                if( (used_move!=2) && CheckFair(escape2) )
                {
                        FButton[4].drawOnSpike(escape2);
                        FButton[4].setVisible(true);
                        potmove2 = escape2;
                        old_spike = -1;
                        status.spike_selected = true;
                }

                //Nope? Then they forfeit
                if(used_move==0)
                {
                        if(!CheckFair(escape1)&&!CheckFair(escape2))
                                Forfeit();
                }
                else if(used_move==1)
                {
                        if(!CheckFair(escape2))
                                Forfeit();
                }
                else if(used_move==2)
                {
                        if(!CheckFair(escape1))
                                Forfeit();
                }
        }

        //Forfeit the current player's turn
        private void Forfeit()
        {
                String msg = "You are stuck you forfeit your turn.";
                JOptionPane.showMessageDialog(this, msg);
                EndTurn();
                repaint();
        }

        //Checks if there is a winner
        //If there is one, displays appropriate message
        //Return true if there was a winner, false otherwise
        private boolean CheckWin(int color)
        {
                String msg;

                if((color==white)&&(!status.networked))
                        msg = "White wins";
                else if((color==black)&&(!status.networked))
                        msg = "Black wins";
                else
                        msg = "You win!";

                if(color==white)
                {
                        if(b.white_bear==15)
                        {
                                if(status.networked)
                                        comm.sendlose();
                                repaint();
                                JOptionPane.showMessageDialog(this, msg);
                                return true;
                        }
                }

                if(color==black)
                {
                        if(b.black_bear==15)
                        {
                                if(status.networked)
                                        comm.sendlose();
                                repaint();
                                JOptionPane.showMessageDialog(this, msg);
                                return true;
                        }
                }
                return false;
        }

        //Roll the dice for the current player
        private void DoRoll()
        {
                b.rollDice();

                if(status.networked)
                        comm.sendroll(b.getDice1(), b.getDice2());

                if(b.getDice1()==b.getDice2())
                {
                        status.doublets = true;
                        doublet_moves = 4;
                }
                else
                        status.doublets = false;

                //Turn off roll dice button
                FButton[1].setEnabled(true);

                repaint();

                //Check if the player is on the bar
                if(b.onBar(current_player))
                        HandleBar();
                else if (!CanMove())
                        Forfeit();
        }

        //End the current player's turn and start the turn
        //of the other player
        private void EndTurn()
        {
                String msg;
                //Change player
                if(current_player == white)
                        current_player = black;
                else
                        current_player = white;

                //Reset vars, turn off new game button
                used_move = 0;
                b.resetDice();
                b.rolled = false;
                //FButton[7].setEnabled(false);
               
                repaint();
                
                if(!status.networked)
                        msg = "Your turn is now over.  Please switch players.";
                else
                        msg = "Your turn is now over.";

                if(status.networked)
                {
                        comm.sendendturn();
                        status.observer = true;
                }

                JOptionPane.showMessageDialog(this, msg);

                if(!status.networked)
                        StartTurn();
                repaint();
        }

        //Begins a player's turn
        private void StartTurn()
        {
                //Enable roll dice and new game buttons
                FButton[1].setEnabled(true);
                FButton[7].setEnabled(true);
                if(status.networked&&!status.observer)
                {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(this, "It is now your turn");
                }
        }

        //Remove focus from a sertain spike which has been selected
        //This allows the player to select a new spike
        private void EndMove()
        {
                status.spike_selected = false;
                //Disable potential move buttons
                FButton[3].setVisible(false);
                FButton[4].setVisible(false);
                //Disable Cancel move button
                FButton[0].setEnabled(false);
        }

        //Return wether the current player can place a checker
        //at a certain position
        private boolean CheckFair(int new_pos)
        {
                debug_msg("CheckFair()");

                //Only positions 0 through 25 are valid moves
                if (new_pos > 25 || new_pos < 0)
                        return false;

                //Positions 0 and 25 are bearing off
                if((new_pos == 25) || (new_pos == 0))
                {
                        if(b.canBearOff(current_player))
                                return true;
                        else
                                return false;
                }
                else
                {
                        //If there is only one checker, the move is legal
                        if(b.getCount(new_pos)==1)
                                return true;
                        //If the spike is empty or has the user's own checkers, the move is legal
                        if((b.getColor(new_pos)==neutral) || (b.getColor(new_pos)==current_player))
                                return true;
                }

                return false;
        }

        //With the current rolls, can the user move anywhere?
        private boolean CanMove()
        {
                int move1, move2;
                //Cycle through all the spikes
                for (int spike = 1; spike <=24; spike++)
                {
                        //Only check spikes which contain the player's pieces
                        if (b.getColor(spike) == current_player)
                        {
                                if(current_player==white)
                                {
                                        move1 = spike+b.getDice1();
                                        move2 = spike+b.getDice2();
                                }
                                else
                                {
                                        move1 = spike-b.getDice1();
                                        move2 = spike-b.getDice2();
                                }
                                if ( (CheckFair(move1) && used_move != 1) || (CheckFair(move2) && used_move != 2))
                                        return true;

                                //CheckFair() only allows bearing off with exact rolls.
                                //If the player has no other option, moving with a roll greater than needed to bear off is legal
                                else if(NeedsInexactRolls() && (move1 > 25 || move1 < 0 || move2 > 25 || move2 < 0))
                                        return true;
                        }
                }
                return false;
        }

        //Returns wether the current player can't move anywhere else
        //and needs to be able to bear off with an inexact roll
        private boolean NeedsInexactRolls()
        {
                boolean canmove = false;
                int move1, move2;
                //Cycle through all the spikes
                for (int spike = 1; spike <=24; spike++)
                {
                        //Only check spikes which contain the player's pieces
                        if (b.getColor(spike) == current_player)
                        {
                                if(current_player==white)
                                {
                                        move1 = spike+b.getDice1();
                                        move2 = spike+b.getDice2();
                                }
                                else
                                {
                                        move1 = spike-b.getDice1();
                                        move2 = spike-b.getDice2();
                                }
                                if ( (CheckFair(move1) && used_move != 1) || (CheckFair(move2) && used_move != 2))
                                        canmove = true;
                        }
                }
                if (!canmove && b.canBearOff(current_player))
                        return true;
                else
                        return false;
        }

        //Moves checker from one position to another,
        //modifying the board object
        public void move(int color, int old_pos, int new_pos)
        {
                //If the move is coming from a bar, remove it from the bar
                //and add it to the spike
                if(old_pos==-1)
                {
                        if(color==white)
                                b.white_bar--;
                        else
                                b.black_bar--;
                        b.setColumn(new_pos, b.getCount(new_pos)+1, color);
                }

                //Move is coming from another spike
                else
                {

                        //Decrease the checkers in the old spike
                        b.setColumn(old_pos, b.getCount(old_pos)-1, color);
                        if(b.getCount(old_pos)==0)
                                b.setColumn(old_pos, 0, neutral);
                        //Increase the checkers on the new spike
                        b.setColumn(new_pos, b.getCount(new_pos)+1, color);
                }
        }

        //Initialize the GUI
        //Sets up all the buttons
        public void setupGUI()
        {
                FButton[0].setBounds(475, 365, 135, 25);
                FButton[0].setVisible(true);
                FButton[0].setText(CANCEL);
                FButton[0].addActionListener(this);
                FButton[0].setEnabled(false);

                FButton[1].setBounds(475, 330, 135, 25);
                FButton[1].setVisible(true);
                FButton[1].setText(ROLL_DICE);
                FButton[1].addActionListener(this);
                FButton[1].setEnabled(true);

                FButton[2].setBounds(475, 295, 135, 25);
                FButton[2].setVisible(true);
                FButton[2].setText(BEAR_OFF);
                FButton[2].addActionListener(this);
                FButton[2].setEnabled(false);

                FButton[3].setBounds(650, 490, 9, 10);
                FButton[3].setVisible(true);
                FButton[3].setText(MOVE1);
                FButton[3].addActionListener(this);
                FButton[3].setEnabled(true);

                FButton[4].setBounds(750, 490, 9, 10);
                FButton[4].setVisible(true);
                FButton[4].setText(MOVE2);
                FButton[4].addActionListener(this);
                FButton[4].setEnabled(true);

                FButton[7].setBounds(475, 260, 135, 25);
                FButton[7].setVisible(true);
                FButton[7].setText(NEW_GAME);
                FButton[7].addActionListener(this);
                FButton[7].setEnabled(true);

                if(status.networked)
                {
                        FButton[5].setBounds(475, 225, 135, 25);
                        FButton[5].setVisible(true);
                        FButton[5].setText(CONNECT);
                        FButton[5].addActionListener(this);
                        FButton[5].setEnabled(true);

                        FButton[6].setBounds(475, y_offset + getInsets().top + 412, 135, 25);
                        FButton[6].setVisible(true);
                        FButton[6].setText(SEND_MSG);
                        FButton[6].addActionListener(this);
                        FButton[6].setEnabled(false);

                        FButton[1].setEnabled(true);

                        msg_input.setBounds(x_offset - getInsets().left, y_offset + getInsets().top + 412, 450, 25);

                        msg_scrollpane.setBounds(x_offset - getInsets().left, y_offset + getInsets().top + 327, 593, 80);
                        msg_display.setEditable(false);
                        msg_display.setLineWrap(true);
                        msg_display.setWrapStyleWord(true);
                }

        }

        //Connect to another Backgammon for network play
        public void Connect()
        {
                String input_ip;
                input_ip = JOptionPane.showInputDialog("Enter computer name or IP address");
                FButton[5].setEnabled(false);
                if(input_ip!=null)
                {
                        if( (comm.portBound == 1 ) &&
                            (input_ip.equalsIgnoreCase("localhost") || 
                             input_ip.equals("127.0.0.1")) )
                        {
                                JOptionPane.showMessageDialog(this,
                                        "Jbackgammon cannot connect to the same instance of itself");
                                FButton[5].setEnabled(true);
                        }
                        else
                        {
                                status.clicker = true;
                                comm.connect(input_ip);
                        }
                }
                else //The user canceled, re-enable the connect button
                        FButton[5].setEnabled(true);
        }

        //Method to send a message through a JOptionPane to the other user
        public void SendMessage()
        {
                String message = msg_input.getText();
                if(message.length()>0)
                {
                        comm.sendmessage(message);
                        msg_display.append("White player: " + message + '\n');
                        //Scroll the text area to the bottom
                        msg_display.scrollRectToVisible(new Rectangle(0, msg_display.getHeight(), 1, 1));
                }
                msg_input.setText("");
        }


        /*=================================================
         * Network Methods 
         * ================================================*/


        //The network player has won
        public void receivelose()
        {
                FButton[7].setEnabled(true);
                JOptionPane.showMessageDialog(this, "You lose!");
        }

        //Connection lost, reset the board for a new game
        public void disconnected()
        {
                JOptionPane.showMessageDialog(this, "Network connection lost!");
                //Allow the person to connect to someone else
                FButton[5].setEnabled(true);
                //Reset the order of connecting
                status.clicker = false;
                //Start listening for connections again
                comm.listen();
                resetGame();
        }

        //Could not connect to an ip
        public void connectionrefused()
        {
                JOptionPane.showMessageDialog(this, "Connection refused.\n\nMake sure the computer name/IP is correct\nand that the destination is running Backgammon in networked mode.");
                status.clicker = false;
                FButton[5].setEnabled(true);
        }

        //The network player has rolled the dice, display them
        public void receiverolls(int i, int j)
        {
                        current_player = black;
                        b.setDice(i, j);
                        b.rolled = true;
                        repaint();
        }

        //The non-network player got sent to the bar, update the board
        public void receivebar(int spike)
        {
                b.setColumn(25 - spike, 0, neutral);
                b.white_bar++;
                repaint();
        }

        //The network player requested a new game, get a response
        public void receiveResetReq()
        {
                int reset = JOptionPane.showConfirmDialog(this, 
                        "The network player has requested a new game.\nDo you want to accept?",
                        "New Game Request",JOptionPane.YES_NO_OPTION);
                comm.sendResetResp(reset);
                if( reset == JOptionPane.YES_OPTION )
                        resetGame();
        }

        //The network player responded to a new game request, process the results
        public void receiveResetResp( int resp )
        {
                if(resp == JOptionPane.NO_OPTION)
                {
                        JOptionPane.showMessageDialog(this, 
                                "Request for new game denied.");
                }
                else
                {
                        JOptionPane.showMessageDialog(this, "Request for new game accepted.");
                        resetGame();
                        Random r = new Random();
                        boolean goesfirst = r.nextBoolean();
                        if (goesfirst)
                        {
                                status.observer = false;
                                StartTurn();
                        }
                        else
                        {
                                status.observer = true;
                                comm.sendendturn();
                        }
                }
        }

        //The network player has moved, update the board
        public void receivemove(int oldpos, int newpos)
        {
                        if((oldpos>0)&&(oldpos<25)&&(newpos>0)&&(newpos<25))
                        {
                                move(black, (25-oldpos), (25-newpos));
                                repaint();
                        }
                        else if(newpos==26)
                        {
                                b.black_bear++;
                                b.setColumn((25-oldpos), b.getCount((25-oldpos))-1, black);
                                repaint();
                        }
                        else if(oldpos==-1)
                        {
                                b.black_bar--;
                                b.setColumn((25-newpos), b.getCount((25-newpos))+1, black);
                                repaint();
                        }
        }

        //The network player has sent an instant message. Display it
        public void receivemessage(String message)
        {
                msg_display.append("Black player: " + message + '\n');
                //Scroll the text area to the bottom
                msg_display.scrollRectToVisible(new Rectangle(0, msg_display.getHeight(), 1, 1));
        }

        //Connection with an instance of Backgammon successfully established
        //Start the game
        public void connected()
        {
                FButton[5].setEnabled(false);
                FButton[6].setEnabled(true);

                //The client initiating the connection
                //decides who goes first
                if(status.clicker)
                {
                        Random r = new Random();
                        boolean goesfirst = r.nextBoolean();
                        if (goesfirst)
                        {
                                status.observer = false;
                                StartTurn();
                        }
                        else
                        {
                                status.observer = true;
                                comm.sendendturn();
                        }
                }
                else
                        status.observer = true;
                repaint();
        }

        //The network player has finished his turn.
        //Start the local player's turn
        public void turnfinished()
        {
                status.observer = false;
                current_player = white;

                b.resetDice();
                b.rolled = false;
                StartTurn();
        
        }

        /*=================================================
         * Overridden Methods 
         * ================================================*/

        //Backgammon class constructor
        //Sets title bar, size, shows the window, and does the GUI
        public Backgammon(boolean n)
        {
                setTitle("Backgammon");
                setResizable(false);
                status = new Status();
                b = new board();
                status.networked = n;

                addMouseListener(new tablaMouseListener(this));

                //Call pack() since otherwise getItsets() does not work until the frame is shown
                pack();

                for(int i=0; i < FButton.length; i++)
                        FButton[i] = new FixedButton(getContentPane(), this);

                if(status.networked)
                {
                        comm = new Communication((CommunicationAdapter)this);
                        comm.listen();
                        setSize(632, 560);
                        //Set up the window for messaging
                        getRootPane().setDefaultButton(FButton[6]);
                        msg_input = new JTextField();
                        getContentPane().add(msg_input);
                        msg_display = new JTextArea();
                        msg_scrollpane = new JScrollPane(msg_display);
                        msg_scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        getContentPane().add(msg_scrollpane);
                }
                else
                {
                        setSize(632, 480);
                }

                //Set up double buffering
                b_bimage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                g_buffer = b_bimage.createGraphics();

                setupGUI();
                show();
        }

        public void actionPerformed(ActionEvent e)
        {
                if(e.getActionCommand().equals(ROLL_DICE))
                        DoRoll();

                else if(e.getActionCommand().equals(CANCEL))
                {
                        status.spike_selected = false;
                        FButton[0].setEnabled(false);
                        FButton[2].setEnabled(false);
                        FButton[3].setVisible(false);
                        FButton[4].setVisible(false);
                        repaint();
                }

                else if(e.getActionCommand().equals(BEAR_OFF))
                        BearOff();

                else if(e.getActionCommand().equals(MOVE1))
                        SuperMove(potmove1, 1);

                else if(e.getActionCommand().equals(SEND_MSG))
                        SendMessage();

                else if(e.getActionCommand().equals(MOVE2))
                        SuperMove(potmove2, 2);

                else if(e.getActionCommand().equals(CONNECT))
                        Connect();
                
                else if(e.getActionCommand().equals(NEW_GAME))
                {
                        if( status.networked )
                        {
                                int conf = JOptionPane.showConfirmDialog(this,
                                        "Send new game request?", "New Game",
                                        JOptionPane.YES_NO_OPTION);
                                if( conf == JOptionPane.YES_OPTION )
                                {
                                        // FIXME: should check for network connection(?)
                                        comm.sendResetReq();
                                }
                        }
                        else
                        {
                               int conf = JOptionPane.showConfirmDialog(this,
                                        "Start a new game?", "New Game",
                                        JOptionPane.YES_NO_OPTION);
                                if( conf == JOptionPane.YES_OPTION )
                                        resetGame();
                        }
                }


        }

        public void paint(Graphics g)
        {
                //Cast the Graphics to a Graphics2D so actual drawing methods
                //are available
                Graphics2D screen = (Graphics2D) g;
                g_buffer.clearRect(0, 0, getWidth(), getHeight());
                drawBoard();
                drawBar();
                drawMen();
                drawBearStats();

                if(b.rolled)
                {
                        if(current_player==white)
                        {
                                drawDice(b.getDice1(), 479, 200, Color.WHITE, Color.BLACK);
                                drawDice(b.getDice2(), 529, 200, Color.WHITE, Color.BLACK);
                        }
                        else
                        {
                                drawDice(b.getDice1(), 479, 200, clr_black, Color.WHITE);
                                drawDice(b.getDice2(), 529, 200, clr_black, Color.WHITE);
                        }
                }

                if(status.networked && !comm.Connected())
                        putString("Waiting for connection...", 15, 50, Color.RED, 15);

                //Blit the buffer onto the screen
                screen.drawImage(b_bimage, null, 0, 0);

                FButton[0].repaint();
                FButton[1].repaint();
                FButton[2].repaint();
                FButton[3].repaint();
                FButton[4].repaint();
                FButton[7].repaint();

                if(status.networked)
                {
                        FButton[5].repaint();
                        FButton[6].repaint();
                        msg_input.repaint();
                        msg_scrollpane.repaint();
                }
        }

        public static void main(String args[])
        {
                JButton ButtonA = new JButton("1P vs. 2P (same computer)");
                JButton ButtonB = new JButton("1P vs. 2P (network)");
                JFrame f = new JFrame("Main Menu");
                JLabel l1 = new JLabel("Backgammon v" + VERSION);
                JLabel l2 = new JLabel("by Tara and Jet");
              

                f.setResizable(false);

                f.addWindowListener(
                        new WindowAdapter()
                        {
                                public void windowClosing(WindowEvent e)
                                {
                                        System.exit(0);
                                }
                        }
                );

                ButtonA.addActionListener(new MainMenuListener(f, false));
                ButtonB.addActionListener(new MainMenuListener(f, true));
                 

                JPanel pane = new JPanel();
                pane.setBorder(BorderFactory.createEmptyBorder(
                                       100, //top
                                       100, //left
                                       100, //bottom
                                       100) //right
                              );
   
               /* public void paintComponent(Graphics g) { 
        super.paintComponent(g);     
        Image pic = new ImageIcon( "paint.png" ).getImage();
        g.drawImage(pic,0,0,null); 
   }  */ 
    
                pane.setLayout(new GridLayout(0, 1));
                pane.add(l1);
                pane.add(l2);
                pane.add(ButtonA);
                pane.add(ButtonB);
                f.getContentPane().add(pane);
                f.pack();
                f.show();
        }

        /*=================================================
         * Drawing Methods 
         * ================================================*/

        public int findX(int spike)
        {
                if(spike<=6)
                        return x_offset+401-(32*(spike-1));
                if(spike<=12)
                        return x_offset+161-(32*(spike-7));
                if(spike<=18)
                        return x_offset+1+(32*(spike-13));
                if(spike<=24)
                        return x_offset+241+(32*(spike-19));
                return -1;
        }

        public int findY(int spike)
        {
                if(spike<=12)
                        return y_offset;
                if(spike<=24)
                        return y_offset+361;
                return -1;
        }

        public void drawBearStats()
        {
                String m1, m2;
                m1 = "White Pieces Beared Off: " + b.white_bear;
                m2 = "Black Pieces Beared Off: " + b.black_bear;

                g_buffer.setColor(Color.BLACK);
                g_buffer.fill(new Rectangle2D.Double(475, 130, 150, 30));

                putString(m1, 455, 150, Color.WHITE, 12);
                putString(m2, 455, 165, Color.WHITE, 12);
        }


        private void putString(String message, int x, int y, Color c, int size)
        {
                g_buffer.setFont(new Font("Arial", Font.BOLD, size));
                g_buffer.setColor(c);
                //g_buffer.drawString(message, x, y);
        }

        private void drawDice(int roll, int x, int y, Color dicecolor, Color dotcolor)
        {
                g_buffer.setColor(dicecolor);
                g_buffer.fill(new Rectangle2D.Double(x, y, 25, 25));

                g_buffer.setColor(dotcolor);

                switch(roll)
                {
                case 1:
                        g_buffer.fill(new Rectangle2D.Double(x+11, y+11, 4, 4));
                        break;
                case 2:
                        g_buffer.fill(new Rectangle2D.Double(x+2, y+2, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+19, y+19, 4, 4));
                        break;
                case 3:
                        g_buffer.fill(new Rectangle2D.Double(x+2, y+2, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+11, y+11, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+19, y+19, 4, 4));
                        break;
                case 4:
                        g_buffer.fill(new Rectangle2D.Double(x+2, y+2, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+19, y+19, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+19, y+2, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+2, y+19, 4, 4));
                        break;
                case 5:
                        g_buffer.fill(new Rectangle2D.Double(x+2, y+2, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+19, y+19, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+19, y+2, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+2, y+19, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+11, y+11, 4, 4));
                        break;
                case 6:
                        g_buffer.fill(new Rectangle2D.Double(x+2, y+2, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+19, y+19, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+19, y+2, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+2, y+19, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+2, y+11, 4, 4));
                        g_buffer.fill(new Rectangle2D.Double(x+19, y+11, 4, 4));
                        break;

                }
        }

        /* drawTriangle: Draws a triangle with the point facing downward, takes in
        left corner coordinates and a number for color 
        hooks: status, g_buffer, old_spike */
        private void drawTriangle(int x, int y, int spike_color)
        {
                if(spike_color==1)
                        g_buffer.setColor(spike_white);
                else
                        g_buffer.setColor(spike_black);

                Polygon tri = new Polygon(new int[]{x,x+15,x+30}, new int[]{y,y+160,y},3);
                g_buffer.fillPolygon(tri);
                if(status.spike_selected)
                {
                        debug_data("TRI: Calling getSpikeNum",0);
                        if(old_spike == getSpikeNum(x,y))
                        {
                                g_buffer.setColor(Color.RED);
                                debug_data("TRI: old_spike = ",old_spike);
                        }
                }
                g_buffer.drawPolygon(tri);
        }


        /* drawTriangleRev: Draws a triangle with the point facing downward,
        takes in left corner coordinates and a number for color
        hooks: status, g_buffer, old_spike */
        private void drawTriangleRev(int x, int y, int spike_color)
        {
                if(spike_color==0)
                        g_buffer.setColor(spike_white);
                else
                        g_buffer.setColor(spike_black);

                Polygon tri = new Polygon(new int[]{x,x+15,x+30}, new int[]{y,y-160,y},3);
                g_buffer.fillPolygon(tri);
                if(status.spike_selected)
                {
                        debug_data("DEBUG: TRIREV: Calling getSpikeNum",0);
                        if(old_spike == getSpikeNum(x,y))
                        {
                                g_buffer.setColor(Color.RED);
                                debug_data("TRIREV: old_spike = ",old_spike);
                        }
                }
                g_buffer.drawPolygon(tri);
        }

        //Draws the Backgammon board onto the buffer
        private void drawBoard()
        {
                //Set the green color
                g_buffer.setColor(new Color(0 , 150, 0));

                //Draw the two halves of the board
                Rectangle2D.Double halfBoardA = new Rectangle2D.Double(x_offset, y_offset, 192, 360);
                Rectangle2D.Double halfBoardB = new Rectangle2D.Double(x_offset+238, y_offset, 192, 360);

                g_buffer.draw(halfBoardA);
                g_buffer.fill(halfBoardA);
                g_buffer.draw(halfBoardB);
                g_buffer.fill(halfBoardB);

                //Draw the bar
                g_buffer.setColor(new Color(128,64,0));
                Rectangle2D.Double bar = new Rectangle2D.Double(x_offset+192, y_offset, 46, 360);
                g_buffer.draw(bar);
                g_buffer.fill(bar);

                g_buffer.setColor(Color.WHITE);
                int spike_color = 0;

                //Draw the spikes
                for(int i=0;i<=180;i+=32)
                {
                        if(spike_color==1)
                                spike_color = 0;
                        else
                                spike_color = 1;

                        drawTriangle(x_offset+i, y_offset, spike_color);
                        drawTriangleRev(x_offset+i, y_offset+360, spike_color);

                        drawTriangle(x_offset+240+i, y_offset, spike_color);
                        drawTriangleRev(x_offset+240+i, y_offset+360, spike_color);
                }
                debug_data("FINISHED THE SPIKES ",0);
        }

        private void drawBar()
        {
                g_buffer.setColor(new Color(100, 50, 0));
                g_buffer.drawRect(x_offset+192,y_offset+120,46,40);
                g_buffer.fill(new Rectangle2D.Double(x_offset+192, y_offset+120, 46, 40));
                g_buffer.fill(new Rectangle2D.Double(x_offset+192, y_offset+200, 46, 40));

                g_buffer.setColor(Color.WHITE);
                g_buffer.fill(new Rectangle2D.Double(x_offset+192, y_offset+160, 47, 40));

                if(b.onBar(white))
                {
                        g_buffer.setColor(clr_white);
                        g_buffer.fill(new Ellipse2D.Double(x_offset+201, y_offset+205, 29, 29));
                        if(b.white_bar>1)
                                putString(String.valueOf(b.white_bar), 232, 285, Color.RED, 15);
                }

                if(b.onBar(black))
                {
                        g_buffer.setColor(clr_black);
                        g_buffer.fill(new Ellipse2D.Double(x_offset+201, y_offset+125, 29, 29));
                        if(b.black_bar>1)
                                putString(String.valueOf(b.black_bar), 232, 205, Color.RED, 15);
                }


        }

        private void drawMen()
        {
                debug_msg("drawMen()");
                for(int spike=1;spike<=12;spike++)
                {
                        if((b.getCount(spike)>0) && (b.getCount(spike)<6))
                        {
                                for(int i=0;i<b.getCount(spike);i++)
                                {
                                        if(b.getColor(spike)==white)
                                                g_buffer.setColor(clr_white);
                                        else
                                                g_buffer.setColor(clr_black);
                                        g_buffer.fill(new Ellipse2D.Double(findX(spike), findY(spike) + i*30, 29, 29));
                                }
                        }
                        if(b.getCount(spike)>5)
                        {
                                for(int i=0;i<5;i++)
                                {
                                        if(b.getColor(spike)==white)
                                                g_buffer.setColor(clr_white);
                                        else
                                                g_buffer.setColor(clr_black);
                                        g_buffer.fill(new Ellipse2D.Double(findX(spike), findY(spike) + i*30, 29, 29));
                                }
                                putString(String.valueOf(b.getCount(spike)), findX(spike)+10, 235, Color.RED, 15);
                        }
                }

                for(int spike=13;spike<=24;spike++)
                {
                        if((b.getCount(spike)>0) && (b.getCount(spike)<6))
                        {
                                for(int i=0;i<b.getCount(spike);i++)
                                {
                                        if(b.getColor(spike)==white)
                                                g_buffer.setColor(clr_white);
                                        else
                                                g_buffer.setColor(clr_black);
                                        g_buffer.fill(new Ellipse2D.Double(findX(spike), findY(spike) - 30 - i*30, 29, 29));
                                }
                        }
                        if(b.getCount(spike)>5)
                        {
                                for(int i=0;i<5;i++)
                                {
                                        if(b.getColor(spike)==white)
                                                g_buffer.setColor(clr_white);
                                        else
                                                g_buffer.setColor(clr_black);
                                        g_buffer.fill(new Ellipse2D.Double(findX(spike), findY(spike) - 30 - i*30, 29, 29));
                                }
                                putString(String.valueOf(b.getCount(spike)), findX(spike)+10, 255, Color.RED, 15);
                        }
                }
        }

        public int getSpikeNum(int spike_x, int spike_y)
        {
                int quad=0;
                int half=0;
                int i=1;

                debug_data("spike_x = ",spike_x);
                debug_data("spike_y = ",spike_y);
                //Find which portion of the board the click occured in
                if(spike_y>=200)
                        half = 1;

                if(spike_x>=238)
                {
                        spike_x -= 238;
                        debug_data("spike_x changed to ",spike_x);
                        quad = 1;
                }
                debug_data("half = ",half);
                debug_data("quad = ",quad);
                //Find how many times we can subtract 32 from the position
                //while remaining positive
                for( i=1; spike_x >= 32; spike_x-=32)
                        i++;

                //Compensate for top/bottom and left/right
                if(half==0)
                {
                        if(quad==0)
                                i = (6-i) + 7;
                        else
                                i = (6-i) + 1;
                }
                else
                {
                        if(quad==0)
                                i += 12;
                        else
                                i += 18;
                }
                // Useful debug statements
                debug_data("getSpikeNum returns ",i);
                return i;
        }

        public void debug_data( String msg, int data)
        {
        }

        public void resetGame()
        {
                //System.out.println("GAME RESET WAS HIT");
                // Reset Backgammon data/
                used_move = 0;
                old_spike = 0;
                current_player = white;
                doublet_moves = 0;

                // Reset buttons
                FButton[0].setEnabled(false);
                FButton[2].setEnabled(false);
                //FButton[7].setEnabled(false);
                FButton[3].setVisible(false);
                FButton[4].setVisible(false);
                FButton[1].setEnabled(true);
                // Re-create the board
                b = new board();

                // Have the Status object reset game values, keep network value
                status.newGame();
                
                repaint();
                
        }
        
}

