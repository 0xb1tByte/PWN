package proxy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author alaa 
 TODO : 
 1 - Proxy ==> DONE 
This class intercepts the traffic between the "Game Server" & "Client" , 
and Forward the packets between them using threads

 2 - Parsing Packets ===> Not Completed Yet!
 */
public class Proxy {

    
// ============================================== // 
                 /// Class Variables
// ============================================= //   
    
    // Proxy Variables
    private ServerSocket proxySocket; // server socket
    private int portNumber; // this port should be the port that the client is using to connent to the Game Server

    // Client Variables
    private Socket clientSocket; // Client socket
    private String clientAddress; // to store the client address
    private InputStream fromClient; // Data sent by Client to the Proxy
    private OutputStream toClient; // Data sent by the Proxy to the client (the data that we are forwarding from the Game Server)

    // Game Server Variables
    private Socket gameServerSocket; // Game Server socket
    private String gameServerHostName = ""; // use your game server's Hostname here 
    private int gameServerPort; // the port that the Game Server is listening on 
    private InputStream fromGameServer; // Data sent by Game Server to the Proxy
    private OutputStream toGameServer; // Data sent by Proxy to the Game Server (the data that we are forwarding from the Client)

    // Creating buffers for client-to-server and server-to-client communication.
    final byte[] request = new byte[500];
    final byte[] reply = new byte[2000];

