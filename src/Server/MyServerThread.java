package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;

import javafx.scene.text.Text;

public class MyServerThread extends Thread {

    Socket clientSocket;
    Text notificationText;
    String clientFName;
    String clientLName;
    Connection connection = null;
    BufferedReader reader;
    PrintWriter writer;

    public MyServerThread(Socket clientSocket, Text notificationText){
        this.clientSocket = clientSocket;
        this.notificationText = notificationText;
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
                    this.listenForMessages();
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
                    this.listenForMessages();
                }
                else{
                    writer.println("loginFail");
                    System.out.println("Login Failure");
                }

            }

        }
        catch(Exception e){
            System.out.println("Error: " + e.getStackTrace());
        }
    }

    public void listenForMessages() throws IOException{
        notificationText.setText(clientFName + " " + clientLName + " has entered the server.");
        writer.println("Welcome " + clientFName + " " + clientLName);
         while(true){
             String line = reader.readLine();
             this.logMessage(line, clientFName, clientLName);
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
    }



    public void logMessage(String message, String FName, String LName){

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
                PreparedStatement preparedStatementRetrieve = connection.prepareStatement("SELECT * FROM `users` WHERE `f_name` = ? AND `l_name` = ?;");
                preparedStatementRetrieve.setString(1, FName);
                preparedStatementRetrieve.setString(2, LName);
                ResultSet rs = preparedStatementRetrieve.executeQuery();
                String userTimestamp = "";
                String userIP = "";
                while(rs.next()){
                    userTimestamp = rs.getString("creation_timestamp");
                    userIP = rs.getString("creation_ip");
                }

                PreparedStatement preparedStatementInsert = connection.prepareStatement("INSERT INTO JoshBook.messages(`message`, `time_stamp`, `message_status`, `user_creation_timestamp`, `user_creation_ip`) VALUES (?, ?, ?, ?, ?);");
                preparedStatementInsert.setString(1, message);
                preparedStatementInsert.setString(2, messageTimestampString);
                preparedStatementInsert.setString(3, "Unread");
                preparedStatementInsert.setString(4, userTimestamp);
                preparedStatementInsert.setString(5, userIP);
                preparedStatementInsert.executeUpdate();                
            }
            catch(Exception e){
                e.printStackTrace();
                System.out.println("Insertion Error: " + e);
            }

        }
        

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
            return true;
        }
        else{
            return false;
        }

    }

    
}
