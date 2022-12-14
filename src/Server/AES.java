package Server;


import java.io.File;
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
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


/**
 *
 * @author ahmed
 */
public class AES {
    
    /**
     * Private Key that I have
     */
    private SecretKey secretkey; 

    private PrivateKey privateKey;
    
    
    public AES() throws NoSuchAlgorithmException, FileNotFoundException 
    {
        this.getPrivateServerKey();
    }
    
    
    /**
	* Step 1. Generate a AES key using KeyGenerator 
    */
    
    public SecretKey generateKey() throws NoSuchAlgorithmException 
    {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        this.setSecretkey(keyGen.generateKey());       
        return this.secretkey;
         
    }
    

    public void getPrivateServerKey() throws FileNotFoundException{
        File file = new File("./lib/serverPrivateKey");
        Scanner sc = new Scanner(file);
        String serverPrivateKey = sc.nextLine();
        sc.close();
        System.out.println(serverPrivateKey);
        byte[] serverPrivate = Base64.getMimeDecoder().decode(serverPrivateKey);
        try {
            PrivateKey privateServerKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(serverPrivate));
            this.setPrivateKey(privateServerKey);
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    

    
    public byte[] encrypt (String strDataToEncrypt) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, IllegalBlockSizeException, 
            BadPaddingException
    {
        Cipher aesCipher = Cipher.getInstance("AES"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!
        aesCipher.init(Cipher.ENCRYPT_MODE, this.getSecretkey());
        byte[] byteDataToEncrypt = strDataToEncrypt.getBytes();
        byte[] byteCipherText = aesCipher.doFinal(byteDataToEncrypt);       
        return byteCipherText;
    }
    
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
Cipher rsaCipher = Cipher.getInstance("RSA"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!				
rsaCipher.init(Cipher.DECRYPT_MODE, this.getPrivateKey());        
byte[] byteDecryptedText = rsaCipher.doFinal(strCipherText);        
return new String(byteDecryptedText);
}   

    /**
     * @return the secretkey
     */
    public SecretKey getSecretkey() {
        return secretkey;
    }

    /**
     * @param secretkey the secretkey to set
     */
    public void setSecretkey(SecretKey secretkey) {
        this.secretkey = secretkey;
    }

    public PrivateKey getPrivateKey(){
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey){
        this.privateKey = privateKey;
    }
}
