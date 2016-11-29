package pl.lodz.p.dmcs.map;

/**
 * Created by emicgaj on 2016-11-05.
 */

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

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
    private CheckBox gps2 = null;

    private LocationManager locationManager;
    private LocationListener listener;
    private LocationListener listener2;
    private Location location2 = null;

    private TextView z = null;
    private CheckBox s1 = null;
    protected final Arbiter syncToken = new Arbiter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_location_list);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            token = extras.getString("token");
        }

        JSONObject data = new JSONObject();
        try {
            data.put("action", "getBuildings");
            data.put("token", token);
        } catch (Exception e) {
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

    @Override
    protected void onPause() {
        super.onPause();
        if (listener != null)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        locationManager.removeUpdates(listener);
        Log.d("PAUSEEEEEEEE","a");

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
                    GeoPoint startPoint = null;
                    Integer startRoom = null;
                    Integer startLevel = null;
                    if(listaBudynkow != null) {
                        if (gps2.isChecked()) {
                            final ProgressDialog barProgressDialog = new ProgressDialog(NavigateActivity.this);
                            barProgressDialog.setTitle("Proszę czekać");
                            barProgressDialog.setMessage("Ustalanie pozycji ...");
                            barProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            barProgressDialog.setIndeterminate(true);
                            barProgressDialog.setCancelable(false);
                            barProgressDialog.show();
                            final GeoPoint tmp = new GeoPoint(0.0, 0.0);
                            //final ProgressDialog ringProgressDialog = ProgressDialog.show(NavigateActivity.this, "Proszę czekać...", "Ustalanie lokalizacji", true);
                            //ringProgressDialog.setCancelable(true);
                            Thread mThread = new Thread() {
                                @Override
                                public void run() {
                                    syncToken.waitProducer();
                                    tmp.setLatitude(location2.getLongitude());
                                    tmp.setLongitude(location2.getLatitude());
                                    Log.i("Temp", tmp.toString());
                                    sendLocationData(tmp, null, null);
                                    syncToken.dataConsumed();
                                    barProgressDialog.dismiss();
                                }
                            };
                            mThread.start();
                        } else {
                            for (Budynki b : listaBudynkow) {
                                Log.d("LOOOOG", String.valueOf(b));
                                if (b.getNazwa_Obiektu().trim().equals(nav_start.getText().toString().trim())) {

                                    startPoint = new GeoPoint(b.getLong(), b.getLat());
                                    for (Floor f : b.getFloors()) {
                                        for (Room r : f.getRooms()) {
                                            if (new String(r.getRoomName() + ", pietro " + f.getLevel()).trim().equals(nav_room_start.getText().toString().trim())) {
                                                startLevel = f.getLevel();
                                                startRoom = r.getId();
                                            }
                                        }
                                    }
                                }
                            }
                            sendLocationData(startPoint, startLevel, startRoom);
                        }
                    }
                }
            });
        }

        s1 = (CheckBox) findViewById(R.id.sala_z);
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




        gps2 = (CheckBox) findViewById(R.id.search_from_my_location);
        gps2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                z = (TextView) findViewById(R.id.search_label_source);
                if (((CheckBox) v).isChecked()) {
                    z.setEnabled(false);
                    nav_start.setEnabled(false);
                    nav_start.setHint("");
                    s1.setEnabled(false);
                    if(nav_room_start != null){
                        nav_room_start.setEnabled(false);
                        nav_room_start.setHint("");
                    }
                    Boolean isGPSEnabled = locationManager
                            .isProviderEnabled(LocationManager.GPS_PROVIDER);
                    Log.d("GPSENABLED", isGPSEnabled+"");

                    listener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d("SIEC_CHANGE", "ONLOCATIONCHANGE");
                            location2 = new Location(location);
                            syncToken.dataLoaded();
                        }

                        @Override
                        public void onStatusChanged(String s, int i, Bundle bundle) {

                        }

                        @Override
                        public void onProviderEnabled(String s) {

                        }

                        @Override
                        public void onProviderDisabled(String s) {

                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    };
                    listener2 = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d("GPS_CHANGE", "ONLOCATIONCHANGE");
                            location2 = new Location(location);
                            syncToken.dataLoaded();
                        }

                        @Override
                        public void onStatusChanged(String s, int i, Bundle bundle) {

                        }

                        @Override
                        public void onProviderEnabled(String s) {

                        }

                        @Override
                        public void onProviderDisabled(String s) {

                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    };
                    configure();
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
    void configure(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        ,10);
            }
            gps2.setChecked(false);
            z.setEnabled(true);
            nav_start.setEnabled(true);
            nav_start.setHint("Początek drogi");
            s1.setEnabled(true);
            if (nav_room_start != null) {
                if(s1.isChecked()) nav_room_start.setEnabled(true);
                if(s1.isChecked()) nav_room_start.setHint("Sala");
            }
            return;
        }
        else{
            if (listener != null || listener2 != null) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener2);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, listener);
            }
        }


    }

    void sendLocationData(GeoPoint startPoint, Integer startRoom, Integer startLevel) {
        Intent returnIntent = new Intent();
        GeoPoint endPoint = null;
        Integer endRoom = null;
        Integer endLevel = null;
        if (listaBudynkow != null)
        {
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
    private class Arbiter {
        private boolean dataLoaded = false;
        public synchronized void waitProducer(){
            while(!dataLoaded){
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        public synchronized void waitConsumer(){
            while(dataLoaded){
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        public synchronized void dataLoaded(){
            dataLoaded = true;
            notify();
        }public synchronized void dataConsumed(){
            dataLoaded = false;
            notify();
        }}
}
