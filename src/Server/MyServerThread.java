package Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.sql.*;

import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class MyServerThread extends Thread {

    Socket clientSocket;
    String clientFName;
    String clientLName;
    Connection connection = null;
    BufferedReader reader;
    PrintWriter writer;
    Server server;

    String timeStamp;
    String ipAddress;

    String uniqueUserID;
    PrivateKey privateServerKey;
    



    //Key Stuff
        DataOutputStream dataOut;

    public MyServerThread(Socket clientSocket, Server server){
        this.clientSocket = clientSocket;
        this.server = server;
    }

    public void run(){
       
        try{

            AES aes = new AES();

            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            //Setting up Connection
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url ="jdbc:sqlserver://joshbooksql.database.windows.net:1433;database=XsSALGJjHDKdBTJa;user=XsSALGJjHDKdBTJa@joshbooksql;password=kj99jGP4T79ttQF;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";     
            connection = DriverManager.getConnection(url);


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
                    server.getOnlineMap().put(me.toString(), clientSocket);
                    server.gui.updateServerMessage("New User " + me.toString() + " has been created");

                    this.listenForMessages(me);
                }
                else{
                    writer.println("AccountFailure");
                    System.out.println("Failure");
                    //CLOSE SERVER
                }
            }
            else if(createOrLog.equals("login")){
                String encodedClientFName = reader.readLine();
                String encodedClientLName = reader.readLine();
                String encodedClientPass = reader.readLine();

                //DECODE
                byte[] encryptedFName = Base64.getDecoder().decode(encodedClientFName);
                byte[] encryptedLName = Base64.getDecoder().decode(encodedClientLName);
                byte[] encrtypedPWord = Base64.getDecoder().decode(encodedClientPass);

                //DECRYPT
                clientFName = aes.decryptUsingServerPrivateKey(encryptedFName);
                clientLName = aes.decryptUsingServerPrivateKey(encryptedLName);
                String clientPass = aes.decryptUsingServerPrivateKey(encrtypedPWord);

                //IF LOGIN INFO IS CORRECT
                if(checkLogIn(clientFName, clientLName, clientPass)){
                    writer.println("loginSuccess");
                    System.out.println("LoginSuccess");

                    //TAKE IN SESSIONKEY

                    User me = new User(clientFName, clientLName, ipAddress, timeStamp);
                    server.getOnlineMap().put(me.toString(), clientSocket);

                    this.listenForMessages(me);
                }
                else{
                    writer.println("loginFail");
                    System.out.println("Login Failure");
                }

            }

        }
        catch(NullPointerException e){
            System.out.println("The client has disconnected");

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void listenForMessages(User me) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NullPointerException{
        uniqueUserID = me.toString();
        writer.println("Welcome " + uniqueUserID);
        writer.println(uniqueUserID);

        String encSessionKey = reader.readLine();

                //Decode -> Decrypt -> Decode Client Key
                byte[] decodedKey = Base64.getMimeDecoder().decode(encSessionKey);

                try {
                    AES aes = new AES();
                    //DECRYPT KEY
                    String decryptedKey = aes.decryptUsingServerPrivateKey(decodedKey);
                    //DECODE KEY 
                    byte[] decryptedAndDecodedKey = Base64.getMimeDecoder().decode(decryptedKey);
                    SecretKey sessionKey = new SecretKeySpec(decryptedAndDecodedKey, 0, decryptedAndDecodedKey.length, "AES");
                    server.getSessionKeys().put(uniqueUserID, sessionKey);
                }catch(Exception e){
                    e.printStackTrace();
                }
        //Tell client how many other users there are online right now
        writer.println(server.getOnlineMap().size());
        //SEND ALL THE ONLINE USERS
        for(String userID: server.getOnlineMap().keySet()){
            if(!userID.equals(uniqueUserID)){
                writer.println(userID);
            }

        }
        writer.println("Online Users Populated");

        //The actual Listening bit
         while(true){
             String line = reader.readLine();
             if(line.equals("NEW_USER_ENTRANCE")){
                server.gui.updateServerMessage(uniqueUserID + " has entered the chatroom.");


             
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
                String recepient = reader.readLine();
                String message = "";
                String encMessage = reader.readLine();

                //Decode -> Decrypt Messages
                byte[] decodedMessage = Base64.getMimeDecoder().decode(encMessage);

                AES aes = new AES();
                try{
                    aes.setSecretkey(server.getSessionKeys().get(uniqueUserID));
                }catch(Exception e){
                    e.printStackTrace();
                }
                    //DECRYPT MESSAGE
                    message = aes.decrypt(decodedMessage);
                    System.out.println(message);
                    this.logMessage(message, uniqueUserID, recepient);
                    server.gui.updateServerMessage(uniqueUserID + " has sent a message to " + recepient);


                //Re-encrypt message using recepient's AES KEY and then encode it
                //Encrypt AES KEY and then Encode it


                 Socket recepientSocket = server.getOnlineMap().get(recepient);
                 SecretKey recepientKey = server.getSessionKeys().get(recepient);
                 aes.setSecretkey(recepientKey);
                 String encodedMessage = Base64.getEncoder().encodeToString(aes.encrypt(message));

                 PrintWriter tempWriter = new PrintWriter(recepientSocket.getOutputStream(), true);
                 tempWriter.println("MESSAGE_INCOMING");
                 tempWriter.println(uniqueUserID);
                 tempWriter.println(encodedMessage);

                }

                else if(line.equals("LO357GGI1NG_O683UT_T)%#IME")){
                    System.out.println("This got triggered");



                    String logOutUser = reader.readLine();
                    server.getOnlineMap().remove(logOutUser);
                    server.gui.updateServerMessage(logOutUser + " has logged out.");

                    for(Socket client: server.getOnlineMap().values())
                {
                    PrintWriter tempWriter = new PrintWriter(client.getOutputStream(), true);
                    tempWriter.println("USER_LOGGING_OUT");
                    tempWriter.println(logOutUser);
                    
                }


                }
                
            




             
             
         }

    }

    public void logUser(String fName, String lName, String pWord, String IPAddress) throws SQLException{

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String timeStampString = timestamp.toString();

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(f_name, l_name, p_word, creation_timestamp, creation_ip) VALUES (?, ?, ?, ?, ?);");
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

                PreparedStatement preparedStatementInsert = connection.prepareStatement("INSERT INTO messages(message, time_stamp, message_status, sender, receiver) VALUES (?, ?, ?, ?, ?);");
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

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE f_name = ? AND l_name = ?;");
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
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE f_name = ? AND l_name = ? AND p_word = ?;");
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
