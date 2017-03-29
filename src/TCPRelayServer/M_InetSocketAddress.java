package TCPRelayServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by NCS-KSW on 2017-03-13.
 */
public class M_InetSocketAddress extends InetSocketAddress{
    boolean IS_SSL = false;

    public M_InetSocketAddress(InetAddress addr, int port){
        super(addr, port);
    }

    public M_InetSocketAddress(InetAddress addr, int port, boolean isSSL){
        this(addr, port);
        this.IS_SSL = isSSL;
    }

    public M_InetSocketAddress(int port){
        super(port);
    }

    public M_InetSocketAddress(int port, boolean isSSL){
        this(port);
        this.IS_SSL = isSSL;
    }

    public M_InetSocketAddress(String hostname, int port){
        super(hostname, port);
    }

    public M_InetSocketAddress(String hostname, int port, boolean isSSL){
        this(hostname, port);
        this.IS_SSL = isSSL;
    }

    public boolean getSSL(){
        return IS_SSL;
    }

    public String toString(){
        return super.toString() + " [ssl: " + getSSL() + ']';
    }
}
