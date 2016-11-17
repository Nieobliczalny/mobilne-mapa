package pl.lodz.p.dmcs.map;

import java.util.ArrayList;

/**
 * Created by emicgaj on 2016-11-17.
 */

public class Room {

    private String roomName;
    private int type;
    private int floor;
    private ArrayList<Double> polygons = null;
    private int id;

    public Room(String roomName, int type, int floor, ArrayList<Double> polygons, int id) {
        this.roomName = roomName;
        this.type = type;
        this.floor = floor;
        this.polygons = polygons;
        this.id = id;
    }

    public ArrayList<Double> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<Double> polygons) {
        this.polygons = polygons;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomName='" + roomName + '\'' +
                ", type=" + type +
                ", floor=" + floor +
                ", polygons=" + polygons +
                ", id=" + id +
                '}';
    }
}
