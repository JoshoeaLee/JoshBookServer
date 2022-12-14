package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.SecretKey;

import javafx.scene.text.Text;

public class Server {

    int port;
    ServerSocket listener;
    Text notificationText;
    ArrayList<User> onlineUsers = new ArrayList<>();
    HashMap<String, Socket> onlineUsersMap = new HashMap<>();
    HashMap<String, SecretKey> sessionKeys = new HashMap<>();

    public Server(int port, Text serverBoxText, Text notificationText, ServerGUI serverGUI) throws IOException{

        this.port = port;
        this.notificationText = notificationText;
        listener = new ServerSocket(port);
        serverBoxText.setText("Server started on port " + port);
        serverGUI.giveMeServer(this);
        try{
            System.out.println("Initial Connection Made");
            while(true){
                Socket clientSocket = listener.accept();
                new MyServerThread(clientSocket, notificationText, this).start();
            }
            
        }
        catch(SocketException e){
           System.out.println("Server socket has been closed!");
           System.out.println(listener);
        }
        finally{
            try {
                if(!listener.isClosed()){
                    listener.close();
                    System.out.println("OH NO!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void stopServer(){
        System.out.println("Closing Server.java class");
        if(!listener.isClosed()){
            try{
                new CloseServerThread(listener).start();
                notificationText.setText("Server has been stopped");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
     
    }

    public ArrayList<User> getOnlineUsers(){
        return onlineUsers;
    }

    public void addUser(User user){
        onlineUsers.add(user);
    }

    public HashMap<String, Socket> getOnlineMap(){
        return onlineUsersMap;
    }

    public HashMap<String, SecretKey> getSessionKeys(){
        return sessionKeys;
    }
    
}
