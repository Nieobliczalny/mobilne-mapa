package pl.lodz.p.dmcs.map;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    protected Context ctx;
    protected String token = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        ctx = this;
        Button loginBtn = (Button) findViewById(R.id.btnLogIn);
        if (loginBtn != null) loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject data = new JSONObject();
                try {
                    data.put("action", "login");
                    EditText emailField = (EditText) findViewById(R.id.email);
                    data.put("email", emailField != null ? emailField.getText() : "");
                    EditText passField = (EditText) findViewById(R.id.pass);
                    data.put("password", passField != null ? passField.getText() : "");
                } catch (Exception e){
                    e.printStackTrace();
                }
                SendPostTask task = new SendPostTask();
                task.setActivity(MainActivity.this);
                task.setResponseListener(new JsonResponseListener() {
                    @Override
                    public void onResponse(final JSONObject obj) {
                        try {
                            if (obj.has("token")) token = obj.getString("token");
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast t = Toast.makeText(ctx, "Welcome " + obj.getString("nick") + "!", Toast.LENGTH_SHORT);
                                    t.show();
                                    Intent intent = new Intent(MainActivity.this, OSMapsActivity.class);
                                    intent.putExtra("token", token);
                                    startActivity(intent);
                                } catch (JSONException ex){
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }
                });
                task.execute(data);
            }
        });
        Button registerBtn = (Button) findViewById(R.id.btnRegister);
        if (registerBtn != null) registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject data = new JSONObject();
                try {
                    data.put("action", "register");
                    EditText emailField = (EditText) findViewById(R.id.emailNew);
                    data.put("email", emailField != null ? emailField.getText() : "");
                    EditText passField = (EditText) findViewById(R.id.passNew);
                    data.put("password", passField != null ? passField.getText() : "");
                    EditText nickField = (EditText) findViewById(R.id.nickNew);
                    data.put("nick", nickField != null ? nickField.getText() : "");
                } catch (Exception e){
                    e.printStackTrace();
                }
                SendPostTask task = new SendPostTask();
                task.setActivity(MainActivity.this);
                task.setResponseListener(new JsonResponseListener() {
                    @Override
                    public void onResponse(final JSONObject obj) {
                        try {
                            if (obj.has("token")) token = obj.getString("token");
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast t = Toast.makeText(ctx, "Account created, you can now login " + obj.getString("nick") + "!", Toast.LENGTH_SHORT);
                                    t.show();
                                } catch (JSONException ex){
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }
                });
                task.execute(data);
            }
        });
    }
}
