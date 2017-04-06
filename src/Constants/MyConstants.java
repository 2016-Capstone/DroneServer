package Constants;

import TCPRelayServer.M_InetSocketAddress;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by NCS-KSW on 2017-03-02.
 */
public class MyConstants {

    //public static String CV_VERSION = Core.VERSION;

    static public boolean VERBOSE = false, DEBUG = false;

    static public final String PROTO_DVTYPE_KEY = "DVTYPE";
    public enum PROTO_DVTYPE {
        PHONE, DRONE
    };

    static public final String PROTO_MSGTYPE_KEY = "MSGTYPE";
    public enum PROTO_MSGTYPE {
        CMD, GPS, PICTURE, HELLO;
    }

    static public final String PROTO_DATA_KEY = "DATA";

    public static final int MIN_THREAD_POOL_SIZE = 2;
    public static final int MAX_THREAD_POOL_SIZE = 64;
    public static int BUFSIZE = 1024;
    public static final int DEFAULT_PORT = 443;

       public static final HashMap USER_TABLE = new HashMap();

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd hh:mm");

/*    public static final Queue<String> CMD_FIFO = new LinkedList<>();

    public static final boolean ADD_MSG(String msg) {
        return CMD_FIFO.offer(msg);
    }

    public static final String GET_MSG(){
        return CMD_FIFO.poll();
    }
*/
    public static void log(String method_name, String msg) {
        System.out.println(getCurrentTime() + '[' + method_name + "]: " + msg);
    }

    public static void log(String msg) {
        System.out.println(getCurrentTime() + msg);
    }

    public static String getCurrentTime(){
        return "[" + DATE_FORMAT.format(new Date()).toString() + "] ";
    }

    public static String toString(SocketChannel ch) {
        StringBuilder sb=new StringBuilder();
        Socket sock;

        if (ch == null)
            return null;
        if ((sock=ch.socket()) == null)
            return null;
        sb.append(sock.getInetAddress().getHostName()).append(':').append(sock.getPort());
        return sb.toString();
    }

    public static String toString(InetSocketAddress addr) {
        StringBuilder sb = new StringBuilder();

        if (addr == null)
            return null;
        sb.append(addr.getAddress().getHostName()).append(':').append(addr.getPort());
        if (addr instanceof M_InetSocketAddress)
            sb.append(" [ssl=").append(((M_InetSocketAddress) addr).getSSL()).append(']');
        return sb.toString();
    }

   public static String toString(Socket s) {
        if(s == null) return null;
        return s.getInetAddress().getHostName() + ':' + s.getPort();
    }
}
