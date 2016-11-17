package pl.lodz.p.dmcs.map;

/**
 * Created by emicgaj on 2016-11-05.
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;


public class NavigateActivity extends AppCompatActivity {

    private ArrayAdapter<String> hintsBuilding = null;
    private ArrayAdapter<String> hintsRooms = null;
    protected String token = "";
    private List<Budynki> listaBudynkow = null;
    private JSONArray buildings = null;
    AutoCompleteTextView nav_start = null;
    AutoCompleteTextView nav_room_start = null;
    AutoCompleteTextView nav_end = null;
    AutoCompleteTextView nav_room_end = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_location_list);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            token = extras.getString("token");
        }

        JSONObject data = new JSONObject();
        try {
            data.put("action", "getBuildings");
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task = new SendPostTask();
        task.setActivity(NavigateActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    buildings = obj.getJSONArray("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setUpListeners();
            }
        });
        task.execute(data);

    }




    private void setUpListeners(){

        if (hintsBuilding == null) {
            listaBudynkow = new ArrayList<Budynki>();
            hintsBuilding = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
            hintsRooms = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
            Log.d("DOOOOOOOG",buildings.toString());
            if (buildings == null) return;
            for (int i = 0; i < buildings.length(); i++) {
                try {
                    JSONObject building = buildings.getJSONObject(i);
                    Double lat = building.getDouble("latitude");
                    Double lng = building.getDouble("longitude");
                    String name = building.getString("name");
                    ArrayList<Floor> listaPieter = new ArrayList<>();
                    JSONArray floors = building.getJSONArray("floors");
                    for (int j = 0; j < floors.length(); j++)                    {
                        JSONObject floor = floors.getJSONObject(j);
                        Integer numerPietra = floor.getInt("level");
                        Integer idBudynku = floor.getInt("building");
                        ArrayList<Room> listaPomieszczen = new ArrayList<>();
                        JSONArray rooms = floor.getJSONArray("rooms");
                        for (int k = 0; k < rooms.length(); k++)                        {
                            JSONObject room = rooms.getJSONObject(k);
                            String roomName = room.getString("name");
                            Integer typeRoom = room.getInt("type");
                            Integer floorLevel = room.getInt("floor");
                            Integer idRoom = room.getInt("id");

                            Room temp3 = new Room(roomName,typeRoom,floorLevel,new ArrayList<Double>(),idRoom);
                            listaPomieszczen.add(temp3);
                        }
                        Floor temp2 = new Floor(listaPomieszczen,idBudynku,numerPietra,new ArrayList<Double>());
                        listaPieter.add(temp2);
                    }
                    Budynki temp = new Budynki(name, lng, lat, listaPieter);
                    Log.d("DOOOOOOOG TEMP",temp.toString());
                    listaBudynkow.add(temp);
                    hintsBuilding.add(temp.getNazwa_Obiektu());
                    for(Floor f : listaPieter){
                        for(Room r : f.getRooms()){
                            hintsRooms.add(r.getRoomName()+", pietro "+f.getLevel());
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            nav_start = (AutoCompleteTextView) findViewById(R.id.search_source);
            nav_start.setAdapter(hintsBuilding);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            nav_end = (AutoCompleteTextView) findViewById(R.id.search_destination);
            nav_end.setAdapter(hintsBuilding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            nav_room_start = (AutoCompleteTextView) findViewById(R.id.search_sala_z);
            nav_room_start.setEnabled(false);
            nav_room_start.setAdapter(hintsRooms);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            nav_room_end = (AutoCompleteTextView) findViewById(R.id.search_sala_do);
            nav_room_end.setEnabled(false);
            nav_room_end.setAdapter(hintsRooms);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button showBtn = (Button) findViewById(R.id.search_show_route);
        if (showBtn != null)
        {
            showBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent returnIntent = new Intent();
                    GeoPoint startPoint = null;
                    GeoPoint endPoint = null;
                    Integer startRoom = null;
                    Integer endRoom = null;
                    Integer startLevel = null;
                    Integer endLevel = null;
                    if(listaBudynkow != null) {
                        for (Budynki b : listaBudynkow) {
                            Log.d("LOOOOG",String.valueOf(b));
                            if (b.getNazwa_Obiektu().trim().equals(nav_start.getText().toString().trim())) {

                                startPoint = new GeoPoint(b.getLong(), b.getLat());
                                for(Floor f : b.getFloors()){
                                    for (Room r : f.getRooms()){
                                        if(new String(r.getRoomName()+", pietro "+f.getLevel()).trim().equals(nav_room_start.getText().toString().trim())){
                                            startLevel = f.getLevel();
                                            startRoom = r.getId();
                                        }
                                    }
                                }
                            }
                        }
                        for (Budynki b : listaBudynkow) {
                            if (b.getNazwa_Obiektu().trim().equals(nav_end.getText().toString().trim())) {
                                endPoint = new GeoPoint(b.getLong(), b.getLat());
                                for(Floor f : b.getFloors()){
                                    for (Room r : f.getRooms()){
                                        if(new String(r.getRoomName()+", pietro "+f.getLevel()).trim().equals(nav_room_end.getText().toString().trim())){
                                            endLevel = f.getLevel();
                                            endRoom = r.getId();
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if(startPoint != null) {
                        returnIntent.putExtra("startLat", startPoint.getLatitude());
                        returnIntent.putExtra("startLng", startPoint.getLongitude());
                    }
                    if(endPoint != null) {
                        returnIntent.putExtra("endLat", endPoint.getLatitude());
                        returnIntent.putExtra("endLng", endPoint.getLongitude());
                    }
                    if (startRoom != null)
                    {
                        returnIntent.putExtra("startRoom",startRoom);
                        returnIntent.putExtra("startLevel",startLevel);
                    }
                    if (endRoom != null)
                    {
                        returnIntent.putExtra("endRoom",endRoom);
                        returnIntent.putExtra("endLevel",endLevel);
                    }
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }
            });
        }

        final CheckBox s1 = (CheckBox) findViewById(R.id.sala_z);
        s1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    if(nav_room_start != null){
                        nav_room_start.setEnabled(true);
                        nav_room_start.setHint("Sala");
                    }
                }
                else {
                    if (nav_room_start != null) {
                        nav_room_start.setEnabled(false);
                        nav_room_start.setHint("");
                    }
                }

            }
        });

        CheckBox s2 = (CheckBox) findViewById(R.id.sala_do);
        s2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    if(nav_room_end != null){
                        nav_room_end.setEnabled(true);
                        nav_room_end.setHint("Sala");
                    }
                }
                else {
                    if (nav_room_end != null) {
                        nav_room_end.setEnabled(false);
                        nav_room_end.setHint("");
                    }
                }

            }
        });




        CheckBox gps2 = (CheckBox) findViewById(R.id.search_from_my_location);
        gps2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TextView z = (TextView) findViewById(R.id.search_label_source);
                if (((CheckBox) v).isChecked()) {

                    z.setEnabled(false);
                    nav_start.setEnabled(false);
                    nav_start.setHint("");
                    s1.setEnabled(false);
                    if(nav_room_start != null){
                        nav_room_start.setEnabled(false);
                        nav_room_start.setHint("");

                    }
                }
                else {
                    z.setEnabled(true);
                    nav_start.setEnabled(true);
                    nav_start.setHint("Początek drogi");
                    s1.setEnabled(true);
                    if (nav_room_start != null) {
                        if(s1.isChecked()) nav_room_start.setEnabled(true);
                        if(s1.isChecked()) nav_room_start.setHint("Sala");
                    }
                }

            }
        });
    }
}
