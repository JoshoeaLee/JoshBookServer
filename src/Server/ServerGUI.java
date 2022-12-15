package Server;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ServerGUI extends Application {

    public static TableView<User> dataView = new TableView<>(); 
    private ServerService serverService;
    Server server;

    @Override
    public void start(Stage primaryStage) throws Exception{

        //Root Pane
        BorderPane root = new BorderPane();

        //Grab SideBar
        VBox serverSidebar = this.makeSidebar();
        root.setLeft(serverSidebar);

        //Add Main Table
        root.setCenter(dataView);

        //Scene setup
        Scene scene = new Scene(root,1000,1000);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JoshBook Server");
        primaryStage.show();
    }


    /**
     * Configures the sidebar used for the server-side UI.
     * @return a VBox which is filled with boxes used to control the server
     */
    private VBox makeSidebar(){

         //Port Number Insertion Box////////////////////////////////////////////////////////////////////////////////////////////////////////
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
 
         
         //stopServer.setDisable(true); //Can't Stop server before starting it.
 
         VBox serverControlBox = new VBox(10);
         serverControlBox.getChildren().addAll(serverInstructions, startServer, stopServer);
         serverControlBox.setMinHeight(160);
         serverControlBox.setAlignment(Pos.CENTER);
         serverControlBox.setBackground(new Background(new BackgroundFill(Color.LEMONCHIFFON, CornerRadii.EMPTY, Insets.EMPTY)));
 
 
         //Database View Selection Box////////////////////////////////////////////////////////////////////////////////////////////////////////
         Text sideBox3Text = new Text("Select Database");

         ChoiceBox<String> databaseChoice = new ChoiceBox<String>();
         databaseChoice.getItems().add("Users");
         databaseChoice.getSelectionModel().selectFirst();

         Button dataSelection = new Button("View Data");
         dataSelection.setOnAction(click->{
            this.retrieveDatabase(databaseChoice.getValue());
         });

         VBox databaseViewBox = new VBox(10);
         databaseViewBox.getChildren().addAll(sideBox3Text, databaseChoice, dataSelection);
         databaseViewBox.setMinHeight(160);
         databaseViewBox.setAlignment(Pos.CENTER);
         databaseViewBox.setBackground(new Background(new BackgroundFill(Color.PAPAYAWHIP, CornerRadii.EMPTY, Insets.EMPTY)));
 
 
         //SideBox 4?////////////////////////////////////////////////////////////////////////////////////////////////////////
         Text sideBox4Text = new Text("What will go here?");
         VBox sideBox4 = new VBox(10);
         sideBox4.getChildren().addAll(sideBox4Text);
         sideBox4.setAlignment(Pos.CENTER);
         sideBox4.setMinHeight(160);
         sideBox4.setBackground(new Background(new BackgroundFill(Color.MISTYROSE, CornerRadii.EMPTY, Insets.EMPTY)));
		
 
 
         //SideBar
         VBox sideBar = new VBox(10);
         sideBar.getChildren().addAll(portBox, serverControlBox, databaseViewBox, sideBox4);
 


         //Start Server Button
                  startServer.setOnAction(e->{
                    int portNumber = 0;
                    try{
                       portNumber = Integer.parseInt(portField.getText());
        
                       try{
                        serverService = new ServerService(portNumber, serverInstructions, sideBox4Text, ServerGUI.this);
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

        //Stop Server Button
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
         return sideBar;
    }

    /**
     * 
     * @param database The selection the user makes regarding which database they want to see 
     * information from (User/Friends/Messages)
     */
    private void retrieveDatabase(String database){
        dataView.getItems().clear();
        dataView.getColumns().clear();
        if(database.equals("Users")){
            this.getUserTable();
        }
    }





        //https://jenkov.com/tutorials/javafx/tableview.html Was the template of code I used for this.
    /**
     * Returns a table which shows the users of JoshBook.
     * @return Data regarding the users of JoshBook
     */
    private void getUserTable(){

        ObservableList<User> userList = FXCollections.observableArrayList();

        userList.add(new User("Josh", "Lee", "123.456", "Aug12"));
        System.out.println(userList.get(0).getFirstName());

        TableColumn<User, String> fNameColumn = new TableColumn<>("First Name");
        fNameColumn.setMinWidth(150);
        fNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));

        TableColumn<User, String> lNameColumn = new TableColumn<>("Last Name");
        lNameColumn.setMinWidth(150);
        lNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));

        TableColumn<User, String> ipColumn = new TableColumn<>("IP Address");
        ipColumn.setMinWidth(200);
        ipColumn.setCellValueFactory(new PropertyValueFactory<User, String>("IPAddress"));

        TableColumn<User, String> timestampColumn = new TableColumn<>("Timestamp");
        timestampColumn.setMinWidth(200);
        timestampColumn.setCellValueFactory(new PropertyValueFactory<User, String>("timeStamp"));
       

        dataView.getItems().addAll(userList);
        dataView.getColumns().addAll(fNameColumn, lNameColumn, ipColumn, timestampColumn);
        dataView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        dataView.setPlaceholder(new Label("No Users Found!"));


    }

    public void giveMeServer(Server s){
        this.server = s;
    }



    public static void main(String[] args) {
        launch(args);
    }
}