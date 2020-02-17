package proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

 /* NEW TODO : 
 * 1 - Proxy ==> This class intercepts the traffic between the "Game Server" & "Client", 
                    and Forward the packets between them using threads =====================> DONE
 * 2 - Parsing Packets ===> NO IDEA YET!
 */
public class Proxy {

    // Proxy Variables
    private ServerSocket proxySocket; // server socket
    private int portNumber; // this port should be the port that the client is using to connent to the Game Server

    // Client Variables
    private Socket clientSocket; // Client socket
    private String clientAddress; // to store the Client address
    private InputStream fromClient; // Data sent by Client to the Proxy
    private OutputStream toClient; // Data sent by the Proxy to the Client (the data that we are forwarding from the Game Server)

    // Game Server Variables
    private Socket gameServerSocket; // Game Server socket
    private String gameServerHostName = ""; // use your game server's Hostname here 
    private int gameServerPort; // the port that the Game Server is listening on 
    private InputStream fromGameServer; // Data sent by Game Server to the Proxy
    private OutputStream toGameServer; // Data sent by Proxy to the Game Server (the data that we are forwarding from the Client)

    // Creating buffers for client-to-server and server-to-client communication.
    final byte[] request = new byte[1024];
    final byte[] reply = new byte[4096];

    Proxy(int port) throws IOException, ClassNotFoundException {
        // 1 - Create a proxy socket and bind it to a specific port number
        // we set the port for the our Proxy 
        this.portNumber = port;
        // we set the same port for the Game Server Socket
        this.gameServerPort = port;
        proxySocket = new ServerSocket(portNumber);
        // FOR DEBUGGING : 
        //System.out.println("Inside constructor");
        // 2 - Listen for a connection from the client 
        listen();
    } // end Constructor

    private void listen() throws IOException, ClassNotFoundException {
        // FOR DEBUGGING : 
        // System.out.println("Inside Listen");
        while (true) {
            try {
                System.out.println("[+] Waiting for Client on port: " + proxySocket.getLocalPort());
                // 4 - Accept the connection from the client
                this.clientSocket = this.proxySocket.accept();
                // getting the client IP 
                this.clientAddress = this.clientSocket.getInetAddress().getHostAddress();
                System.out.println("[+] New connection from " + clientAddress); 

                // 5 - Read data from the client via an InputStream obtained from the client socket. ==> will be used in the Thread
                this.fromClient = clientSocket.getInputStream();

                // 6 - Send data to the client via the client socket’s OutputStream. ==> will be used in the Thread
                this.toClient = clientSocket.getOutputStream();

                // 7 - Making the Connection to the Game Server
                try {

                    this.gameServerSocket = new Socket(this.gameServerHostName, this.gameServerPort);
                } // end try 
                catch (IOException e) {
                    // If we were not able to connect to the Game Server inform the Client
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(this.toClient));
                    out.println("[!] Proxy server cannot connect to " + this.gameServerHostName + ":" + this.gameServerPort + ":\n" + e);
                    out.flush();
                    this.clientSocket.close();
                    continue; // continue listening for incoming connections          
                } // end catch 

                // Get server streams.
                this.fromGameServer = this.gameServerSocket.getInputStream();
                this.toGameServer = this.gameServerSocket.getOutputStream();

                // 8 - Creating Threads to read Cleint'data and pass them to the Game Server
                // we are using seperate threads for request (Client side) and reply (Server Side)
                new Thread() {

                    public void run() {
                        int bytes_read;
                        try {
                            // while the Client is sending streams , forward them to the Game Server 
                            while ((bytes_read = fromClient.read(request)) != -1) {
                                toGameServer.write(request, 0, bytes_read);
                                System.out.println("[+] Client is sending " + bytes_read + " Bytes to the Game Server:\n" + printHex(request));
                                toGameServer.flush();
                            }
                        } catch (IOException e) {
                        }
                        // if the Client close the connection with the Proxy, close the connection with the Game Server
                        try {
                            toGameServer.close();
                        } catch (IOException e) {
                        }
                    }
                }.start(); // NOTE : when program calls start() method a new Thread is created and code inside run() method is executed

                // 9 - Reading the Game Server's response , then forward it to the Client 
                int bytes_read;
                try {
                    // while the Game Server is sending streams, forward them to the Client 
                    while ((bytes_read = fromGameServer.read(reply)) != -1) {
                        try {
                            Thread.sleep(1);
                            System.out.println("[+] Game Server is sending " + bytes_read + " Bytes to the Client:\n" + printHex(request));
                        } // end try  
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        } // end catch 
                        toClient.write(reply, 0, bytes_read);
                        toClient.flush();
                    } // end while 
                } // end try
                catch (IOException e) {
                } // end catch 
                // if the Game Server close the connection with the Proxy, close the connection with the Client           
                toClient.close();
            } // end try 
            catch (IOException e) {
                System.err.println(e);
            } // end catch 
            // Closing the resources
            finally {
                try {
                    if (gameServerSocket != null) {
                        gameServerSocket.close();
                    } // end if 
                    if (clientSocket != null) {
                        clientSocket.close();
                    } // end if 
                } // end try 
                catch (IOException e) {
                } // end catch 
            } // end finally 
        } // end while  
    } // end listen 

    public static void main(String[] args) throws IOException, ClassNotFoundException {
      
        int portNumber = 3333; // set the port number to the default port for the Game Server (This port is for Authenticating the Player)
        if (args.length > 0) {
            try {
                portNumber = Integer.parseInt(args[0]);
               // create a proxy instance 
                Proxy PWNProxy = new Proxy(portNumber);
            } catch (NumberFormatException e) {
                System.err.println("[!] Argument" + args[0] + " must be an integer.");
                System.exit(1);
            }
        } // end if 
        if (args.length == 0) 
        {
         System.out.println("[!] Please enter the port of the Game Server to start the Proxy");
         System.out.println("[~] Usage: java proxy portNumber");
        } // end if     
    } // end main

    private static String printHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();

    }
} // end Proxy class 
