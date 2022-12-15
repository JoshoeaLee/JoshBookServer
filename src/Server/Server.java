package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import javax.crypto.SecretKey;
import javafx.scene.text.Text;

/*
 * Opens a server socket and then listens for clients. When a client comes, the server makes a new server thread and lets that thread 
 * deal with the client.
 */
public class Server {

    int port;
    ServerSocket listener;
    HashMap<String, Socket> onlineUsersMap = new HashMap<>();
    HashMap<String, SecretKey> sessionKeys = new HashMap<>();
    ServerGUI gui;

    public Server(int port, Text serverBoxText, ServerGUI serverGUI) throws IOException{

        this.port = port;
        this.gui = serverGUI;
        listener = new ServerSocket(port);  
        serverBoxText.setText("Server started on port " + port);
        serverGUI.setServer(this);

        try{
            System.out.println("Initial Connection Made");
            while(true){
                Socket clientSocket = listener.accept();
                new MyServerThread(clientSocket, this).start();
            }
            
        }
        catch(SocketException e){
           System.out.println("Server socket has been closed!");
        }
        finally{ //Making sure that the server socket gets closed.
            try {
                if(!listener.isClosed()){
                    listener.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * ServerSocket can't be closed directly from here. A thread needs to be made.
     */
    public void stopServer(){
        if(!listener.isClosed()){
            try{
                new CloseServerThread(listener).start();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
     
    }

    //I've chosen the server class to hold my hashmaps of ClientUserId/ClientSocket and ClientUserId/SessionKey
    public HashMap<String, Socket> getOnlineUsers(){
        return onlineUsersMap;
    }

    public HashMap<String, SecretKey> getSessionKeys(){
        return sessionKeys;
    }
    
}
