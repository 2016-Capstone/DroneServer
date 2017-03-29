package relay.service;

import constants.MyConstants;
import relay.core.GPS;

/**
 * Created by NCS-KSW on 2017-03-16.
 */
public class MsgAnalyzer {

    public final static String MSG_DELI = "%%";
    public final static String VALUE_DELI = "=";

    public static int GET_VALUE_DVTYPE(String msg){
        String[] input = msg.split(MSG_DELI);
        String[] values;

        for (String value: input) {
            values = value.split(VALUE_DELI);
            if(values[0].equals(MyConstants.PROTO_DVTYPE_KEY)){
                return Integer.parseInt(values[1]);
            }
        }
        return -1;
    }

    public static int GET_VALUE_MSGTYPE(String msg){
        String[] input = msg.split(MSG_DELI);
        String[] values;

        for (String value: input) {
            values = value.split(VALUE_DELI);
            if(values[0].equals(MyConstants.PROTO_MSGTYPE_KEY)){
                return Integer.parseInt(values[1]);
            }
        }
        return -1;
    }

    public static String GET_VALUE_DATA(String msg){
        String[] input = msg.split(MSG_DELI);
        String[] values;

        for (String value : input) {
            values = value.split(VALUE_DELI);
            if(values[0].equals(MyConstants.PROTO_DATA_KEY)) {
                return values[1];
            }
        }
        return null;
    }

    public static boolean IS_YOU(String msg){
        String[] input = msg.split(MSG_DELI);
        String[] values;

        for (String value: input) {
            values = value.split(VALUE_DELI);
            if(values[0].equals(MyConstants.PROTO_MSGTYPE_KEY)
                    && values[1].equals(MyConstants.PROTO_MSGTYPE.HELLO.ordinal()+"")){
                return true;
            }
        }
        return false;
    }

    public static boolean CMP_VALUE_DVTYPE(String msg, int val){
        String[] input = msg.split(MSG_DELI);
        String[] values;

        for (String value: input) {
            values = value.split(VALUE_DELI);
            if(values[0].equals(MyConstants.PROTO_DVTYPE_KEY)
                    && values[1].equals(val+"")){
                return true;
            }
        }
        return false;
    }

    public static boolean CMP_VALUE_MSGTYPE(String msg, int val){
        String[] input = msg.split(MSG_DELI);
        String[] values;

        for (String value: input) {
            values = value.split(VALUE_DELI);
            if(values[0].equals(MyConstants.PROTO_MSGTYPE_KEY)
                    && values[1].equals(val+"")){
                return true;
            }
        }
        return false;
    }

    public static GPS TO_GPS(String msg){
        String[] values = null;
        GPS gps = null;

        try{
            values = msg.split("/");
            gps = new GPS(values[0], values[1]);
        } catch (Exception ex){
            MyConstants.log("TO_GPS", ex.getMessage());
        }

        return gps;
    }
}
