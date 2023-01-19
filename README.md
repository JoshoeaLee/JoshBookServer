# JoshBookServer

(For the Client Side of the Messaging App please go here -> https://github.com/JoshoeaLee/JoshBookClient )

A live messaging app which has a server side and client sides with messages being stored in an intermediatory Azure SQL database. 
Individual session keys(AES) are created for clients when they log-in.
These are encrypted using asymmetrical encryption methods (RSA) using the Server Public key to encrypt the AES session keys
and then when the server receives the message, it decrypts using its private key. 

(Originally deployed on an Azure VM but then taken off to save money)

Technologies Used: Java, Java-FX, Azure SQL, RSA/AES Encryption, Azure VM 

<img width="270" alt="Screenshot 2023-01-19 at 11 06 32 PM" src="https://user-images.githubusercontent.com/114985386/213414215-6db07674-ca39-4f79-a368-375c6d622df7.png">
