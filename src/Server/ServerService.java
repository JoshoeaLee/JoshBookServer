package Server;


import java.io.IOException;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.text.Text;

public class ServerService extends Service<String>{


    int portNumber;
    Server server;
    Text serverBoxText;
    ServerGUI serverGUI;

    public ServerService(int port, Text serverBoxText, ServerGUI serverGUI) throws IOException{

        this.portNumber = port;
        this.serverGUI = serverGUI;
        this.serverBoxText = serverBoxText;

        
        setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle(WorkerStateEvent e){
                System.out.println((String)e.getSource().getValue());
            }
        });

    }

    @Override
    protected Task<String> createTask(){
        
        return new Task<String>(){
            @Override
            protected String call() throws Exception{
                server = new Server(portNumber, serverBoxText, serverGUI);
                return "End of the server";
            }
        };
    }

 

    public Server getServer(){
        return server;
    }



    
}
