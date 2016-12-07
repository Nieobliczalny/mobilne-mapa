package pl.lodz.p.dmcs.map;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class SearchRoomActivity extends AppCompatActivity {
    private ArrayAdapter<String> mAdapter;
    private final ArrayList<String> list = new ArrayList<>();
    private final ArrayList<ResultData> results = new ArrayList<>();
    protected String token;
    protected JSONArray buildings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_room);
        final Button btnSearch = (Button) findViewById(R.id.btnSearch);
        if (btnSearch != null)
        {
            btnSearch.setVisibility(View.INVISIBLE);
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.clear();
                    results.clear();
                    EditText searchEdit = (EditText) findViewById(R.id.searchText);
                    if (searchEdit == null) return;
                    Pattern pattern = Pattern.compile(searchEdit.getText().toString().replace(' ', '|'), Pattern.CASE_INSENSITIVE);//("/inf|ele");
                    for (int i = 0; i < buildings.length(); i++)
                    {
                        try {
                            JSONObject obj = buildings.getJSONObject(i);
                            //android.util.Log.i("FFF", pattern.toString() + " / " + obj.getString("name") + " / " + pattern.matcher(obj.getString("name")).find() + Pattern.matches("inf|ele", obj.getString("name")));
                            JSONArray floors = obj.getJSONArray("floors");
                            for (int j = 0; j < floors.length(); j++)
                            {
                                JSONObject floor = floors.getJSONObject(j);
                                JSONArray rooms = floor.getJSONArray("rooms");
                                for (int k = 0; k < rooms.length(); k++)
                                {
                                    JSONObject room = rooms.getJSONObject(k);
                                    if (pattern.matcher(room.getString("name")).find())
                                    {
                                        list.add(obj.getString("name") + "\r\nPiętro " + floor.getInt("level") + "\r\n" + room.getString("name"));
                                        ResultData r = new ResultData();
                                        r.roomID = room.getInt("id");
                                        r.roomLevel = floor.getInt("level");
                                        r.coords = new GeoPoint(obj.getDouble("latitude"), obj.getDouble("longitude"));
                                        results.add(r);
                                    }
                                }
                            }
                            if (pattern.matcher(obj.getString("name")).find())
                            {
                                list.add(obj.getString("name"));
                                ResultData r = new ResultData();
                                r.roomID = null;
                                r.roomLevel = null;
                                r.coords = new GeoPoint(obj.getDouble("latitude"), obj.getDouble("longitude"));
                                results.add(r);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        ListView listView = (ListView) findViewById(R.id.resultsView);
        if (listView != null)
        {
            listView.setAdapter(mAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    ResultData r = results.get(position);
                    Intent returnIntent = new Intent();
                    if (r.coords != null)
                    {
                        returnIntent.putExtra("searchLat", r.coords.getLatitude());
                        returnIntent.putExtra("searchLng", r.coords.getLongitude());
                    }
                    if (r.roomID != null)
                    {
                        returnIntent.putExtra("searchID", r.roomID);
                        returnIntent.putExtra("searchLevel", r.roomLevel);
                    }
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }
            });
        }
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
        task.setActivity(SearchRoomActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    buildings = obj.getJSONArray("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (btnSearch != null) btnSearch.setVisibility(View.VISIBLE);
            }
        });
        task.execute(data);
    }

    private class ResultData
    {
        public Integer roomLevel;
        public Integer roomID;
        public GeoPoint coords;
    }
}
