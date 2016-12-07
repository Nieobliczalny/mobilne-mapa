package pl.lodz.p.dmcs.map;

/**
 * Created by emicgaj on 2016-11-05.
 */

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import java.util.Locale;


public class NavigateActivity extends AppCompatActivity {

    private ArrayAdapter<String> hintsBuilding = null;
    private ArrayAdapter<String> hintsRoomsStart = null;
    private ArrayAdapter<String> hintsRoomsEnd = null;
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
    private CheckBox s2 = null;
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
        if (listener != null) {
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
            locationManager.removeUpdates(listener2);
        }
        Log.d("PAUSEEEEEEEE","a");

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("RESUMEEEEEE","a");


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 20) {
            Log.d("ONACTIVITYRESULT","a");

            boolean gps_enabled = false;
            boolean network_enabled = false;

            try {
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ex) {}

            try {
                network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {}

            if(!gps_enabled && !network_enabled) {
                if(gps2 != null) gps2.setChecked(false);
                if(z != null) z.setEnabled(true);
                if(nav_start != null) {
                    nav_start.setEnabled(true);
                    nav_start.setHint("Początek drogi");
                }
                if(s1 != null) {
                    s1.setEnabled(true);
                    if (nav_room_start != null) {
                        if (s1.isChecked()) nav_room_start.setEnabled(true);
                        if (s1.isChecked()) nav_room_start.setHint("Sala");
                    }
                }
            }
        }

    }

    private void setUpListeners(){

        if (hintsBuilding == null) {
            listaBudynkow = new ArrayList<Budynki>();
            hintsBuilding = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
            hintsRoomsStart = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
            hintsRoomsEnd = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
            Log.d("DOOOOOOOG",buildings.toString());
            if (buildings == null) return;
            for (int i = 0; i < buildings.length(); i++) {
                try {
                    JSONObject building = buildings.getJSONObject(i);
                    Double lat = building.getDouble("latitude");
                    Double lng = building.getDouble("longitude");
                    String name = building.getString("name");
                    String unofficial_name = building.getString("unofficial_name");
                    String number = building.getString("number");
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
                    Budynki temp = new Budynki(name, lng, lat, listaPieter, unofficial_name, number);
                    Log.d("DOOOOOOOG TEMP",temp.toString());
                    listaBudynkow.add(temp);
                    String unofficial_name2 = building.getString("unofficial_name").replace(" ; ",", ");
                    String number2 = building.getString("number");
                    String temp2 = building.getString("name");
                    if(!unofficial_name2.equals("") || !number2.equals("")) {
                        temp2+=" ( ";
                        if (!unofficial_name2.equals("")) {
                            temp2 += unofficial_name2;
                        }
                        if (!number2.equals("")) {
                            if (!unofficial_name2.equals("")) temp2 +=", ";
                            temp2 += number;
                        }
                        temp2+=" )";
                    }
                    hintsBuilding.add(temp2);
//                    for(Floor f : listaPieter){
//                        for(Room r : f.getRooms()){
//                            hintsRooms.add(r.getRoomName()+", pietro "+f.getLevel());
//                        }
//                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            nav_start = (AutoCompleteTextView) findViewById(R.id.search_source);
            nav_start.setAdapter(hintsBuilding);
            nav_start.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(s1 != null) s1.setChecked(false);
                    if (nav_room_start != null) {
                        nav_room_start.setEnabled(false);
                        nav_room_start.setHint("");
                        nav_room_start.setText("");
                    }

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            nav_end = (AutoCompleteTextView) findViewById(R.id.search_destination);
            nav_end.setAdapter(hintsBuilding);
            nav_end.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(s2 != null) s2.setChecked(false);
                    if (nav_room_end != null) {
                        nav_room_end.setEnabled(false);
                        nav_room_end.setHint("");
                        nav_room_end.setText("");
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            nav_room_start = (AutoCompleteTextView) findViewById(R.id.search_sala_z);
            nav_room_start.setEnabled(false);


        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            nav_room_end = (AutoCompleteTextView) findViewById(R.id.search_sala_do);
            nav_room_end.setEnabled(false);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Button showBtn = (Button) findViewById(R.id.search_show_route);
        if (showBtn != null)
        {

            showBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                            //Filtrowanie po wpisanej nazwie
                            String searchValue = nav_start.getText().toString().trim().toLowerCase().replace(';', ' ');
                            final ArrayList<Budynki> found = new ArrayList<Budynki>();
                            for (Budynki b : listaBudynkow)
                            {
                                if (isBuildingMatch(b, searchValue)) found.add(b);
                            }
                            if (found.size() > 1)
                            {
                                ArrayList<String> items = new ArrayList<String>();
                                for (Budynki b : found)
                                {
                                    items.add(b.getNazwa_Obiektu()+" ; "+b.getUnofficial_name()+" ; "+b.getNumber());
                                }
                                String[] opts = new String[items.size()];
                                for (int i = 0; i < opts.length; i++) {
                                    opts[i] = items.get(i);
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(NavigateActivity.this);
                                builder.setTitle("Punkt początkowy - Znaleziono kilka trafień, wybierz właściwe")
                                        .setItems(opts, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // The 'which' argument contains the index position
                                                // of the selected item
                                                saveStartNavigationData(found.get(which));
                                            }
                                        });
                                AlertDialog d = builder.create();
                                d.show();
                            }
                            else if (found.size() == 0)
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        Toast t = Toast.makeText(NavigateActivity.this, "Punkt początkowy - Nie znaleziono takiego obiektu! Sprawdź pisownię i spróbuj ponownie.", Toast.LENGTH_SHORT);
                                        t.show();
                                        return;
                                    }
                                });
                            }
                            else {
                                saveStartNavigationData(found.get(0));
                            }
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
                    hintsRoomsStart.clear();
                    if(nav_room_start != null){
                        nav_room_start.setEnabled(true);
                        nav_room_start.setHint("Sala");


                        if (listaBudynkow != null)
                        {
                            //Filtrowanie po wpisanej nazwie
                            String searchValue = nav_start.getText().toString().trim().toLowerCase().replace(';', ' ');
                            final ArrayList<Budynki> found = new ArrayList<Budynki>();
                            for (Budynki b : listaBudynkow) {
                                if (isBuildingMatch(b, searchValue)) found.add(b);
                            }
                            if (found.size() > 1) {
                                Toast t = Toast.makeText(NavigateActivity.this, "Znaleziono kilka budynków pasujących do wzorca. Sprecyzuj", Toast.LENGTH_SHORT);
                                s1.setChecked(false);
                                if (nav_room_start != null) {
                                    nav_room_start.setEnabled(false);
                                    nav_room_start.setHint("");
                                }
                                t.show();

                                return;
                            } else if (found.size() == 0) {
                                Toast t = Toast.makeText(NavigateActivity.this, "Nie znaleziono budynku bądź sali do budynku.", Toast.LENGTH_SHORT);
                                s1.setChecked(false);
                                if (nav_room_start != null) {
                                    nav_room_start.setEnabled(false);
                                    nav_room_start.setHint("");
                                }
                                t.show();
                                return;
                            } else {
                                for(Floor f : found.get(0).getFloors()){
                                    for(Room r : f.getRooms()){
                                        hintsRoomsStart.add(r.getRoomName()+", pietro "+f.getLevel());
                                    }
                                }

                                nav_room_start.setAdapter(hintsRoomsStart);
                            }
                        } else {
                            Toast t = Toast.makeText(NavigateActivity.this, "Sale - Błąd danych, spróbuj uruchomić ponownie aplikację.", Toast.LENGTH_SHORT);
                            t.show();
                        }
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

        s2 = (CheckBox) findViewById(R.id.sala_do);
        s2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    hintsRoomsEnd.clear();
                    if(nav_room_end != null){
                        nav_room_end.setEnabled(true);
                        nav_room_end.setHint("Sala");

                        if (listaBudynkow != null)
                        {
                            //Filtrowanie po wpisanej nazwie
                            String searchValue = nav_end.getText().toString().trim().toLowerCase().replace(';', ' ');
                            final ArrayList<Budynki> found = new ArrayList<Budynki>();
                            for (Budynki b : listaBudynkow) {
                                if (isBuildingMatch(b, searchValue)) found.add(b);
                            }
                            if (found.size() > 1) {
                                Toast t = Toast.makeText(NavigateActivity.this, "Znaleziono kilka budynków pasujących do wzorca. Sprecyzuj", Toast.LENGTH_SHORT);
                                s2.setChecked(false);
                                if (nav_room_end != null) {
                                    nav_room_end.setEnabled(false);
                                    nav_room_end.setHint("");
                                }
                                t.show();

                                return;
                            } else if (found.size() == 0) {
                                Toast t = Toast.makeText(NavigateActivity.this, "Nie znaleziono budynku bądź sali do budynku.", Toast.LENGTH_SHORT);
                                s2.setChecked(false);
                                if (nav_room_end != null) {
                                    nav_room_end.setEnabled(false);
                                    nav_room_end.setHint("");
                                }
                                t.show();
                                return;
                            } else {
                                for(Floor f : found.get(0).getFloors()){
                                    for(Room r : f.getRooms()){
                                        hintsRoomsEnd.add(r.getRoomName()+", pietro "+f.getLevel());
                                    }
                                }

                                nav_room_end.setAdapter(hintsRoomsEnd);
                            }
                        } else {
                            Toast t = Toast.makeText(NavigateActivity.this, "Sale - Błąd danych, spróbuj uruchomić ponownie aplikację.", Toast.LENGTH_SHORT);
                            t.show();
                        }

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
        gps2.setClickable(true);
        gps2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                z = (TextView) findViewById(R.id.search_label_source);
                if (((CheckBox) v).isChecked()) {
                    z.setEnabled(false);
                    nav_start.setEnabled(false);
                    nav_start.setText("");
                    nav_start.setHint("");
                    s1.setChecked(false);
                    s1.setEnabled(false);
                    if(nav_room_start != null){
                        nav_room_start.setEnabled(false);
                        nav_room_start.setHint("");
                        nav_room_start.setText("");
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
                            startActivityForResult(i,20);
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

                            //Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                           // startActivity(i);
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
    void saveStartNavigationData(Budynki b){
        final GeoPoint startPoint = new GeoPoint(b.getLong(), b.getLat());
        if (s1.isChecked()) {
            Integer startRoom = null;
            Integer startLevel = null;
            final ArrayList<RoomFloorStruct> data = new ArrayList<>();
            String searchValue = nav_room_start.getText().toString().trim().toLowerCase().replace(';', ' ');
            for (Floor f : b.getFloors()) {
                for (Room r : f.getRooms()) {
                    if (isRoomMatch(r, searchValue)) data.add(new RoomFloorStruct(f, r));
                }
            }
            if (data.size() > 1)
            {
                ArrayList<String> items = new ArrayList<String>();
                for (RoomFloorStruct d : data)
                {
                    items.add(d.getRoom().getRoomName() + ", piętro " + d.getFloor().getLevel());
                }
                final String[] opts = new String[items.size()];
                for (int i = 0; i < opts.length; i++) {
                    opts[i] = items.get(i);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        AlertDialog.Builder builder = new AlertDialog.Builder(NavigateActivity.this);
                        builder.setTitle("Punkt początkowy (sala) - Znaleziono kilka trafień, wybierz właściwe")
                                .setItems(opts, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // The 'which' argument contains the index position
                                        // of the selected item
                                        sendLocationData(startPoint, data.get(which).getRoom().getId(), data.get(which).getFloor().getLevel());
                                    }
                                });
                        AlertDialog d = builder.create();
                        d.show();
                    }
                });
            }
            else if (data.size() == 0)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast t = Toast.makeText(NavigateActivity.this, "Punkt początkowy - Nie znaleziono takiej sali! Sprawdź pisownię i spróbuj ponownie.", Toast.LENGTH_SHORT);
                        t.show();
                    }
                });
                return;
            }
            else {
                startLevel = data.get(0).getFloor().getLevel();
                startRoom = data.get(0).getRoom().getId();
                sendLocationData(startPoint, startRoom, startLevel);
            }
        } else {
            sendLocationData(startPoint, null, null);
        }
    }
    void saveEndNavigationData(final GeoPoint startPoint, final Integer startRoom, final Integer startLevel, Budynki b){
        final GeoPoint endPoint = new GeoPoint(b.getLong(), b.getLat());
        CheckBox roomEndChk = (CheckBox) findViewById(R.id.sala_do);
        if (roomEndChk.isChecked()) {
            Integer endRoom = null;
            Integer endLevel = null;
            final ArrayList<RoomFloorStruct> data = new ArrayList<>();
            String searchValue = nav_room_end.getText().toString().trim().toLowerCase().replace(';', ' ');
            for (Floor f : b.getFloors()) {
                for (Room r : f.getRooms()) {
                    if (isRoomMatch(r, searchValue)) data.add(new RoomFloorStruct(f, r));
                }
            }
            if (data.size() > 1)
            {
                ArrayList<String> items = new ArrayList<String>();
                for (RoomFloorStruct d : data)
                {
                    items.add(d.getRoom().getRoomName() + ", piętro " + d.getFloor().getLevel());
                }
                final String[] opts = new String[items.size()];
                for (int i = 0; i < opts.length; i++) {
                    opts[i] = items.get(i);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        AlertDialog.Builder builder = new AlertDialog.Builder(NavigateActivity.this);
                        builder.setTitle("Punkt końcowy (sala) - Znaleziono kilka trafień, wybierz właściwe")
                                .setItems(opts, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // The 'which' argument contains the index position
                                        // of the selected item
                                        finishLocationData(startPoint, startRoom, startLevel, endPoint, data.get(which).getRoom().getId(), data.get(which).getFloor().getLevel());
                                    }
                                });
                        AlertDialog d = builder.create();
                        d.show();
                    }
                });
            }
            else if (data.size() == 0)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast t = Toast.makeText(NavigateActivity.this, "Punkt końcowy - Nie znaleziono takiej sali! Sprawdź pisownię i spróbuj ponownie.", Toast.LENGTH_SHORT);
                        t.show();
                        return;
                    }
                });
            }
            else {
                endLevel = data.get(0).getFloor().getLevel();
                endRoom = data.get(0).getRoom().getId();
                finishLocationData(startPoint, startRoom, startLevel, endPoint, endRoom, endLevel);
            }
        } else {
            finishLocationData(startPoint, startRoom, startLevel, endPoint, null, null);
        }
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

    void sendLocationData(final GeoPoint startPoint, final Integer startRoom, final Integer startLevel) {
        if (listaBudynkow != null)
        {
            //Filtrowanie po wpisanej nazwie
            String searchValue = nav_end.getText().toString().trim().toLowerCase().replace(';', ' ');
            final ArrayList<Budynki> found = new ArrayList<Budynki>();
            for (Budynki b : listaBudynkow) {
                if (isBuildingMatch(b, searchValue)) found.add(b);
            }
            if (found.size() > 1) {
                ArrayList<String> items = new ArrayList<String>();
                for (Budynki b : found)
                {
                    items.add(b.getNazwa_Obiektu());
                }
                final String[] opts = new String[items.size()];
                for (int i = 0; i < opts.length; i++) {
                    opts[i] = items.get(i);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        AlertDialog.Builder builder = new AlertDialog.Builder(NavigateActivity.this);
                        builder.setTitle("Punkt końcowy - Znaleziono kilka trafień, wybierz właściwe")
                                .setItems(opts, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // The 'which' argument contains the index position
                                        // of the selected item
                                        saveEndNavigationData(startPoint, startRoom, startLevel, found.get(which));
                                    }
                                });
                        AlertDialog d = builder.create();
                        d.show();
                    }
                });
            } else if (found.size() == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast t = Toast.makeText(NavigateActivity.this, "Punkt końcowy - Nie znaleziono takiego obiektu! Sprawdź pisownię i spróbuj ponownie.", Toast.LENGTH_SHORT);
                        t.show();
                    }
                });
                return;
            } else {
                saveEndNavigationData(startPoint, startRoom, startLevel, found.get(0));
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast t = Toast.makeText(NavigateActivity.this, "Punkt końcowy - Błąd danych, spróbuj uruchomić ponownie aplikację.", Toast.LENGTH_SHORT);
                    t.show();
                }
            });
        }
    }
    void finishLocationData(final GeoPoint startPoint, final Integer startRoom, final Integer startLevel, GeoPoint endPoint, Integer endRoom, Integer endLevel) {
        Intent returnIntent = new Intent();

        if ((startPoint == endPoint) || (startPoint != null && endPoint != null && startPoint.getLongitude() == endPoint.getLongitude() && startPoint.getLatitude() == endPoint.getLatitude() &&
                ((startRoom == endRoom) || (startRoom != null && startLevel != null && startRoom.equals(endRoom) && startLevel.equals(endLevel))))) {

            runOnUiThread(new Runnable() {
                  @Override
                  public void run() {

                      Toast t = Toast.makeText(NavigateActivity.this, "Punkt końcowy jest równy punktowi początkowemu. Nawigacja nie została uruchomiona.", Toast.LENGTH_SHORT);
                      t.show();
                  }
            });
            setResult(Activity.RESULT_CANCELED);
        } else {
            if (startPoint != null) {
                returnIntent.putExtra("startLat", startPoint.getLatitude());
                returnIntent.putExtra("startLng", startPoint.getLongitude());
            }
            if (endPoint != null) {
                returnIntent.putExtra("endLat", endPoint.getLatitude());
                returnIntent.putExtra("endLng", endPoint.getLongitude());
            }
            if (startRoom != null) {
                returnIntent.putExtra("startRoom", startRoom);
                returnIntent.putExtra("startLevel", startLevel);
            }
            if (endRoom != null) {
                returnIntent.putExtra("endRoom", endRoom);
                returnIntent.putExtra("endLevel", endLevel);
            }
            setResult(Activity.RESULT_OK, returnIntent);
        }
        finish();
    }
    public boolean isBuildingMatch(Budynki b, String searchValue)
    {
        String buildingName = b.getNazwa_Obiektu().trim().toLowerCase();
        String buildingUName = b.getUnofficial_name().trim().toLowerCase();
        String buildingNumber = b.getNumber().trim().toLowerCase();
        if (buildingName.contains(searchValue)) return true;
        else if (buildingNumber.contains(searchValue)) return true;
        else if (buildingUName.contains(searchValue)) return true;
        else if (buildingName.length() > 0 && searchValue.contains(buildingName)) return true;
        else if (buildingNumber.length() > 0 && searchValue.contains(buildingNumber)) return true;
        else if (buildingUName.length() > 0 && searchValue.contains(buildingUName)) return true;
        String[] uNames = buildingUName.split(" ; ");
        for (String uName : uNames)
        {
            if (uName.length() > 0 && searchValue.contains(uName)) return true;
        }
        return false;
    }
    public boolean isRoomMatch(Room r, String searchValue)
    {
        String roomName = r.getRoomName().trim().toLowerCase();
        if (roomName.length() > 0 && searchValue.contains(roomName)) return true;
        else if (roomName.contains(searchValue)) return true;
        return false;
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
    private class RoomFloorStruct
    {
        private Floor floor;
        private Room room;

        public RoomFloorStruct(Floor floor, Room room) {
            this.floor = floor;
            this.room = room;
        }

        public Floor getFloor() {
            return floor;
        }

        public void setFloor(Floor floor) {
            this.floor = floor;
        }

        public Room getRoom() {
            return room;
        }

        public void setRoom(Room room) {
            this.room = room;
        }
    }
}
