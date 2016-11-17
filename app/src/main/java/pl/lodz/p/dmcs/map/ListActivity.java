package pl.lodz.p.dmcs.map;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    protected String token;
    protected Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ctx = this;
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
        task.setActivity(ListActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    final JSONArray buildings = obj.getJSONArray("data");
                    HashMap<String, List<String>> listDataChild = new HashMap<>();
                    ExpandableListView exListView = (ExpandableListView) findViewById(R.id.exListView);
                    if (exListView != null)
                    {
                        ArrayList<String> listDataHeader = new ArrayList<>();
                        for (int i = 0; i < buildings.length(); i++)
                        {
                            JSONObject building = buildings.getJSONObject(i);
                            JSONArray floors = building.getJSONArray("floors");
                            listDataHeader.add(building.getString("name"));
                            ArrayList<String> list = new ArrayList<>();
                            list.add("Zobacz opinie o budynku");
                            for (int j = 0; j < floors.length(); j++)
                            {
                                JSONObject floor = floors.getJSONObject(j);
                                JSONArray rooms = floor.getJSONArray("rooms");
                                for (int k = 0; k < rooms.length(); k++)
                                {
                                    JSONObject room = rooms.getJSONObject(k);
                                    list.add(room.getString("name") + " - Piętro " + floor.getInt("level"));
                                }
                            }
                            listDataChild.put(listDataHeader.get(i), list);
                        }
                        ExpandableListAdapter listAdapter = new ExpandableListAdapter(ctx, listDataHeader, listDataChild);
                        exListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                            @Override
                            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long idView) {
                                String type = "";
                                int id = 0;
                                if (childPosition == 0)
                                {
                                    type = "building";
                                    try {
                                        id = buildings.getJSONObject(groupPosition).getInt("id");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else
                                {
                                    type = "room";
                                    try {
                                        JSONObject building = buildings.getJSONObject(groupPosition);
                                        JSONArray floors = building.getJSONArray("floors");
                                        int childID = 1;
                                        for (int j = 0; j < floors.length(); j++)
                                        {
                                            JSONObject floor = floors.getJSONObject(j);
                                            JSONArray rooms = floor.getJSONArray("rooms");
                                            for (int k = 0; k < rooms.length(); k++)
                                            {
                                                JSONObject room = rooms.getJSONObject(k);
                                                if (childID == childPosition) id = room.getInt("id");
                                                childID++;
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                android.util.Log.i(">>>", "id: " + id + ", type: " + type);
                                Intent intent = new Intent(ListActivity.this, ShowRoomActivity.class);
                                intent.putExtra("token", token);
                                intent.putExtra("type", type);
                                intent.putExtra("id", id);
                                startActivity(intent);
                                //android.util.Log.i(">>>", groupPosition + " / " + childPosition + " / " + id + " / " + v.getY());
                                return false;
                            }
                        });
                        // setting list adapter
                        exListView.setAdapter(listAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }
}
