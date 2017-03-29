package TCPRelayServer;

import Constants.MyConstants;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

/**
 * Created by NCS-KSW on 2017-03-13.
 */
public class SocketAcceptor implements Runnable {
    ServerSocket serverSocket = null;
    M_InetSocketAddress dest = null;

    public SocketAcceptor(M_InetSocketAddress socketAddress, M_InetSocketAddress dest, Executor executor) throws Exception {
        this.dest = dest;

        if(socketAddress.getSSL()) {
            serverSocket = createSSLServerSocket(socketAddress);
        } else {
            serverSocket = createServerSocket(socketAddress);
        }
        executor.execute(this);
    }

    public void run() {
        M_Connection conn;
        Socket tmpSocket, destSocket;

        while(serverSocket != null) {
            try {
                tmpSocket = serverSocket.accept();
                destSocket = dest.getSSL() ? createSSLSocket(dest) : createSocket(dest);
                conn = new M_Connection(tmpSocket, destSocket);
                conn.start();
            } catch (Exception e){
                MyConstants.log("Proxy.SSLServerSocketAcceptor.run(): exception=" + e);
                break;
            }
        }
    }

    Socket createSocket(InetSocketAddress addr) throws Exception {
        return new Socket(addr.getAddress(), addr.getPort());
    }

    Socket createSSLSocket(InetSocketAddress addr) throws Exception {
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        return sslsocketfactory.createSocket(addr.getAddress(), addr.getPort());
    }

    ServerSocket createServerSocket(InetSocketAddress addr) throws Exception {
        return new ServerSocket(addr.getPort(), 10, addr.getAddress());
    }

    ServerSocket createSSLServerSocket(InetSocketAddress addr) throws Exception {
        SSLServerSocketFactory sslserversocketfactory =
                (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        SSLServerSocket sslserversocket;
        sslserversocket=(SSLServerSocket)sslserversocketfactory.createServerSocket(addr.getPort(), 10, addr.getAddress());
        return sslserversocket;
    }
}
