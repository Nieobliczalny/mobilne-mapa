package pl.lodz.p.dmcs.map;

/**
 * Created by emicgaj on 2016-11-05.
 */

public class Budynki {



    private String Nazwa_Obiektu;
    private double Long;
    private double Lat;

    public Budynki(String nazwa_Obiektu, double lat, double aLong) {
        Long = aLong;
        Nazwa_Obiektu = nazwa_Obiektu;
        Lat = lat;
    }

    public String getNazwa_Obiektu() {
        return Nazwa_Obiektu;
    }

    public void setNazwa_Obiektu(String nazwa_Obiektu) {
        Nazwa_Obiektu = nazwa_Obiektu;
    }

    public double getLong() {
        return Long;
    }

    public void setLong(double aLong) {
        Long = aLong;
    }



    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }




}
