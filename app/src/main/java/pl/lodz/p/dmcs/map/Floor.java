package pl.lodz.p.dmcs.map;

import java.util.ArrayList;

/**
 * Created by emicgaj on 2016-11-17.
 */

public class Floor {
    private ArrayList<Room> rooms;
    int building = 0;
    int level = 0;
    private ArrayList<Double> polygons = null;

    public Floor(ArrayList<Room> room, int building, int level, ArrayList<Double> polygons) {
        this.rooms = room;
        this.building = building;
        this.level = level;
        this.polygons = polygons;
    }

    public ArrayList<Double> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<Double> polygons) {
        this.polygons = polygons;
    }

    public int getBuilding() {
        return building;
    }

    public void setBuilding(int building) {
        this.building = building;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public void setRooms(ArrayList<Room> room) {
        this.rooms = room;
    }


    @Override
    public String toString() {
        return "Floor{" +
                "room=" + rooms +
                ", building=" + building +
                ", level=" + level +
                ", polygons=" + polygons +
                '}';
    }
}
