package proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author alaa 
 * NEW TODO : 
 * 1 - Proxy ==> This class intercepts the traffic between the "Game Server" & "Client", 
                    and Forward the packets between them using threads 
 * 2 - Parsing Packets ===> NO IDEA YET!
 */
public class Proxy {

    // Proxy Variables
    private ServerSocket proxySocket; // server socket
    private int portNumber = 3333; // this port should be the port that the client is using to connent to the Game Server

    // Client Variables
    private Socket clientSocket; // client socket
    private String clientAddress; // to store the game client address
    private InputStream fromClient; // Data sent by client to the proxy
    private OutputStream toClient; // Data sent by the proxy to the client (the data that we are forwarding from the Game Server)

    // Game Server Variables
    private Socket gameServerSocket; // Game Server socket
    private String gameServerHostName = ""; // use your game server's Hostname here 
    private int gameServerPort = 3333;
    private InputStream fromGameServer;
    private OutputStream toGameServer;

    // Creating buffers for client-to-server and server-to-client communication
    final byte[] request = new byte[1024];
    final byte[] reply = new byte[4096];

    Proxy() throws IOException, ClassNotFoundException {
        // 1 - Create a proxy socket and bind it to a specific port number
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
                System.out.println("[+] Waiting for client on port: " + proxySocket.getLocalPort());
                // 4 - Accept the connection from the client
                this.clientSocket = this.proxySocket.accept();
                // getting the client IP 
                this.clientAddress = this.clientSocket.getInetAddress().getHostAddress();
                System.out.println("[+] New connection from " + clientAddress); // PASSED

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
                    out.println("Proxy server cannot connect to " + this.gameServerHostName + ":" + this.gameServerPort + ":\n" + e);
                    out.flush();
                    this.clientSocket.close();
                    continue; // continue listening for incoming connections          
                } // end catch 

                // Get server streams.
                this.fromGameServer = this.gameServerSocket.getInputStream();
                this.toGameServer = this.gameServerSocket.getOutputStream();

                // 8 - Creating Threads to read Cleint'data and pass them to the Game Server
                // we are using seperate threads for request and reply
                new Thread() {
                    public void run() {
                        int bytes_read;
                        try {
                            while ((bytes_read = fromClient.read(request)) != -1) {
                                toGameServer.write(request, 0, bytes_read);
                                System.out.println(bytes_read + "To Server ===>" + new String(request, "UTF-8") + "<===");
                                toGameServer.flush();
                            }
                        } catch (IOException e) {
                        }
                        try {
                            toGameServer.close();
                        } catch (IOException e) {
                        }
                    }
                }.start();
                int bytes_read;
                try {
                    while ((bytes_read = fromGameServer.read(reply)) != -1) {
                        try {
                            Thread.sleep(1);
                            System.out.println(bytes_read + "to_client--->" + new String(request, "UTF-8") + "<---");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        toClient.write(reply, 0, bytes_read);
                        toClient.flush();
                    }
                } catch (IOException e) {
                }
                toClient.close();
            } catch (IOException e) {
                System.err.println(e);
            }
            finally {
                try {
                    if (gameServerSocket != null) {
                        gameServerSocket.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                }
            }
        } // end while  
    } // end listen 

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // create a proxy instance 
        Proxy PWNProxy = new Proxy();
    } // end main
} // end Proxy class 

