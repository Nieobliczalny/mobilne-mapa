package pl.lodz.p.dmcs.map;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by emicgaj on 2016-11-05.
 */

public class Budynki {



    private String Nazwa_Obiektu;
    private double Long;
    private double Lat;
    private ArrayList<Floor> floors;
    private String unofficial_name;
    private String number;
    private JSONArray units;

    public Budynki(String nazwa_Obiektu, double aLong, double lat, ArrayList<Floor> floors, String unofficial_name, String number, JSONArray units) {
        Nazwa_Obiektu = nazwa_Obiektu;
        Long = aLong;
        Lat = lat;
        this.floors = floors;
        this.unofficial_name = unofficial_name;
        this.number = number;
        this.units = units;
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

    public ArrayList<Floor> getFloors() { return floors; }

    public void setFloors(ArrayList<Floor> floors) { this.floors = floors; }

    public String getUnofficial_name() {
        return unofficial_name;
    }

    public void setUnofficial_name(String unofficial_name) {
        this.unofficial_name = unofficial_name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public JSONArray getUnits() {
        return units;
    }

    public void setUnits(JSONArray units) {
        this.units = units;
    }

    @Override
    public String toString() {
        return "Budynki{" +
                "Nazwa_Obiektu='" + Nazwa_Obiektu + '\'' +
                ", Long=" + Long +
                ", Lat=" + Lat +
                ", floors=" + floors +
                ", unofficial_name=" + unofficial_name +
                ", number=" + number +
                '}';
    }
}
