package TCPRelayServer;

import java.net.Socket;

/**
 * Created by NCS-KSW on 2017-03-13.
 */
public class M_Connection implements M_Relayer.Listener {
    M_Relayer inToOut = null;
    M_Relayer outToIn = null;

    public M_Connection(Socket inSocket, Socket outSocket) throws Exception {
        inToOut = new M_Relayer(inSocket, outSocket, "in-out");
        inToOut.addListener(this);
        outToIn = new M_Relayer(outSocket, inSocket, "out-in");
        outToIn.addListener(this);
    }

    public void start() {
        inToOut.start();
        outToIn.start();
    }

    public void stop() {
        if(inToOut != null) {
            inToOut.stop();
        }
        if(outToIn != null) {
            outToIn.stop();
        }
    }

    public void connectionClosed() {
        stop();
    }
}
