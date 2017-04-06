package exceptions;

/**
 * Created by NCS-KSW on 2017-04-06.
 */
public class InvalidUserException extends Exception {

    String userIP = null;
    int userPort = -1;

    public InvalidUserException (String userIP, int userPort){
        this.userIP = userIP; this.userPort = userPort;
    }

    public String getMessage(){
        String msg = "Invalid user";

        if (userIP != null){
            msg += "==> " + userIP;

            if(userPort != -1) {
                msg += " : "  + userPort;
            }
        }
        return msg;
    }

}
