package relay.thread;

import Constants.MyConstants;
import relay.core.User;
import relay.service.MsgAnalyzer;

import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by NCS-KSW on 2017-03-14.
 */
public class ServerThread extends Thread {
    Socket socket;
    BufferedReader bufferedReader = null;
    PrintWriter printWriter = null;
    String userIP = null;
    int userPort = 0;
    User user = null;
    boolean isFirst = true;


    public ServerThread(Socket socket) {
        this.socket = socket;
        this.userIP = socket.getInetAddress().toString();
        this.userPort = socket.getPort();
    }

    public void run() {
        try {
            service();
        } catch (Exception ex) {
            MyConstants.log("**" + userIP + ":" + userPort + "** connection closed");
            synchronized (MyConstants.USER_TABLE) {
                MyConstants.USER_TABLE.remove(userPort);
            }
        } finally {
            try {
                closeAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void service() throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        printWriter = new PrintWriter(socket.getOutputStream(), true);
        user = new User(userIP, userPort, printWriter);

        String str = null;

        synchronized (MyConstants.USER_TABLE) {
//            MyConstants.USER_TABLE.put(userIP, printWriter);
            MyConstants.USER_TABLE.put(user.getUserPort(), user);
        }

        while (true) {
            str = bufferedReader.readLine();
            if (str.equals("\n")) continue;

            if (str == null) {
                MyConstants.log(userIP + ":" + userPort + " go out");
                synchronized (MyConstants.USER_TABLE) {
                    MyConstants.USER_TABLE.remove(userPort);
                }
                throw new IOException();
            }


            if (isFirst) {
                if (!MsgAnalyzer.IS_YOU(str)) {
                    printWriter.print("Invalid user");
                    MyConstants.log("IS_YOU", userIP + ":" + userPort + "==> Invalid user");
                    throw new IOException();
                } else {
                    int dvtype = MsgAnalyzer.GET_VALUE_DVTYPE(str);
                    if (dvtype == MyConstants.PROTO_DVTYPE.DRONE.ordinal()) {
                        user.setDvtype(MyConstants.PROTO_DVTYPE.DRONE);
                    } else if (dvtype == MyConstants.PROTO_DVTYPE.PHONE.ordinal()) {
                        user.setDvtype(MyConstants.PROTO_DVTYPE.PHONE);
                    } else {
                        printWriter.print("Invalid device");
                        MyConstants.log("IS_YOU", userIP + ":" + userPort + "==> Invalid device");
                        throw new IOException();
                    }
                    printWriter.print("HELLO");
                    printWriter.flush();
                    isFirst = false;
                    MyConstants.log(userIP + ":" + userPort + " is Established");
                    continue;
                }
            }

            MyConstants.log(userIP + ":" + userPort + " =>  " + str);

            int msgtypeValue = MsgAnalyzer.GET_VALUE_MSGTYPE(str);
            if (msgtypeValue == MyConstants.PROTO_MSGTYPE.CMD.ordinal()) {
                MyConstants.log("INPUT TYPE", MyConstants.PROTO_MSGTYPE.CMD.toString());
                sendCommand(str);
            } else if (msgtypeValue == MyConstants.PROTO_MSGTYPE.GPS.ordinal()) {
                MyConstants.log("INPUT TYPE", MyConstants.PROTO_MSGTYPE.GPS.toString());
                sendGPS(str);
            } else if (msgtypeValue == MyConstants.PROTO_MSGTYPE.PICTURE.ordinal()) {
                MyConstants.log("INPUT TYPE", MyConstants.PROTO_MSGTYPE.PICTURE.toString());
            } else {
                MyConstants.log("Invalid MSG");
                continue;
            }

           // broadcast(str);

            /*
            synchronized (MyConstants.CMD_FIFO){
                if(!MyConstants.ADD_MSG(str)) {
                    MyConstants.log(userIP + ": Failed add to queue");
                }
            }*/
        }
    }

    public void broadcast(String msg) {
        synchronized (MyConstants.USER_TABLE) {
            Collection collection = MyConstants.USER_TABLE.values();
            Iterator iter = collection.iterator();
            User target = null;
            while (iter.hasNext()) {
                target = (User) iter.next();
                if (target.getUserPort() == user.getUserPort()) continue;
                PrintWriter pw = target.getPrintWriter();
                pw.print(msg);
                pw.flush();
            }
        }
    }

    public void sendCommand(String msg) {
        String cmd;

        if ((cmd = MsgAnalyzer.GET_VALUE_DATA(msg)) == null) return;

        synchronized (MyConstants.USER_TABLE) {
            Collection collection = MyConstants.USER_TABLE.values();
            Iterator iter = collection.iterator();
            User target = null;
            while (iter.hasNext()) {
                target = (User) iter.next();
                if (target.getUserPort() == user.getUserPort() || target.getDvtype() != MyConstants.PROTO_DVTYPE.DRONE)
                    continue;
                PrintWriter pw = target.getPrintWriter();
                pw.print(cmd);
                pw.flush();
                break;
            }
        }
    }

    public void sendGPS(String msg) {
        String gps;

        if ((gps = MsgAnalyzer.GET_VALUE_DATA(msg)) == null) return;

        gps = MyConstants.PROTO_MSGTYPE_KEY + MsgAnalyzer.VALUE_DELI + "GPS" + MsgAnalyzer.MSG_DELI
                + MyConstants.PROTO_DATA_KEY + MsgAnalyzer.VALUE_DELI + gps;
        synchronized (MyConstants.USER_TABLE) {
            Collection collection = MyConstants.USER_TABLE.values();
            Iterator iter = collection.iterator();
            User target = null;
            while (iter.hasNext()) {
                target = (User) iter.next();
                if (target.getUserPort() == user.getUserPort() || target.getDvtype() != MyConstants.PROTO_DVTYPE.PHONE)
                    continue;
                PrintWriter pw = target.getPrintWriter();
                pw.print(gps);
                pw.flush();
                break;
            }
        }
    }

    public void closeAll() throws IOException {
        if (printWriter != null) {
            printWriter.close();
        }

        if (bufferedReader != null) {
            bufferedReader.close();
        }

        if (socket != null) {
            socket.close();
        }
    }
}
