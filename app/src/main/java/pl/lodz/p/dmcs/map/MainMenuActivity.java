package pl.lodz.p.dmcs.map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
                Intent intent = new Intent(MainMenuActivity.this, OSMapsActivity.class);
                intent.putExtra("token", token);
                //startActivity(intent);
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
    }
}
