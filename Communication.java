 /*
 * [Communication.java]
 * This file contains the communication class and functions for sending Backgammon events between two computers.
 * Authour: Tara Rafi
 */


package Backgammon;

import java.io.*;
import java.net.*;

public class Communication
{
        Socket sock; //The open socket
        boolean connected; //Whether a connection has been established or not
        int portBound; //Binding state of port: -1=error, 0=untried, 1=bound
        PrintWriter out;//The out stream on the socket
        BufferedReader in;//The in stream of the socket
        SocketListener listen; //Port listening thread
        CommunicationAdapter parent; //The parent class
        public final int PORT = 1776; //port to do communication on

        public Communication(CommunicationAdapter p)
        {//Constructor
                sock = null;
                connected = false;
                portBound = 0;
                out = null;
                in = null;
                listen = null;
                parent = p;
        }

        public void listen()
        {//Start listening on the right port
                PortListener watch = new PortListener(this, PORT);
                watch.start();
                portBound = watch.getValidBindState();
        }

        public void ConnectionEstablished(Socket s)
        {//Executes when a connection with another computer is opened
                if (s != null)
                {
                        if (!connected)
                        {
                                sock = s;
                                connected=true;
                                try
                                {
                                        out = new PrintWriter(sock.getOutputStream(), true);
                                        in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                                }
                                catch (IOException e)
                                {
                                        socketerror();
                                }
                                listen = new SocketListener(this, in);
                                listen.start();
                                parent.connected();
                        }
                }
        }

        public void connect(String address)
        {//This connects to another computer
                ConnectThread temp = new ConnectThread(this, address, PORT);
                temp.start();
        }

        public void sendroll(int first, int second)
        {//Send information about a roll of dice
                String packet = "$R:" + first + ":" + second + ":";
                out.println(packet);
        }

        public void sendmove(int oldpos, int newpos)
        {//Send a player's move
                String packet = "$M:" + oldpos + ":" + newpos + ":";
                out.println(packet);
        }

        public void sendmessage(String text)
        {//Send a text message to the other player
                String packet = "$T:" + text;
                out.println(packet);
        }

        public void sendendturn()
        {//Ends the player's turn
                String packet = "$E:";
                out.println(packet);
        }

        public void sendonbar(int spike)
        {//Sends a man on the bar
                String packet = "$B:" + spike + ":";
                out.println(packet);
        }

        public void sendlose()
        {//Tells the other player they lost
                String packet = "$L:";
                out.println(packet);
        }

        public void sendResetReq()
        { //Sends a request for a new game
                String packet = "$N:";
                out.println(packet);
        }

        public void sendResetResp( int reset )
        {//Sends the response for a reset request
                String packet = "$Y:" + reset + ":";
                out.println(packet);
        }

        public boolean Connected()
        {//Returns the state of the connection
                return connected;
        }

        public void onGetPacket(String packet)
        {//This parses the packets received
                String temp = packet.substring(1,2);//Header byte
                if (temp.equals("R"))
                {
                        int firstroll = Integer.parseInt(packet.substring(3, packet.indexOf(":", 3)));
                        int secondroll = Integer.parseInt(packet.substring(packet.indexOf(":", 3) + 1,
                                                          packet.indexOf(":", packet.indexOf(":", 3) + 1)));
                        parent.receiverolls(firstroll, secondroll);
                }
                else if(temp.equals("M"))
                {
                        int oldpos = Integer.parseInt(packet.substring(3, packet.indexOf(":", 3)));
                        int newpos = Integer.parseInt(packet.substring(packet.indexOf(":", 3) + 1, packet.indexOf(":",
                                                      packet.indexOf(":", 3) + 1)));
                        parent.receivemove(oldpos, newpos);
                }
                else if(temp.equals("T"))
                {
                        temp = packet.substring(3);
                        parent.receivemessage(temp);
                }
                else if(temp.equals("E"))
                {
                        parent.turnfinished();
                }
                else if(temp.equals("B"))
                {
                        int spike = Integer.parseInt(packet.substring(3, packet.indexOf(":", 3)));
                        parent.receivebar(spike);
                }
                else if(temp.equals("L"))
                {
                        parent.receivelose();
                }
                else if(temp.equals("N"))
                {
                        parent.receiveResetReq();
                }
                else if(temp.equals("Y"))
                {
                        int response = Integer.parseInt(packet.substring(3, packet.indexOf(":",3)));
                        parent.receiveResetResp(response);
                }
                else
                {
                        //Illegal Packet
                }
        }
        public void socketerror()
        {//Gets called when there's an error writing/reading on the socket
                connected = false;
                parent.disconnected();
        }

        public void connrefused()
        {//Gets called when there's an error connecting
                parent.connectionrefused();
        }
}

class PortListener extends Thread
{//This thread listens on a port for connections
        private int portBound;
        Socket sock;
        int port;
        Communication parent;
        public PortListener(Communication p, int prt)
        {
                port = prt;
                parent = p;
                portBound = 0; // Untried value
        }

        public synchronized int getValidBindState()
        {
                while(portBound == 0)
                {
                        try {
                        this.wait();
                        }
                        catch (InterruptedException e) {}
                }
                return portBound;
        }

        public synchronized void setBindState( int newState )
        {
                portBound = newState;
                this.notify();
        }

        public void run()
        {
                ServerSocket serv = null;
                try
                {
                        serv = new ServerSocket(port);
                }
                catch (UnknownHostException e)
                {
                        setBindState(-2);
                        return; //System.out.println("Unknown Host");
                }
                catch (BindException e)
                {
                        setBindState(-1);
                        //System.out.println(e.getMessage());
                        return;
                }
                catch (IOException e)
                {
                        setBindState(-3);
                        return; //System.out.println("I/O error");
                }
                setBindState(1);
                Socket sock = null;
                try
                {
                        sock = serv.accept();
                        serv.close();
                }
                catch (IOException e)
                {
                        return;
                }
                parent.ConnectionEstablished(sock);
        }
}

class SocketListener extends Thread
{//Once a connection is established, this thread listens for packets
        BufferedReader in;
        Communication parent;

        public SocketListener(Communication p, BufferedReader i)
        {
                parent = p;
                in = i;
        }

        public void run()
        {
                String input = null;
                while (true)
                {
                        try
                        {
                                input = in.readLine();
                        }
                        catch (IOException e)
                        {
                                parent.socketerror();
                                return;
                        }

                        if (input == null)
                        {
                                parent.socketerror();
                                return;
                        }

                        parent.onGetPacket(input);
                        input = null;
                }
        }

}

class ConnectThread extends Thread //This thread connects to a specified computer
{
        Communication parent;
        String address;
        int port;

        public ConnectThread(Communication p, String a, int prt)
        {
                parent = p;
                address = a;
                port = prt;
        }

        public void run()
        {
                Socket s=null;
                try
                {
                        s = new Socket(address, port);
                }
                catch (IOException e)
                {
                        parent.connrefused();
                        return;
                }
                parent.ConnectionEstablished(s);
        }
}
