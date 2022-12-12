package Server;

import java.io.IOException;
import java.net.ServerSocket;

public class CloseServerThread extends Thread {

    ServerSocket serverSocket;

    public CloseServerThread(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void run(){
        
        try {
            System.out.println("Close server thread invoked");
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
