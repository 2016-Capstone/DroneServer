package TCPRelayServer;

import constants.MyConstants;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by NCS-KSW on 2017-03-13.
 */
public class Proxy {

    InetAddress local = null, remote = null;
    int localPort = -1, remotePort = -1;



    String mappingFile = null;
    final HashMap mappings = new HashMap();

    Executor executor;

    public Proxy(InetAddress local, int localPort, InetAddress remote, int remotePort, boolean verbose, boolean debug){
        this.local = local;
        this.localPort = localPort;
        this.remote = remote;
        this.remotePort = remotePort;
        MyConstants.VERBOSE = verbose;
        MyConstants.DEBUG = debug;
    }

    public Proxy(InetAddress local, int localPort, InetAddress remote, int remotePort, boolean verbose, boolean debug, String mappingFile){
        this(local, localPort, remote, remotePort, verbose, debug);
        this.mappingFile = mappingFile;
    }

    void populateMappings(String filename) throws Exception {
        FileInputStream in = new FileInputStream(filename);
        BufferedReader reader;
        String line;
        URI key, value;
        int index;
        boolean sslKey, sslValue;
        final String HTTPS = "https";

        reader = new BufferedReader(new InputStreamReader(in));

        while ((line = reader.readLine()) != null){
            line = line.trim();

            if(line.startsWith("//") || line.startsWith("#") || line.length() == 0){
                continue;
            }

            index = line.indexOf('=');
            if(index == -1){
                throw new Exception("Proxy.populateMappings(): There isn't '=' character in " + line);
            }

            key = new URI(line.substring(0, index));
            sslKey = key.getScheme().trim().equals(HTTPS);

            value = new URI(line.substring(index + 1));
            sslValue = value.getScheme().trim().equals(HTTPS);

            check(key);
            check(value);

            MyConstants.log("key: " + key + ", value: " + value);

            mappings.put(new M_InetSocketAddress(key.getHost(), key.getPort(), sslKey),
                    new M_InetSocketAddress(value.getHost(), value.getPort(), sslValue));
        }
        in.close();
    }

    void check(URI uri) throws Exception {
        if(uri.getScheme() == null){
            throw new Exception("scheme is null in " + uri + ", (valid URI is \"http(s)://<host>:<port>\")");
        }

        if (uri.getHost() == null) {
            throw new Exception(
                    "host is null in " + uri + ", (valid URI is \"http(s)://<host>:<port>\")");
        }

        if (uri.getPort() <= 0) {
            throw new Exception(
                    "port is <=0 in " + uri + ", (valid URI is \"http(s)://<host>:<port>\")");
        }
    }

    public static void log(String method_name, String msg) {
        System.out.println('[' + method_name + "]: " + msg);
    }

    public static void log(String msg) {
        System.out.println(msg);
    }



    public void start() throws Exception{
        Map.Entry entry;
        Selector selector;
        ServerSocketChannel socketChannel;
        M_InetSocketAddress mKey, mValue;

        if(remote != null && local != null){
            mappings.put(new InetSocketAddress(local, localPort), new InetSocketAddress(remote, remotePort));
        }

        if(mappingFile != null){
            try {
                populateMappings(mappingFile);
            } catch (Exception ex) {
                log("Failed reading " + mappingFile);
                throw ex;
            }
        }

        MyConstants.log("\nProxy started at " + new java.util.Date());

        if(MyConstants.VERBOSE){
            MyConstants.log("\nMappings:\n---------");
            for (Iterator it = mappings.entrySet().iterator(); it.hasNext();) {
                entry = (Map.Entry) it.next();
                MyConstants.log(MyConstants.toString((InetSocketAddress) entry.getKey()) + " <--> "
                        + MyConstants.toString((InetSocketAddress) entry.getValue()));
            }
            MyConstants.log("\n");
        }

        selector = Selector.open();

        executor = new ThreadPoolExecutor(MyConstants.MIN_THREAD_POOL_SIZE, MyConstants.MAX_THREAD_POOL_SIZE, 30000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(100));

        for(Iterator it = mappings.keySet().iterator(); it.hasNext();){
            mKey = (M_InetSocketAddress) it.next();
            mValue = (M_InetSocketAddress) mappings.get(mKey);

            if(mKey.getSSL() || mValue.getSSL()){
                SocketAcceptor acceptor = new SocketAcceptor(mKey, mValue, executor);
                executor.execute(acceptor);
                continue;
            }

            socketChannel = ServerSocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.socket().bind(mKey);
            socketChannel.register(selector, SelectionKey.OP_ACCEPT, mKey);
        }
        loop(selector);
    }

