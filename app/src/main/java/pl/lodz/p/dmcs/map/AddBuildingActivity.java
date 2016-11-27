package pl.lodz.p.dmcs.map;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class AddBuildingActivity extends AppCompatActivity {
    protected String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_building);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            token = extras.getString("token");
        }
        Button btnSend = (Button) findViewById(R.id.btnSend);
        if (btnSend != null) btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText name = (EditText) findViewById(R.id.newName);
                final EditText lat = (EditText) findViewById(R.id.newLat);
                final EditText lng = (EditText) findViewById(R.id.newLng);
                JSONObject data = new JSONObject();
                try {
                    data.put("action", "addBuilding");
                    data.put("name", name.getText().toString());
                    data.put("latitude", lat.getText().toString());
                    data.put("longitude", lng.getText().toString());
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
                        Toast t = Toast.makeText(AddBuildingActivity.this, "Dane o budynku wysłane. Po rozpatrzeniu propozycji przez Administratora otrzymasz e-mail o akceptacji/odrzuceniu.", Toast.LENGTH_SHORT);
                        t.show();
                    }
                });
                task.execute(data);
            }
        });
    }
}
