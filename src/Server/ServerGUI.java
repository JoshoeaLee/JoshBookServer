package Server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * GUI class for the Server side. 
 * Will display any activities performed by the client-side users.
 */
public class ServerGUI extends Application {

    public ListView<String> dataView = new ListView<>();  //ListView to display client activities
    ObservableList<String> serverMessages = FXCollections.observableArrayList();   //Observable List containing records of all client activities
    private ServerService serverService;  //A Service which allows the JavaFX gui to dynamically update.
    Server server; //Server class which is responsible for accepting client connections and then firing them off to server threads.

    @Override
    public void start(Stage primaryStage) throws Exception{

        //Root Pane
        BorderPane root = new BorderPane();

        //Grab SideBar
        VBox serverSidebar = this.makeSidebar();
        root.setLeft(serverSidebar);

        //Add Main Display Table
        this.dataView.setPlaceholder(new Label("JoshBook Server"));
        this.dataView.setItems(serverMessages);
        root.setCenter(dataView);

        //Scene setup
        Scene scene = new Scene(root,600,500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JoshBook Server");
        primaryStage.show();
    }



    /**
     * Configures the sidebar used for the server-side UI.
     * @return a VBox which is filled with boxes used to control the server
     */
    private VBox makeSidebar(){

         //Port Number Selection Box////////////////////////////////////////////////////////////////////////////////////////////////////////
         Text portLabel = new Text("Select a Port Number: ");
         TextField portField = new TextField();        

         VBox portBox = new VBox(10);
         portBox.getChildren().addAll(portLabel, portField);
         portBox.setMinHeight(160);
         portBox.setAlignment(Pos.CENTER);
         portBox.setBackground(new Background(new BackgroundFill(Color.BEIGE, CornerRadii.EMPTY, Insets.EMPTY)));
 
 
         //Start/Stop Server Box////////////////////////////////////////////////////////////////////////////////////////////////////////
         Text serverInstructions = new Text("Press to start your server.");
         Button startServer = new Button("Start Server");
         Button stopServer = new Button("Stop Server");
 
          
         VBox serverControlBox = new VBox(10);
         serverControlBox.getChildren().addAll(serverInstructions, startServer, stopServer);
         serverControlBox.setMinHeight(160);
         serverControlBox.setAlignment(Pos.CENTER);
         serverControlBox.setBackground(new Background(new BackgroundFill(Color.LEMONCHIFFON, CornerRadii.EMPTY, Insets.EMPTY)));

		
         //Giving the buttons functionality and making responsive server side instructions.
         this.addServerButtonFunctionalities(startServer, stopServer, serverInstructions, portField);

         //SideBar
         VBox sideBar = new VBox(10);
         sideBar.getChildren().addAll(portBox, serverControlBox);
        
         return sideBar;
    }

    /**
     * Gives the startServer, stopServer button functionality. 
     * Updates serverInstructions based on what button is pressed.
     * Uses the port number from the portField to connect to the client.
     */
    public void addServerButtonFunctionalities(Button startServer, Button stopServer, Text serverInstructions, TextField portField){

         //START SERVER FUNCTIONALITY
         startServer.setOnAction(e->{
            int portNumber = 0;
            try{
               portNumber = Integer.parseInt(portField.getText());
               try{
                serverService = new ServerService(portNumber, serverInstructions, ServerGUI.this);
                serverService.start();
                server = serverService.getServer();
                serverInstructions.setText("Server Started!");
               }
               catch(Exception error){
                serverInstructions.setText("Could not connect!");
               }
            }catch(NumberFormatException error){
                serverInstructions.setText("Please enter a valid port number!");
            }
         });

         //STOP SERVER BUTTON FUNCTIONALITY
         stopServer.setOnAction(e->{
            try{
                if(server==null){
                    System.out.println("No server");
                }
                if(server!=null){
                    server.stopServer();
                }
                serverInstructions.setText("Server Stopped!");
            }
            catch(Exception error){
                error.printStackTrace();
                serverInstructions.setText("Could not stop server.");
            }
         });
    }

    /**
     * Updates the listview with messages regarding the client's actions.
     * MIRO TAUGHT ME ABOUT RUNNABLE
     */
    public void updateServerMessage(String message){
        Platform.runLater(new Runnable() {
            @Override
            public void run(){
        ServerGUI.this.serverMessages.add(message);
    
    }});
    }

    //Getter
    public void setServer(Server s){
        this.server = s;
    }


    public static void main(String[] args) {
        launch(args);
    }
}