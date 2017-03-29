package relay.service;

import constants.MyConstants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Created by NCS-KSW on 2017-03-21.
 */
public class PhotoParser {

    public static byte[] TO_BYTE_ARR(String payload){
        byte[] byteArr = null;

        try {
            byteArr = payload.getBytes();
        } catch (Exception ex){
            MyConstants.log("TO_BYTE_ARR", ex.getMessage());
        }

        return byteArr;
    }

    public static Object TO_IMG(byte[] data){
        BufferedImage img = null;

        try {
            img = ImageIO.read(new ByteArrayInputStream(data));
        } catch (Exception ex){
            MyConstants.log("TO_IMG", ex.getMessage());
        }

        return img;
    }
}
