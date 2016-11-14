package pl.lodz.p.dmcs.map;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ShowRoomActivity extends AppCompatActivity {
    private String token;
    private int id;
    private String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_room);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            token = extras.getString("token");
            id = extras.getInt("id");
            type = extras.getString("type");
        }
    }
}
