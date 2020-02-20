## Description :
This is a simple Java program that act as a **Proxy**  between the **Client** and **Game Server** .<br>
The methodology for building the proxy has been inspired by [this video](https://www.youtube.com/watch?v=iApNzWZG-10&list=PLhixgUqwRTjzzBeFSHXrw9DnQtssdAwgG&index=9) and [this tutorial](http://www.jcgonzalez.com/java-simple-proxy-socket-server-examples) 

## Usage : 
```
java Proxy <portNumber>
```

## NOTES : 
- Please make sure that you append the **hostname** for the **Game Server** that you are using before running the program by modifying this line on the code.
```
private String gameServerHostName = ""; // use your game server's Hostname here 
```
- The code has been tested on **local Server** , and has not been tested on the **Online Game Server**.

## Output 
- 1 - Capturing **Location Packet (id=0x6d76)**
![alt text](https://github.com/0xb1tByte/PWN/blob/master/PwnAdventure/locationCap.png)

- 2 - Capturing **Jump Packet (id=0x6a70)**
![alt text](https://github.com/0xb1tByte/PWN/blob/master/PwnAdventure/jumpCap.png)
