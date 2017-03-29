package relay.core;

import Constants.MyConstants;

import java.io.PrintWriter;

/**
 * Created by NCS-KSW on 2017-03-16.
 */
public class User {
    private String userIP;
    private int userPort;
    private MyConstants.PROTO_DVTYPE dvtype = null;
    private PrintWriter printWriter;

    public User(String userIP, int userPort, PrintWriter printWriter) {
        this.userIP = userIP;
        this.userPort = userPort;
        this.printWriter = printWriter;
    }

    public String getUserIP() {
        return userIP;
    }

    public int getUserPort() {
        return userPort;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public void setDvtype(MyConstants.PROTO_DVTYPE dvtype) {
        this.dvtype = dvtype;
    }

    public MyConstants.PROTO_DVTYPE getDvtype() {
        return dvtype;
    }
}