    // Location Packet Variables ==> Should be Moved to PacketParsing Class
    private boolean locationPacket = false;
    private boolean JMPPacket = false;
    private boolean MsgPacket = false;
    private boolean MANAPacket = false;

// ============================================== // 
                 /// MAIN FUNCTIONS
// ============================================= //    
    private void listen() throws IOException, ClassNotFoundException {
        // FOR DEBUGGING : 
        // System.out.println("Inside Listen");
        while (true) {
            try {
                System.out.println("[+] Waiting for Client on port: " + proxySocket.getLocalPort());
                // 3 - Accept the connection from the client
                this.clientSocket = this.proxySocket.accept();
                // getting the client IP 
                this.clientAddress = this.clientSocket.getInetAddress().getHostAddress();
                System.out.println("[+] New connection from " + clientAddress); // PASSED

                // 4 - Read data from the client via an InputStream obtained from the client socket. ==> will be used in the Thread
                this.fromClient = clientSocket.getInputStream();

                // 5 - Send data to the client via the client socket’s OutputStream. ==> will be used in the Thread
                this.toClient = clientSocket.getOutputStream();

                // 6 - Making the Connection to the Game Server
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

                // 7 - Creating Threads to read Cleint'data and pass them to the Game Server
                // we are using seperate threads for request (Client side) and reply (Server Side)
                new Thread() {
                    public void run() {
                        int bytes_read;
                        try {
                            // while the Client is sending streams , forward them to the Game Server 
                            // read() : it returns the total number of bytes read into the buffer, 
                            // or -1 if there is no more data because the end of the stream has been reached.
                            while ((bytes_read = fromClient.read(request)) != -1) {

                                // check if the packet is Location Packet
                                locationPacket = isItLocationPacket(request);
                                if (locationPacket == true) {
                                    System.out.println("Location Packet has been captured!");
                                    //  System.arraycopy(source_arr, sourcePos, dest_arr, destPos, len); 
                                    // System.arraycopy(changePlayerLocation(request), 0, request, 0, request.length); 
                                } // end if  

                                // check if the packet is Jump Packet                                
                                JMPPacket = isItJMPPacket(request);
                                if (JMPPacket == true) {
                                    System.out.println("Jump Packet has been captured!");
                                    //  System.arraycopy(source_arr, sourcePos, dest_arr, destPos, len); 
                                    // System.arraycopy(changePlayerLocation(request), 0, request, 0, request.length); 
                                } // end if  

                                // NOT TESTED YET       
                                // check if the packet is MANA Packet
                                MANAPacket = isItMANAPacket(request);
                                if (MANAPacket == true) {
                                    System.out.println("MANA Packet has been captured!");
                                    //  System.arraycopy(source_arr, sourcePos, dest_arr, destPos, len); 
                                    //System.arraycopy(changeMANA(request), 0, request, 0, request.length); 
                                } // end if              

                                // NOT TESTED YET     
                                // check if the packet is Message Packet
                                MsgPacket = isItMsgPacket(request);
                                if (MsgPacket == true) {
                                    System.out.println("Message Packet has been captured!");
                                    //  System.arraycopy(source_arr, sourcePos, dest_arr, destPos, len); 
                                    //System.arraycopy(writeMSG(request), 0, request, 0, request.length); 
                                } // end if 

                                toGameServer.write(request, 0, bytes_read);
                                System.out.println("[+] Client is sending " + bytes_read + " Bytes to the Game Server:\n" + printHex(request));

                                // FOR DEBUGGING : 
                                //  System.out.println("Bytes: ");
                                //  printByteArray(request);
                                //  System.out.println("Binary: ");   
                                //  printByteAsBits(request);   
                                //  System.out.println("");
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

                // 8 - Reading the Game Server's response , then forward it to the Client 
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
    
    // ============== Identifying Packets Functions ========== //    

    private static boolean isItMsgPacket(byte[] request) {
        boolean condition = false;
        if ((request[0] == 35) && (request[1] == 42)) {
            condition = true;
        } // end if     
        return condition;
    } //  isItLocationPacket()

    private static boolean isItLocationPacket(byte[] request) {
        boolean condition = false;
        if ((request[0] == 109) && (request[1] == 118)) {
            condition = true;
        } // end if     
        return condition;
    } //  isItLocationPacket()

    private static boolean isItJMPPacket(byte[] request) {
        boolean condition = false;
        if ((request[0] == 106) && (request[1] == 112)) {
            condition = true;
        } // end if     
        return condition;
    } //  isItLocationPacket()

    private static boolean isItMANAPacket(byte[] request) {
        byte p1 = (byte) (Integer.parseInt("6d", 16) & 0xff);
        byte p2 = (byte) (Integer.parseInt("61", 16) & 0xff);
        boolean condition = false;
        if ((request[0] == p1) && (request[1] == p2)) {
            condition = true;
        } // end if     
        return condition;
    } //  isItLocationPacket()
    
    // ============== Injection Packets Functions ========== //
    
    private static byte[] writeMSG(byte[] locationPacket) throws IOException {
        byte[] newMSG = locationPacket.clone();
        byte temp = newMSG[4];
        newMSG[4] = newMSG[5];
        newMSG[4] = temp;
        return newMSG;

    } // writeMSG

    private static byte[] changeMANA(byte[] locationPacket) {

        byte[] newMANA = locationPacket.clone();
        newMANA[2] = (byte) 35;
        return newMANA;

    } // end hangeMANA

    private static byte[] changePlayerLocation(byte[] locationPacket) throws IOException {
        //locationPacket
        // coordinates are for position : In town , high in the sk
        // FOR DEBUGGING : 
        // GunShopOwner coordinates:        -37463.0,   -18050.0,   2416.0
        // - GoldenEgg3:           24512.0,    69682.0,   2659.0
        float X = (float) -43655.0;
        float Y = (float) -18050.0;
        float Z = (float) 2416.0;
        byte[] xByte = floatToByteArray(X);
        //   System.out.println("X = " +  X+ " in byte = ");
        //  printByteArray(xByte);

        byte[] yByte = floatToByteArray(Y);
        // System.out.println("Y = " +  Y+ " in byte = ");
        //  printByteArray(yByte);

        byte[] zByte = floatToByteArray(Z);
        // System.out.println("Z = " +  Z+ " in byte = ");
        // printByteArray(zByte);

        // byte [] newPostions = null;
        // clone the same packet 
        byte[] newPostions = locationPacket.clone();

        // looping over the new array and changing the X,Y,Z coordinates 
        for (int i = 0; i < locationPacket.length; i++) {
            // changing X
            // X is stored in bytes[2-5] 
            // we copy values in reverse order because of little-indian things in network
            if (i == 2) {
                newPostions[i] = xByte[3];
            } // end if

            if (i == 3) {
                newPostions[i] = xByte[2];
            } // end if

            if (i == 4) {
                newPostions[i] = xByte[1];
            } // end if

            if (i == 5) {
                newPostions[i] = xByte[0];
            } // end if

            // changing Y , Y is from bytes[6-9]
            if (i == 6) {
                newPostions[i] = yByte[3];
            } // end if

            if (i == 7) {
                newPostions[i] = yByte[2];
            } // end if

            if (i == 8) {
                newPostions[i] = yByte[1];
            } // end if

            if (i == 9) {
                newPostions[i] = yByte[0];
            } // end if 

            // changing Z , Z is from bytes[10-13] 
            if (i == 10) {
                newPostions[i] = zByte[3];
            } // end if

            if (i == 11) {
                newPostions[i] = zByte[2];
            } // end if

            if (i == 12) {
                newPostions[i] = zByte[1];
            } // end if

            if (i == 13) {
                newPostions[i] = zByte[3];
            } // end if 

            // copy the rest of packet in the same positions 
            // newPostions [i] = locationPacket[i];
        } // end for 

        return newPostions;
    } // end changePlayerLocation

    Proxy(int port) throws IOException, ClassNotFoundException {
        // 1 - Create a proxy socket and bind it to a specific port number
        // we set the port for our Proxy 
        this.portNumber = port;
        // we set the same port for the Game Server Socket
        this.gameServerPort = port;
        proxySocket = new ServerSocket(portNumber);
        // FOR DEBUGGING : 
        //System.out.println("Inside constructor");
        // 2 - Listen for a connection from the client 
        listen();
    } // end Constructor

    public static void main(String[] args) throws IOException, ClassNotFoundException {

//      checkingBytesValues();
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
        if (args.length == 0) {
            System.out.println("[!] Please enter the port of the Game Server to start the Proxy");
            System.out.println("[~] Usage: java proxy portNumber");
        } // end if
        // create a proxy instance    
    } // end main

 // ============================================== // 
                /// FOR DEBUGGING 
// ============================================= //  
    private static void printByteAsBits(byte[] byteArray) {
        for (int i = 0; i < byteArray.length; i++) {
            System.out.println(Integer.toBinaryString((byteArray[i] & 0xFF) + 256).substring(1) + " ");
        } // end for
    }  // end printByteAsBits  

    private static byte[] floatToByteArray(float i) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeFloat(i);
        dos.flush();
        return bos.toByteArray();
    } // end floatToByteArray

    private static byte[] hexToByteArray(String i) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        // dos.
        // dos.flush();
        return bos.toByteArray();
    } // end hexToByteArray

    private static String printHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();

    } // end printHex 

    private static void printByteArray(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            System.out.println("position is : " + i);
            System.out.println(bytes[i] + " ");
        }
    } // end printByteArray 

    private static void checkingBytesValues() throws IOException {
        // FOR DEBUGGING : 
        String hex = "6d762fb254c713be46c7004b5c44000020ae00000000007f0000000037633638666633336362";
        byte[] test = hexToByteArray(hex);
        //  printing the bytes values : 
        for (int i = 0; i < test.length; i++) {
            System.out.println("Byte at position " + i + " is : ");
            System.out.println(Integer.toString((test[i] & 0xff) + 0x100, 16).substring(1));
        }

        // FOR DEBUGGING : 
        // printing X & Y & Z values as integer to check values 
        String x = "2fb254c7";
        long XlongBits = Long.valueOf(x, 16).longValue();
        double XdoubleValue = Double.longBitsToDouble(XlongBits);
        System.out.println("X = " + XdoubleValue);

        String y = "13be46c7";
        long YlongBits = Long.valueOf(y, 16).longValue();
        double YdoubleValue = Double.longBitsToDouble(YlongBits);
        System.out.println("Y = " + YdoubleValue);

        String z = "004b5c44";
        long ZlongBits = Long.valueOf(z, 16).longValue();
        double ZdoubleValue = Double.longBitsToDouble(ZlongBits);
        System.out.println("Z = " + ZdoubleValue);

        //double X = -39602.8;
        double X = -54450.2;
        double Y = -18288.0;
        double Z = 2400.28 + 10000;

        System.out.println("X = ");
        byte[] output = new byte[4];
        long lng = Double.doubleToLongBits(X);
        for (int i = 0; i < 4; i++) {
            output[i] = (byte) ((lng >> ((7 - i) * 8)) & 0xff);
            System.out.println(output[i]);
            System.out.println(Integer.toString((output[i] & 0xff) + 0x100, 16).substring(1));
        }
    } // end checkingBytesValues 

} // end Proxy class 
