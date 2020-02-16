
package proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author alaa
 *  TODO : 
 *          1 - Game2Proxy      ===> using ServerSocket (This class implements server sockets,
 *                                                       our proxy is acting as a Server for the Game Client)
 *          2 - Proxy2Server    ===> using Socket (This class implements client sockets, 
 *                                              our proxy here is acting as a Client for the Game Server)
 *          3 - Parsing Packets ===> NO IDEA YET!
 */
public class Proxy 
{
    public static void main(String[] args) 
    {
      
    } // end main
} // end Proxy class 

class  Game2Proxy 
{
 private ServerSocket server; // server socket
 private int portNumber = 3333; // this port should be the port that the client is using to connent to the Game Server
 private Socket socket; // client socket
 private ObjectInputStream clientData; // Data sent by client to the proxy
 private ObjectOutputStream serverData; // Data sent by the proxy to the client (the data that we are forwarding from the Game Server)
 
 Game2Proxy() throws IOException 
 {
  // 1 - Create a server socket and bind it to a specific port number
  server = new ServerSocket(portNumber);
  // 2 - Listen for a connection from the client 
  listen();
 } // end Game2Proxy
 
 private void listen () throws IOException 
 {
     while (true)
     {
     System.out.println("[+] Waiting for connections");
     // 4 - Accept the connection from the client
     this.socket = this.server.accept();
     // 5 - Read data from the client via an InputStream obtained from the client socket
     this.clientData = new ObjectInputStream(socket.getInputStream());
     // 6 - Send data to the client via the client socket’s OutputStream.
     // NOTE : 
                // * Here I should forward the data that are comming from "Proxy2Server" Class
                // * How to pass data between differnt socket from different classes ? 
     this.serverData = new ObjectOutputStream(socket.getOutputStream());
     // 7 - Close the connection > Don't think I need it!   
     } // end while  
 } // end listen 
 
 
} // end Game2Proxy class
