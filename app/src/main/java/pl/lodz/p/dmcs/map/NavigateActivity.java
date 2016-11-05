package pl.lodz.p.dmcs.map;

/**
 * Created by emicgaj on 2016-11-05.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class NavigateActivity extends AppCompatActivity {

    private static ArrayAdapter<String> hints = null;
    protected String token = "";
    private List<Budynki> listaBudynkow = null;
    private JSONArray buildings = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            hints = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
            if (buildings == null) return;
            for (int i = 0; i < buildings.length(); i++) {
                try {
                    JSONObject building = buildings.getJSONObject(i);
                    Double lat = building.getDouble("latitude");
                    Double lng = building.getDouble("longitude");
                    String name = building.getString("name");
                    Budynki temp = new Budynki(name, lat, lng);
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

    }
}
