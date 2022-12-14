package Server;

public class User {
    
    String firstName;
    String lastName;
    String IPAddress;
    String timeStamp;

    public User(String firstName, String lastName, String IPAddress, String timeStamp){
        this.firstName = firstName;
        this.lastName = lastName;
        this.IPAddress = IPAddress;
        this.timeStamp = timeStamp;
    }



    //Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String iPAddress) {
        IPAddress = iPAddress;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    public String toString(){
        String ipStartSubstring = this.IPAddress.substring(1, 3);
        String endTimestampSubstring = this.timeStamp.substring(this.timeStamp.length()-3);


        return firstName + lastName + "#" +  ipStartSubstring + endTimestampSubstring;
    }

    
}
