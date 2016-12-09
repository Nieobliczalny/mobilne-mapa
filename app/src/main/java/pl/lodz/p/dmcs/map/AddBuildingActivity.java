package pl.lodz.p.dmcs.map;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddBuildingActivity extends AppCompatActivity {
    protected String token;
    private String temp;
    protected MyCustomAdapter dataAdapter;
    protected ArrayList<UnitData> units = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_building);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            token = extras.getString("token");
        }
        JSONObject data2 = new JSONObject();
        try {
            data2.put("action", "getUnits");
            data2.put("token", token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SendPostTask task2 = new SendPostTask();
        task2.setActivity(AddBuildingActivity.this);
        task2.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    JSONArray unitArray = obj.getJSONArray("data");
                    for (int i = 0; i < unitArray.length(); i++)
                    {
                        JSONObject unit = unitArray.getJSONObject(i);
                        units.add(new UnitData(unit.getInt("id"), unit.getString("name")));
                    }

                    dataAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task2.execute(data2);

        dataAdapter = new MyCustomAdapter(this, R.layout.checkbox_list_item, units);
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(dataAdapter);

        Button btnSend = (Button) findViewById(R.id.btnSend);
        if (btnSend != null) btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText name = (EditText) findViewById(R.id.newName);
                final EditText lat = (EditText) findViewById(R.id.newLat);
                final EditText lng = (EditText) findViewById(R.id.newLng);
                final EditText un = (EditText) findViewById(R.id.unoficial_name);
                final EditText number = (EditText) findViewById(R.id.number);
                temp = un.getText().toString();
                String[] temp2 = temp.split(",");
                StringBuilder finalny = new StringBuilder();
                int index = 0;
                for (String t: temp2) {
                    finalny.append(t.trim());
                    if(index < temp2.length-1) finalny.append(" ; ");
                    index ++;
                }

                JSONObject data = new JSONObject();
                try {
                    data.put("action", "addBuilding");
                    data.put("name", name.getText().toString().trim());
                    data.put("latitude", lat.getText().toString().trim());
                    data.put("longitude", lng.getText().toString().trim());
                    data.put("unofficial_name",finalny.toString());
                    data.put("number", number.getText().toString().trim());
                    JSONArray unitData = new JSONArray();
                    for (UnitData u : dataAdapter.unitList)
                    {
                        if (u.isChecked()) unitData.put(u.getId());
                    }
                    data.put("units", unitData);

                    data.put("token", token);
                } catch (Exception e){
                    e.printStackTrace();
                }

                SendPostTask task = new SendPostTask();
                task.setActivity(AddBuildingActivity.this);
                task.setResponseListener(new JsonResponseListener() {
                    @Override
                    public void onResponse(final JSONObject obj) {
                        name.setText("");
                        lat.setText("");
                        lng.setText("");
                        un.setText("");
                        number.setText("");
                        Toast t = Toast.makeText(AddBuildingActivity.this, "Dane o budynku wysłane. Po rozpatrzeniu propozycji przez Administratora otrzymasz e-mail o akceptacji/odrzuceniu.", Toast.LENGTH_SHORT);
                        t.show();
                    }
                });
                task.execute(data);
            }
        });
    }

    private class MyCustomAdapter extends ArrayAdapter<UnitData> {

        private ArrayList<UnitData> unitList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<UnitData> unitList) {
            super(context, textViewResourceId, unitList);
            this.unitList = (unitList);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.checkbox_list_item, parent, false);
            final TextView name = (TextView) rowView.findViewById(R.id.code);
            final CheckBox chk = (CheckBox) rowView.findViewById(R.id.checkBox1);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chk.setChecked(!chk.isChecked());
                    unitList.get(position).setChecked(chk.isChecked());
                }
            });
            name.setText(unitList.get(position).getName());
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chk.setChecked(!chk.isChecked());
                    unitList.get(position).setChecked(chk.isChecked());
                }
            });
            chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    unitList.get(position).setChecked(isChecked);
                }
            });
            return rowView;

        }

    }
}
