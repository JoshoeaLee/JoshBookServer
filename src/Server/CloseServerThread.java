package Server;

import java.io.IOException;
import java.net.ServerSocket;

/*
 * Closes server socket.
 */
public class CloseServerThread extends Thread {

    ServerSocket serverSocket;

    public CloseServerThread(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void run(){
        
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
