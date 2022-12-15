package Server;

import java.io.IOException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.text.Text;

/**
 * Allows javaFX to be used while a thread is listening
 * When constructed, this class constructs a Server.
 * 
 * Service help I used - https://www.youtube.com/watch?v=Xb6j8VfHxJo
 */
public class ServerService extends Service<String>{

    int portNumber;
    Server server;
    Text serverBoxText;
    ServerGUI serverGUI;

    public ServerService(int port, Text serverBoxText, ServerGUI serverGUI) throws IOException{

        this.portNumber = port;
        this.serverGUI = serverGUI;
        this.serverBoxText = serverBoxText;

        
        //This will only get printed out when the server has been closed.
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
                server = new Server(portNumber, serverBoxText, serverGUI);  //New Server
                return "End of the server";
            }
        };
    }

    //Getter
    public Server getServer(){
        return server;
    }
}
