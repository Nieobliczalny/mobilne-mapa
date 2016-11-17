package pl.lodz.p.dmcs.map;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ShowUserActivity extends AppCompatActivity {
    protected int id;
    protected String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            token = extras.getString("token");
            id = extras.getInt("id");
        }

        JSONObject data = new JSONObject();
        try {
            data.put("action", "getUserById");
            data.put("id", id);
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }
        SendPostTask task = new SendPostTask();
        task.setActivity(ShowUserActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    final JSONObject user = obj.getJSONObject("data");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView userName = (TextView) findViewById(R.id.userName);
                            TextView userID = (TextView) findViewById(R.id.userID);
                            TextView addedBuildings = (TextView) findViewById(R.id.addedBuildings);
                            TextView addedComments = (TextView) findViewById(R.id.addedComments);
                            TextView addedLikes = (TextView) findViewById(R.id.addedLikes);
                            TextView addedRooms = (TextView) findViewById(R.id.addedRooms);
                            try {
                                if (userName != null) userName.setText(user.getString("nick"));
                                if (userID != null) userID.setText(userID.getText() + user.getString("id"));
                                if (addedBuildings != null) addedBuildings.setText(addedBuildings.getText() + user.getString("addedBuildings"));
                                if (addedComments != null) addedComments.setText(addedComments.getText() + user.getString("addedComments"));
                                if (addedLikes != null) addedLikes.setText(addedLikes.getText() + user.getString("addedLikes"));
                                if (addedRooms != null) addedRooms.setText(addedRooms.getText() + user.getString("addedRooms"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }
}
