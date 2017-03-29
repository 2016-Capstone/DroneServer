package TCPRelayServer;

import Constants.MyConstants;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by NCS-KSW on 2017-03-13.
 */
public class M_Relayer implements Runnable{
    final Socket inSocket;
    final Socket outSocket;
    final InputStream in;
    final OutputStream out;
    Thread th = null;
    final java.util.List listeners = new ArrayList();
    String name = null;

    interface Listener {
        void connectionClosed();
    }

    public M_Relayer(Socket inSocket, Socket outSocket, String name) throws Exception {
        this.inSocket = inSocket;
        this.outSocket = outSocket;
        this.name = name;
        this.in = inSocket.getInputStream();
        this.out = outSocket.getOutputStream();
    }

    public void addListener(Listener listener) {
        if(listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void run() {
        byte[] buf = new byte[1024];
        int num;
        StringBuilder stringBuilder;

        try{
            while(th != null){
                if((num = in.read(buf)) == -1){
                    break;
                }

                if(MyConstants.VERBOSE) {
                    MyConstants.log(printRelayedData(MyConstants.toString(inSocket), MyConstants.toString(outSocket), num));
                }

                if(MyConstants.DEBUG) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(new String(buf, 0, num).trim());
                    MyConstants.log(stringBuilder.toString());
                }

                out.write(buf, 0, num);
            }
        } catch (Exception ex) {
            MyConstants.log("Proxy.Relayer.run(): [" + name + "] exception=" + ex + ", in_sock=" +
                    inSocket + ", out_sock=" + outSocket);
        } finally {
            stop();
        }
    }

    public void start() {
        if(th == null) {
            th = new Thread(this, "Proxy.Relayer");
            th.setDaemon(true);
            th.start();
        }
    }

    public void stop() {
        th = null;
        close(inSocket);
        close(outSocket);
    }

    void notifyListeners() {
        for(Iterator it = listeners.iterator(); it.hasNext();) {
            try {
                ((Listener)it.next()).connectionClosed();
            }
            catch(Throwable ex) {
                /*DO NOTHING*/
            }
        }
    }

    static void close(Socket sock) {
        if (sock !=null) {
            try {
                sock.close();
            }
            catch (Exception ex) {
            }
        }
    }

    static String printRelayedData(String from, String to, int num_bytes) {
        StringBuilder sb;
        sb=new StringBuilder();
        sb.append("\n[PROXY] ").append(from);
        sb.append(" to ").append(to);
        sb.append(" (").append(num_bytes).append(" bytes)");
        // log("Proxy.relay()", sb.toString());
        return sb.toString();
    }
}
