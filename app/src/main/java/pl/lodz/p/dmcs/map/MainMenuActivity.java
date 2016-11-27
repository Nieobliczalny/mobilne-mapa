package pl.lodz.p.dmcs.map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

public class MainMenuActivity extends AppCompatActivity {
    protected String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            token = extras.getString("token");
        }

        //Sprawdzam, czy jestem Adminem
        JSONObject data = new JSONObject();
        try {
            data.put("action", "getAdmins");
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task = new SendPostTask();
        task.setActivity(MainMenuActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    if (obj.getJSONArray("data").length() > 0) {
                        Button btnAdmin = (Button) findViewById(R.id.btnAdmin);
                        if (btnAdmin != null) btnAdmin.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);

        Button btnMap = (Button) findViewById(R.id.btnMap);
        if (btnMap != null) btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, OSMapsActivity.class);
                intent.putExtra("token", token);
                startActivity(intent);
            }
        });

        Button btnList = (Button) findViewById(R.id.btnList);
        if (btnList != null) btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ListActivity.class);
                intent.putExtra("token", token);
                startActivity(intent);
            }
        });

        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        if (btnLogout != null) btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        Button btnAdmin = (Button) findViewById(R.id.btnAdmin);
        if (btnAdmin != null) btnAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, AdminActivity.class);
                intent.putExtra("token", token);
                startActivity(intent);
            }
        });

        Button btnAddBuilding = (Button) findViewById(R.id.btnAddBuilding);
        if (btnAddBuilding != null) btnAddBuilding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, AddBuildingActivity.class);
                intent.putExtra("token", token);
                startActivity(intent);
            }
        });
    }
}
