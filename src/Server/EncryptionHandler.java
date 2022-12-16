package Server;


import java.io.FileNotFoundException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * METHODS INVOLVING ENCRYPTION AND DECRYPTION ARE TAKEN FROM ALI'S CODE.
 * I added on the methods involving the 'Server Private Key'.
 */
public class EncryptionHandler {
    
    private SecretKey sessionKey; 
    private PrivateKey privateKey;
    
    
    /**
     * Constructor of the Encryption Handler class. Gets the Server Private Key on construction.
     */
    public EncryptionHandler() throws NoSuchAlgorithmException, FileNotFoundException 
    {
        this.getPrivateServerKey();
    }
    

        
    /**
     * Reads in the server private key I have made and sets it.
     * @throws FileNotFoundException
     */
    public void getPrivateServerKey() throws FileNotFoundException{
        
        Scanner sc = new Scanner(EncryptionHandler.class.getResourceAsStream("serverPrivateKey.txt"));
        String serverPrivateKey = sc.nextLine();
        sc.close();
        byte[] serverPrivate = Base64.getMimeDecoder().decode(serverPrivateKey);
        try {
            PrivateKey privateServerKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(serverPrivate));
            this.setPrivateKey(privateServerKey);
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    /**
     * Encrypts a message using the AES Session Key set
     * @param strDataToEncrypt The message to encrypt
     * @return A byte array of the encrypted message
     * @author ALI AHMED
     */
    public byte[] encrypt (String strDataToEncrypt) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, IllegalBlockSizeException, 
            BadPaddingException
    {
        Cipher aesCipher = Cipher.getInstance("AES"); 
        aesCipher.init(Cipher.ENCRYPT_MODE, this.getSecretkey());
        byte[] byteDataToEncrypt = strDataToEncrypt.getBytes();
        byte[] byteCipherText = aesCipher.doFinal(byteDataToEncrypt);       
        return byteCipherText;
    }
    
    /**
     * Decrypts a byte array into a string using the session key
     * @param strCipherText
     * @return String message which is decrypted
     * @author ALI AHMED
     */
    public String decrypt (byte[] strCipherText) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, IllegalBlockSizeException, 
            BadPaddingException
    {        
        Cipher aesCipher = Cipher.getInstance("AES"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!				
        aesCipher.init(Cipher.DECRYPT_MODE, this.getSecretkey());        
        byte[] byteDecryptedText = aesCipher.doFinal(strCipherText);        
        return new String(byteDecryptedText);
    }   


    public String decryptUsingServerPrivateKey (byte[] strCipherText) throws 
    NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
    InvalidAlgorithmParameterException, IllegalBlockSizeException, 
    BadPaddingException
    {        
        Cipher rsaCipher = Cipher.getInstance("RSA"); 		
        rsaCipher.init(Cipher.DECRYPT_MODE, this.getPrivateKey());        
        byte[] byteDecryptedText = rsaCipher.doFinal(strCipherText);        
        return new String(byteDecryptedText);
    }   


    //////GETTERS AND SETTERS
    public SecretKey getSecretkey() {
        return sessionKey;
    }

    public void setSecretkey(SecretKey sessionKey) {
        this.sessionKey = sessionKey;
    }

    public PrivateKey getPrivateKey(){
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey){
        this.privateKey = privateKey;
    }
}
