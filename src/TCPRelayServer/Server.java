package TCPRelayServer;

import java.net.InetAddress;
import java.net.InterfaceAddress;

/**
 * Created by NCS-KSW on 2017-03-13.
 */
public class Server {

    Proxy proxy;
    InetAddress local = null, remote = null;
    int localPort = 0, remotePort = 0;
    String tmp, tmpAddr, tmpPort;
    boolean VERBOSE = false, DEBUG = false;
    int index;
    String mappingFile = null;
    String[] args;

    public Server (String[] args) {
        this.args = args;
    }

    public void start() {
        try {
            for (int i=0; i< args.length; i++) {
                tmp = args[i];

                if( "-hemp".equals(tmp)) {
                    help(); return;
                }

                if ("-verbose".equals(tmp)) {
                    VERBOSE = true; continue;
                }

                if ("-local".equals(tmp)) {
                    tmpAddr = args[++i];
                    index = tmpAddr.indexOf(':');
                    if(index > -1) {
                        tmpPort = tmpAddr.substring(index + 1);
                        localPort = Integer.parseInt(tmpPort);
                        tmpAddr = tmpAddr.substring(0, index);
                        local = InetAddress.getByName(tmpAddr);
                    } else {
                        local = InetAddress.getByName(args[++i]);
                    }
                    continue;
                }

                if ("-local_port".equals(tmp)) {
                    localPort = Integer.parseInt(args[++i]); continue;
                }

                if ("-remote".equals(tmp)) {
                    tmpAddr = args[++i];
                    index = tmpAddr.indexOf(':');
                    if (index > -1) {
                        tmpPort = tmpAddr.substring(index + 1);
                        remotePort = Integer.parseInt(tmpPort);
                        tmpAddr = tmpAddr.substring(0, index);
                        remote = InetAddress.getByName(tmpAddr);
                    } else {
                        remote = InetAddress.getByName(args[++i]);
                    }
                    continue;
                }

                if ("-remote_port".equals(tmp)) {
                    remotePort=Integer.parseInt(args[++i]);
                    continue;
                }
                if ("-file".equals(tmp)) {
                    mappingFile=args[++i];
                    continue;
                }
                if ("-debug".equals(tmp)) {
                    DEBUG=true;
                    continue;
                }
                help();
                return;
            }
            if (local == null)
                local=InetAddress.getLocalHost();

            proxy = new Proxy(local, localPort, remote, remotePort, VERBOSE, DEBUG, mappingFile);
            proxy.start();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    static void help() {
        System.out.println("Proxy [-help] [-local <local address>] [-local_port <port>] "
                + "[-remote <remote address>] [-remote_port <port>] [-verbose] "
                + "[-file <mapping file>] [-debug]");
    }
}
