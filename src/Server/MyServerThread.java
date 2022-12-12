package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Map;

import javafx.scene.text.Text;

public class MyServerThread extends Thread {

    Socket clientSocket;
    Text notificationText;
    String clientFName;
    String clientLName;
    Connection connection = null;
    BufferedReader reader;
    PrintWriter writer;
    Server server;

    String timeStamp;
    String ipAddress;

    public MyServerThread(Socket clientSocket, Text notificationText, Server server){
        this.clientSocket = clientSocket;
        this.notificationText = notificationText;
        this.server = server;
    }

    public void run(){
       
        try{

            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            System.out.println("A client request received at " + clientSocket);

        

            //Setting up Connection
            String dbUser = "root";
            String dbPass = "";
            Class.forName("com.mysql.jdbc.Driver");
            String url ="jdbc:mysql://localhost/JoshBook";     
            connection = DriverManager.getConnection(url, dbUser, dbPass);


            String createOrLog = reader.readLine();
            if(createOrLog.equals("createAccount")){
                clientFName = reader.readLine();
                clientLName = reader.readLine();
                String pWord = reader.readLine();
                String clientIPAddress = clientSocket.getRemoteSocketAddress().toString();
                   //IF USER DOESN'T EXIST 
                   if(!checkUser(clientFName, clientLName)){
                    this.logUser(clientFName, clientLName, pWord, clientIPAddress);
                    writer.println("AccountCreated");

                    User me = new User(clientFName, clientLName, ipAddress, timeStamp);
                    server.addUser(me);
                    server.getOnlineMap().put(me.toString(), clientSocket);

                    this.listenForMessages(me);
                }
                else{
                    writer.println("AccountFailure");
                    System.out.println("Failure");
                    //CLOSE SERVER
                }
            }
            else if(createOrLog.equals("login")){
                clientFName = reader.readLine();
                clientLName = reader.readLine();
                String clientPass = reader.readLine();
                //IF LOGIN INFO IS CORRECT
                if(checkLogIn(clientFName, clientLName, clientPass)){
                    writer.println("loginSuccess");
                    System.out.println("LoginSuccess");

                    User me = new User(clientFName, clientLName, ipAddress, timeStamp);
                    server.addUser(me);                    
                    server.getOnlineMap().put(me.toString(), clientSocket);

                    this.listenForMessages(me);
                }
                else{
                    writer.println("loginFail");
                    System.out.println("Login Failure");
                }

            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void listenForMessages(User me) throws IOException{
        notificationText.setText(me.toString() + " has entered the server.");
        writer.println("Welcome " + me.toString());
        writer.println(me.toString());


        //Tell client how many other users there are online right now
        writer.println(server.getOnlineMap().size());

        //SEND ALL THE ONLINE USERS
        for(String userID: server.getOnlineMap().keySet()){
            if(!userID.equals(me.toString())){
                writer.println(userID);
            }

        }

        writer.println("Online Users Populated");

        //The actual Listening bit
         while(true){
             String line = reader.readLine();
             if(line.equals("NEW_USER_ENTRANCE")){

             
                for(Socket client: server.getOnlineMap().values())
                {
                    PrintWriter tempWriter = new PrintWriter(client.getOutputStream(), true);
                    for(String user: server.getOnlineMap().keySet()){
                        tempWriter.println("NEW_USER_WARNING");
                        tempWriter.println(user);
                    }
                }
             }
             else if(line.equals("INCOMING_MESSAGE_X9%(*")){
                System.out.println("I made it here");
                String recepient = reader.readLine();
                System.out.println(recepient);

                
                String message = reader.readLine();
                System.out.println(message);


                Socket recepientSocket = server.getOnlineMap().get(recepient);
                PrintWriter tempWriter = new PrintWriter(recepientSocket.getOutputStream(), true);
                tempWriter.println("MESSAGE_INCOMING");
                tempWriter.println(message);
                this.logMessage(message, me.toString(), recepient);
             }
             else{
               // this.logMessage(line, clientFName, clientLName);
               System.out.println("This shoudln't get called");

             }
         }

    }

    public void logUser(String fName, String lName, String pWord, String IPAddress) throws SQLException{

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String timeStampString = timestamp.toString();

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO JoshBook.users(`f_name`, `l_name`, `p_word`, `creation_timestamp`, `creation_ip`) VALUES (?, ?, ?, ?, ?);");
        preparedStatement.setString(1, fName);
        preparedStatement.setString(2, lName);
        preparedStatement.setString(3, pWord);
        preparedStatement.setString(4, timeStampString);
        preparedStatement.setString(5, IPAddress);

        preparedStatement.executeUpdate();

        this.timeStamp = timeStampString;
        this.ipAddress = IPAddress;

    }



    public void logMessage(String message, String sender, String receiver){

        if(message.equals("Client Closing")){
            try{
                clientSocket.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else{
            try{
                Timestamp messageTimestamp = new Timestamp(System.currentTimeMillis());
                String messageTimestampString = messageTimestamp.toString();

                PreparedStatement preparedStatementInsert = connection.prepareStatement("INSERT INTO JoshBook.messages(`message`, `time_stamp`, `message_status`, `sender`, `receiver`) VALUES (?, ?, ?, ?, ?);");
                preparedStatementInsert.setString(1, message);
                preparedStatementInsert.setString(2, messageTimestampString);
                preparedStatementInsert.setString(3, "Unread");
                preparedStatementInsert.setString(4, sender);
                preparedStatementInsert.setString(5, receiver);
                preparedStatementInsert.executeUpdate();                
            }
            catch(Exception e){
                e.printStackTrace();
                System.out.println("Insertion Error: " + e);
            }

        }
        

    }

    public void stopServer(ServerSocket serverSocket) throws IOException{
        serverSocket.close();
        serverSocket = null;
    }

    /**
     * Checks to see if a user already exists within the database or not
     * @param fName 
     * @param lName
     * @return True if user exists, False if they don't
     * @throws SQLException
     */
    public boolean checkUser(String fName, String lName) throws SQLException{

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `users` WHERE `f_name` = ? AND `l_name` = ?;");
        preparedStatement.setString(1, fName);
        preparedStatement.setString(2, lName);
        ResultSet rs = preparedStatement.executeQuery();

        if(rs.isBeforeFirst()){
            return true;
        }
        else{
            return false;
        }

    }

    public boolean checkLogIn(String fName, String lName, String pWord) throws SQLException{
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `users` WHERE `f_name` = ? AND `l_name` = ? AND `p_word` = ?;");
        preparedStatement.setString(1, fName);
        preparedStatement.setString(2, lName);
        preparedStatement.setString(3, pWord);
        ResultSet rs = preparedStatement.executeQuery();

        if(rs.isBeforeFirst()){
            while(rs.next()){
                timeStamp = rs.getString("creation_timestamp");
                ipAddress = rs.getString("creation_ip");
            }
           
            return true;
        }
        else{
            return false;
        }

    }

    
}
