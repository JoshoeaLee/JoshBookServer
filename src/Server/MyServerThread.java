package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

/*
 * Listens to messages from clients and then does things based on that.
 */
public class MyServerThread extends Thread {

    Socket clientSocket;  
    String clientFName;   
    String clientLName;
    Connection connection = null;  //Inistantiating the connection to the client
    BufferedReader reader;  //Reading from client
    PrintWriter writer;   //Writing to client
    Server server;   //Reference to server class
    String uniqueUserID;
    PrivateKey privateServerKey;

    String timeStamp;  
    String ipAddress;

   
    public MyServerThread(Socket clientSocket, Server server){
        this.clientSocket = clientSocket;
        this.server = server;
    }

    /*
     * Inistantiates the reader,writer and connection between the server and client and the server and my SQL database.
     */
    public void run(){
       
        try{

  

            //READER AND WRITER
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            //SQL CONNECTION
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url ="jdbc:sqlserver://joshbooksql.database.windows.net:1433;database=XsSALGJjHDKdBTJa;user=XsSALGJjHDKdBTJa@joshbooksql;password=kj99jGP4T79ttQF;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";     
            connection = DriverManager.getConnection(url);

            //CHECK IF USER IS CREATING AN ACCOUNT OR LOGGING IN
            this.createOrLog();    

        }
        catch(NullPointerException e){
            System.out.println("The client has disconnected");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    /*
     * Listens to see if user is going to login or create a new account. 
     * Then performs the necessary action.
     */
    public void createOrLog() throws Exception{
            //ENCRYPTION HANDLER
            EncryptionHandler encryptionHandler = new EncryptionHandler();
            //READS IF CLIENT IS LOGGING IN OR CREATING ACCOUNT
            String createOrLog = reader.readLine();
            if(createOrLog.equals("createAccount")){
                this.accountCreation(encryptionHandler);
            }
            else if(createOrLog.equals("login")){
                this.loginUser(encryptionHandler);
            }
    }

    /*
     * Reads in info from the client and then creates an account for them on the SQL database.
     * Automatically logs them in.
     */
    public void accountCreation(EncryptionHandler encryptionHandler) throws Exception{
        //Info will come in from the client as encrypted and encoded strings.
                String encodedClientFName = reader.readLine();
                String encodedClientLName = reader.readLine();
                String encodedClientPass = reader.readLine();

                //DECODE
                byte[] encryptedFName = Base64.getDecoder().decode(encodedClientFName);
                byte[] encryptedLName = Base64.getDecoder().decode(encodedClientLName);
                byte[] encrtypedPWord = Base64.getDecoder().decode(encodedClientPass);

                //DECRYPT
                clientFName = encryptionHandler.decryptUsingServerPrivateKey(encryptedFName);
                clientLName = encryptionHandler.decryptUsingServerPrivateKey(encryptedLName);
                String clientPass = encryptionHandler.decryptUsingServerPrivateKey(encrtypedPWord);

                //GRAB IP ADDRESS 
                String clientIPAddress = clientSocket.getRemoteSocketAddress().toString();

                //IF USER DOESN'T EXIST 
               if(!checkUser(clientFName, clientLName)){
                   this.logUser(clientFName, clientLName, clientPass, clientIPAddress);
                   writer.println("AccountCreated");

                   User me = new User(clientFName, clientLName, ipAddress, timeStamp);
                   server.getOnlineUsers().put(me.toString(), clientSocket); //Add user and socket to Hashmap
                   server.gui.updateServerMessage("New User " + me.toString() + " has been created");

            //MAKE THREAD LISTEN FOR MESSAGES FROM USER
            this.listenForMessages(me);
        }
        else{
            writer.println("AccountFailure");
            this.createOrLog();
        }
    }

    /*
     * Reads in info from the client, checks it against the SQL database and then logs them in.
     */
    public void loginUser(EncryptionHandler encryptionHandler) throws Exception{
        //INFO COMES IN AS ENCRYPTED AND ENCODED STRINGS
                String encodedClientFName = reader.readLine();
                String encodedClientLName = reader.readLine();
                String encodedClientPass = reader.readLine();

                //DECODE
                byte[] encryptedFName = Base64.getDecoder().decode(encodedClientFName);
                byte[] encryptedLName = Base64.getDecoder().decode(encodedClientLName);
                byte[] encrtypedPWord = Base64.getDecoder().decode(encodedClientPass);

                //DECRYPT
                clientFName = encryptionHandler.decryptUsingServerPrivateKey(encryptedFName);
                clientLName = encryptionHandler.decryptUsingServerPrivateKey(encryptedLName);
                String clientPass = encryptionHandler.decryptUsingServerPrivateKey(encrtypedPWord);

                //IF LOGIN INFO IS CORRECT
                if(checkLogIn(clientFName, clientLName, clientPass)){
                    writer.println("loginSuccess");
                    System.out.println("LoginSuccess");

                    User me = new User(clientFName, clientLName, ipAddress, timeStamp);
                    server.getOnlineUsers().put(me.toString(), clientSocket);  //ADDS USER TO ONLINE USER HASHMAP

                    //LISTEN FOR MESSAGES FROM CLIENT
                    this.listenForMessages(me);
                }
                else{
                    writer.println("loginFail");
                    this.createOrLog();
                }
    }


    /*
     * Step 1: Set client ID
     * Step 2: Grab client's Session AES key and decrypt it
     * Step 3: Populate the client's 'online user' box
     * Step 4: Listen.
     * This method both prepares the server and client for listening and actually listens as well.
     */
    public void listenForMessages(User me) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NullPointerException{
        //Forming the Unique User ID for the client and then sending it to the client.
        uniqueUserID = me.toString();
        writer.println("Welcome " + uniqueUserID);
        writer.println(uniqueUserID);

        //Grab Client Session Key
        String encSessionKey = reader.readLine();
        this.decryptSessionKey(encSessionKey);

        //Tell client which other users there are online right now
        writer.println(server.getOnlineUsers().size());
        //SEND ALL THE ONLINE USERS
        for(String userID: server.getOnlineUsers().keySet()){
            if(!userID.equals(uniqueUserID)){
                writer.println(userID);
            }
        }
        writer.println("Online Users Populated");

        //The actual Listening bit
         while(true){
             String line = reader.readLine();
             if(line.equals("NEW_USER_ENTRANCE")){
                this.takeNewUser();
             }
             else if(line.equals("INCOMING_MESSAGE_X9%(*")){
                this.handleMessage();
            }
                else if(line.equals("LO357GGI1NG_O683UT_T)%#IME")){
                this.logUserOut();
            }
         }
    }

    /*
     * When finding out a new user has logged in, the server will update and then notify each client to update
     * their online users.
     */
    public void takeNewUser() throws IOException{

        server.gui.updateServerMessage(uniqueUserID + " has entered the chatroom.");

                for(Socket client: server.getOnlineUsers().values())
                {
                    PrintWriter tempWriter = new PrintWriter(client.getOutputStream(), true);
                    for(String user: server.getOnlineUsers().keySet()){
                        tempWriter.println("NEW_USER_WARNING");
                        tempWriter.println(user);
                    }
                }
    }

    /*
     * When finding out a user has sent a message. The server will decode, decrypt that message. Store it in a sql database.
     * Encrypt the message using the recepient's key and then encode it and send it to the recepient.
     */
    public void handleMessage() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException  {
        String recepient = reader.readLine();
        String message = "";
        String encMessage = reader.readLine();

        //Decode -> Decrypt Messages. Step 1 DECODE.
        byte[] decodedMessage = Base64.getMimeDecoder().decode(encMessage);

        //SET ENCRYPTION HANDLER'S SESSION KEY AS THIS CLIENT'S SESSION KEY
        EncryptionHandler encryptionHandler = new EncryptionHandler();
        encryptionHandler.setSecretkey(server.getSessionKeys().get(uniqueUserID));
       
        //DECRYPT MESSAGE
        message = encryptionHandler.decrypt(decodedMessage);

        //UPDATE SQL DATABASE AND SERVER LISTVIEW
        this.logMessage(message, uniqueUserID, recepient);
        server.gui.updateServerMessage(uniqueUserID + " has sent a message to " + recepient);


        //Re-encrypt message using recepient's AES KEY and then encode it
         Socket recepientSocket = server.getOnlineUsers().get(recepient);
         SecretKey recepientKey = server.getSessionKeys().get(recepient);
         encryptionHandler.setSecretkey(recepientKey);
        //This line Encrypts and encodes.
         String encodedMessage = Base64.getEncoder().encodeToString(encryptionHandler.encrypt(message));

         PrintWriter tempWriter = new PrintWriter(recepientSocket.getOutputStream(), true);
         tempWriter.println("MESSAGE_INCOMING");
         tempWriter.println(uniqueUserID);
         tempWriter.println(encodedMessage);
    }

    /*
     * When finding out a user has closed their client. The server will 'log them out'. 
     * Updating server messages and 'online user' hashmap to reflect this 
     */
    public void logUserOut() throws IOException{
        String logOutUser = reader.readLine();
        server.getOnlineUsers().remove(logOutUser);
        server.gui.updateServerMessage(logOutUser + " has logged out.");

        for(Socket client: server.getOnlineUsers().values())
    {
        PrintWriter tempWriter = new PrintWriter(client.getOutputStream(), true);
        tempWriter.println("USER_LOGGING_OUT");
        tempWriter.println(logOutUser);
        
    }
    }
   
    /*
     * Takes the encoded and encrypted session key from the client and decodes/decrypts it for server use.
     */
    public void decryptSessionKey(String encSessionKey){
        //Decode -> Decrypt -> Decode Client Key
            byte[] decodedKey = Base64.getMimeDecoder().decode(encSessionKey);

        try {
                EncryptionHandler encryptionHandler = new EncryptionHandler();
                //DECRYPT KEY
                String decryptedKey = encryptionHandler.decryptUsingServerPrivateKey(decodedKey);
                //DECODE KEY 
                byte[] decryptedAndDecodedKey = Base64.getMimeDecoder().decode(decryptedKey);
                SecretKey sessionKey = new SecretKeySpec(decryptedAndDecodedKey, 0, decryptedAndDecodedKey.length, "AES");
                server.getSessionKeys().put(uniqueUserID, sessionKey);
            }catch(Exception e){
                e.printStackTrace();
            }
    }

    /*
     * Adds a new user to the SQL database.
     */
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



    /*
     * Adds a message to the SQL database
     */
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

    /**
     * Checks to see if a user already exists within the database or not
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

    /*
     * Checks to see if a user's given credentials are within the database.
     */
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
