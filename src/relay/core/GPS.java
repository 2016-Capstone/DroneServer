package relay.core;

/**
 * Created by NCS-KSW on 2017-03-21.
 */
public class GPS {
    public double lat, lng;

    public GPS (double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }

    public GPS (String lat, String lng){
        double parsedLat, parsedLng;

        try {
            parsedLat = Double.parseDouble(lat);
            parsedLng = Double.parseDouble(lng);
            this.lat = parsedLat;
            this.lng = parsedLng;
        } catch (Exception ex){
            throw ex;
        }
    }
}