    void loop (Selector selector) {
        Set readyKeys;
        SelectionKey key;
        ServerSocketChannel serverSocket;
        SocketChannel inSocket, outSocket;
        InetSocketAddress src, dest;

        while(true) {
            if(MyConstants.VERBOSE) {
                MyConstants.log("[Proxy] ready to accept connection");
            }

            try {
                selector.select();

                readyKeys = selector.selectedKeys();
                for (Iterator it = readyKeys.iterator(); it.hasNext();) {
                    key = (SelectionKey) it.next();
                    it.remove();

                    if(key.isAcceptable()) {
                        serverSocket = (ServerSocketChannel) key.channel();

                        src = (InetSocketAddress) key.attachment();
                        inSocket = serverSocket.accept();

                        if(MyConstants.VERBOSE) {
                            MyConstants.log("Proxy.loop()", "accepted connection from " + toString(inSocket));
                        }
                        dest=(InetSocketAddress) mappings.get(src);

                        if (dest == null) {
                            inSocket.close();
                            MyConstants.log("Proxy.loop()", "did not find a destination host for " + src);
                            continue;
                        } else {
                            if (MyConstants.VERBOSE) {
                                MyConstants.log("Proxy.loop()", "relaying traffic from " + MyConstants.toString(src) + " to " + MyConstants.toString(dest));
                            }
                        }

                        try {
                            outSocket = SocketChannel.open(dest);
                            handleConnection(inSocket, outSocket, executor);
                        } catch (Exception ex) {
                            inSocket.close();
                            throw ex;
                        }
                    }
                }
            } catch (Exception ex ){
                MyConstants.log("Proxy.loop()", "exception: " + ex);
            }
        }
    }

    void handleConnection (SocketChannel in, SocketChannel out, Executor executor) {
        try {
            _handleConnection(in, out, executor);
        } catch (Exception ex) {
            MyConstants.log("Proxy.handleConnection()", "exception: " + ex);
        }
    }

    void _handleConnection (final SocketChannel inChannel, final SocketChannel outChannel, Executor executor) throws Exception {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Selector selector = null;
                    SocketChannel tmpSocketChannel;
                    Set readyKeys;
                    SelectionKey key;
                    ByteBuffer transferBuf = ByteBuffer.allocate(MyConstants.BUFSIZE);

                    try {
                        selector = Selector.open();
                        inChannel.configureBlocking(false);
                        outChannel.configureBlocking(false);
                        inChannel.register(selector, SelectionKey.OP_READ);
                        outChannel.register(selector, SelectionKey.OP_READ);

                        while (selector.select() > 0) {
                            readyKeys = selector.selectedKeys();
                            for (Iterator it = readyKeys.iterator(); it.hasNext();) {
                                key = (SelectionKey) it.next();
                                it.remove();
                                tmpSocketChannel = (SocketChannel) key.channel();
                                if(tmpSocketChannel == null) {
                                    MyConstants.log(
                                            "Proxy._handleConnection()",
                                            "attachment is null, continuing");
                                    continue;
                                }
                                if (key.isReadable()) {
                                    if (tmpSocketChannel == inChannel) {
                                        if(relay(tmpSocketChannel, outChannel, transferBuf) == false) return;
                                    }
                                    if (tmpSocketChannel == outChannel) {
                                        if(relay(tmpSocketChannel, inChannel, transferBuf) == false) return;
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        close(selector, inChannel, outChannel);
                    }
                }
            });
    }

    void close(Selector selector, SocketChannel inChannel, SocketChannel outChannel) {
        try {
            if (selector != null) {
                selector.close();
            }
        } catch (Exception ex) {

        }

        try {
            if (inChannel != null) {
                inChannel.close();
            }
        } catch (Exception ex){

        }

        try {
            if(outChannel != null) {
                outChannel.close();
            }
        } catch (Exception ex){

        }
    }

    boolean relay(SocketChannel from, SocketChannel to, ByteBuffer buf) throws Exception {
        int num;
        StringBuilder stringBuilder;

        buf.clear();
        while (true) {
            num = from.read(buf);
            if(num < 0) {
                return false;
            } else {
                if (num == 0) {
                    return true;
                }
            }
            buf.flip();

            if(MyConstants.VERBOSE) {
                MyConstants.log(printRelayedData(toString(from), toString(to), buf.remaining()));
            }

            if(MyConstants.DEBUG) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(new String(buf.array()).trim());
                stringBuilder.append('\n');
                MyConstants.log(stringBuilder.toString());
            }
            to.write(buf);
            buf.flip();
        }
    }

    String toString (SocketChannel ch) {
        StringBuilder stringBuilder = new StringBuilder();
        Socket socket;

        if ( ch == null ) {
            return  null;
        }

        if ((socket = ch.socket()) == null) {
            return null;
        }

        stringBuilder.append(socket.getInetAddress().getHostName()).append(':').append(socket.getPort());

        return stringBuilder.toString();
    }

    String toString(InetSocketAddress addr) {
        StringBuilder sb;
        sb=new StringBuilder();

        if (addr == null)
            return null;
        sb.append(addr.getAddress().getHostName()).append(':').append(addr.getPort());
        if (addr instanceof M_InetSocketAddress)
            sb.append(" [ssl=").append(((M_InetSocketAddress) addr).getSSL()).append(']');
        return sb.toString();
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
