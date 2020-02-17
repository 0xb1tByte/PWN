package proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author alaa 
 * NEW TODO :
 * 1 - Proxy ==> This class intercepts the traffic between the "Game Server" & "Client", and Forward the packets between them using threads
 * 2 - Parsing Packets ===> NO IDEA YET!
 * ============================================
 * IGNORED TODO : 
 * 1 - Game2Proxy ===> using ServerSocket (This class implements server sockets, 
 *                      our proxy is acting as a Server for the Game Client) 
 * 2 - Proxy2Server ===> using Socket (This class implements client sockets, 
 *                      our proxy here is acting as a Client for the Game Server) 
 * 3 - Parsing Packets ===> NO IDEA YET!
 */
public class Proxy 
{
    private ServerSocket server; // server socket
    private int portNumber = 3333; // this port should be the port that the client is using to connent to the Game Server
    private Socket socket; // client socket
    private ObjectInputStream clientData; // Data sent by client to the proxy
    private ObjectOutputStream serverData; // Data sent by the proxy to the client (the data that we are forwarding from the Game Server)
    private String clientAddress; // to store the game client address
    
    Proxy () throws IOException, ClassNotFoundException
    {
        // 1 - Create a server socket and bind it to a specific port number
        server = new ServerSocket(portNumber);
         // FOR DEBUGGING : 
            //System.out.println("Inside constructor");
        // 2 - Listen for a connection from the client 
        listen();
    } // end Constructor
    
    
    private void listen() throws IOException, ClassNotFoundException 
    {
       // FOR DEBUGGING : 
            // System.out.println("Inside Listen");
        while (true) 
        {
            System.out.println("[+] Waiting for client on port: " + server.getLocalPort());
            // 4 - Accept the connection from the client
            this.socket = this.server.accept();
            // 5 - Read data from the client via an InputStream obtained from the client socket
            this.clientAddress = this.socket.getInetAddress().getHostAddress();
            System.out.println("[+] New connection from " + clientAddress); // PASSED
            this.clientData = new ObjectInputStream(socket.getInputStream());
             
     // 6 - Send data to the client via the client socket’s OutputStream.
            // NOTE : 
            // * Here I should forward the data that are comming from "Proxy2Server" Class
            // * How to pass data between differnt socket from different classes ? 

     // FOR DEBUGGING : just to check the that client (My Acer) can access the proxy (My MAC)
            // Getting the client message ==> NOT WORKING
            String message = this.clientData.readObject().toString();
            System.out.println("[+] Cleint: " + message);
            //create ObjectOutputStream object , to send message back to the client 
                // here we should forward the packets that are coming from Game Server through this object to the Cleint
                // ^^ not sure about this, but this is my first assumption
            this.serverData = new ObjectOutputStream(socket.getOutputStream());
            // write object to Socket ==> NOT WORKING
            this.serverData.writeObject("[+] Server : Hi Client " + message);
            // writing strings ? 
            // this.serverData.writeUTF("[+] Server : Hi Client " + message);
 
             //close resources
            this.clientData.close();
            this.serverData.close();
            socket.close();
            // 7 - Close the connection > Don't think I need it!   
        } // end while  
    } // end listen 

    public static void main(String[] args) throws IOException, ClassNotFoundException 
    {
        // create a proxy instance 
        Proxy PWNProxy = new Proxy();
    } // end main
    
    
    
    
} // end Proxy class 

/* 
* Don't need it for the moment
class Game2Proxy 
{  
} // end Game2Proxy class
*/
