package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.scene.text.Text;

public class Server extends Thread{

    int port;
    ServerSocket listener;
    Text notificationText;

    public Server(int port, Text serverBoxText, Text notificationText) throws IOException{
        this.port = port;
        this.notificationText = notificationText;
        listener = new ServerSocket(port);
        serverBoxText.setText("Server started on port " + port);
        try{
            System.out.println("Initial Connection Made");
            while(true){
                Socket clientSocket = listener.accept();
                new MyServerThread(clientSocket, notificationText).start();
            }
            
        }
        catch(Exception e){
            System.out.println("Error: " + e.getStackTrace());
        }
        finally{
            try {
                listener.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

    public void run(){
        try{
            System.out.println("Second or Later Connection Made");
           while(true){
                Socket clientSocket = listener.accept();
                new MyServerThread(clientSocket, notificationText);
           }
        }
        catch(Exception e){
            System.out.println("Error: " + e.getStackTrace());
        }
        finally{
            try {
                listener.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public void stopServer(){
        System.out.println("Closing Server.java class");
        if(listener!=null){
            try{
                listener.close(); //Close the listener
                listener = null; //Empty listener
                notificationText.setText("Server has been stopped");
            }catch(Exception e){
                System.out.println("Error: " + e.getStackTrace());
            }
        }
       
    }
    
}
