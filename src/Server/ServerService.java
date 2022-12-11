package Server;


import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.text.Text;

public class ServerService extends Service<String>{


    int portNumber;
    Server server;
    Text serverBoxText;
    Text notificationText;

    public ServerService(int port, Text serverBoxText, Text notificationText){

        this.portNumber = port;
        this.serverBoxText = serverBoxText;
        this.notificationText = notificationText;
        
        setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle(WorkerStateEvent e){
                serverBoxText.setText((String) e.getSource().getValue());
            }
        });

    }

    @Override
    protected Task<String> createTask(){
        
        return new Task<String>(){
            @Override
            protected String call() throws Exception{
                server = new Server(portNumber, serverBoxText, notificationText);
                return "Server Created";
            }
        };
    }

    public void stopService() throws Exception{
        if(server!=null){
            server.stopServer();
        }
        else{
            System.out.println("No server to stop!");
        }
    }

    public Server getServer(){
        return server;
    }



    
}
