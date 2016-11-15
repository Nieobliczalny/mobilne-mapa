package pl.lodz.p.dmcs.map;

/**
 * Created by emicgaj on 2016-11-05.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;


public class NavigateActivity extends AppCompatActivity {

    private static ArrayAdapter<String> hints = null;
    protected String token = "";
    private List<Budynki> listaBudynkow = null;
    private JSONArray buildings = null;


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

        if (hints == null) {
            listaBudynkow = new ArrayList<Budynki>();
            hints = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
            Log.d("DOOOOOOOG",buildings.toString());
            if (buildings == null) return;
            for (int i = 0; i < buildings.length(); i++) {
                try {
                    JSONObject building = buildings.getJSONObject(i);
                    Double lat = building.getDouble("latitude");
                    Double lng = building.getDouble("longitude");
                    String name = building.getString("name");
                    Budynki temp = new Budynki(name, lat, lng);
                    Log.d("DOOOOOOOG TEMP",temp.toString());
                    listaBudynkow.add(temp);

                    hints.add(temp.getNazwa_Obiektu());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            AutoCompleteTextView nav_start = (AutoCompleteTextView) findViewById(R.id.search_source);
            nav_start.setAdapter(hints);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AutoCompleteTextView nav_end = (AutoCompleteTextView) findViewById(R.id.search_destination);
            nav_end.setAdapter(hints);
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
                    GeoPoint startPoint = new GeoPoint(19.45376900, 51.75269400);
                    GeoPoint endPoint = new GeoPoint(19.45596900, 51.74705900);
                    Integer startRoom = 6;
                    Integer endRoom = 91;
                    Integer startLevel = 0;
                    Integer endLevel = 4;
                    returnIntent.putExtra("startLat",startPoint.getLatitude());
                    returnIntent.putExtra("startLng",startPoint.getLongitude());
                    returnIntent.putExtra("endLat",endPoint.getLatitude());
                    returnIntent.putExtra("endLng",endPoint.getLongitude());
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

    }
}
