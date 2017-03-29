package relay;

import Constants.MyConstants;
import relay.thread.ServerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by NCS-KSW on 2017-03-14.
 */
public class RelayServer {

    public void start() throws IOException {
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(MyConstants.DEFAULT_PORT);
            MyConstants.log("start()", "Server start");

            while (true) {
                socket = serverSocket.accept();
                ServerThread serverTh = new ServerThread(socket);
                serverTh.start();
                MyConstants.log(socket.getInetAddress() + ":" + socket.getPort() + " is Connected");
            }
        } finally {
            if (socket != null) {
                socket.close();
            }

            if (serverSocket != null) {
                serverSocket.close();
            }

            MyConstants.log("****** Server stop ******");
        }
    }
}
