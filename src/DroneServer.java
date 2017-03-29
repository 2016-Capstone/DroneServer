import TCPRelayServer.*;
import relay.RelayServer;

/**
 * Created by NCS-KSW on 2017-03-02.
 */
public class DroneServer {

    //static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }    //Do not remove this line to use CV

    public static void main(String... args){
/*
        System.out.println("Welcome openCV : " + constants.MyConstants.CV_VERSION);

        Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));

        System.out.println("openCV Mat : " + m);

        Mat mr1 = m.row(1);
        mr1.setTo(new Scalar(1));
        Mat mc5 = m.col(5);
        mc5.setTo(new Scalar(5));

        System.out.println("openCV Mat data : \n" + m.dump());
        */
    //new Server(args).start();

        RelayServer relayServer = new RelayServer();
        try {
            relayServer.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}