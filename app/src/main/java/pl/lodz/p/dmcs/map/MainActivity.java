package pl.lodz.p.dmcs.map;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    protected Context ctx;
    protected String token = "";
    private final static int MENU_ACTIVITY_REQUEST_CODE = 10;
    private final static int MENU_SETTINGS_REQUEST_CODE = 11;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        ctx = this;
        if (!isOnline()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do działania aplikacji wymagane jest połączenie z internetem. Włącz transmisję danych lub WiFi w Ustawieniach.")
                    .setTitle("Błąd połączenia")
                    .setCancelable(false)
                    .setPositiveButton("Otwórz Ustawienia",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                    startActivityForResult(i, MENU_SETTINGS_REQUEST_CODE);
                                }
                            }
                    )
                    .setNegativeButton("Wyjdź",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    MainActivity.this.finish();
                                }
                            }
                    );
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            checkIfLoggedIn();
        }
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
                            CheckBox saveLogin = (CheckBox) findViewById(R.id.saveToken);
                            if (saveLogin != null && saveLogin.isChecked())
                            {
                                SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("loginToken", token);
                                editor.commit();
                            }
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
                                    startActivityForResult(intent, MENU_ACTIVITY_REQUEST_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MENU_ACTIVITY_REQUEST_CODE) {
            if(resultCode != Activity.RESULT_OK){
                finish();
            }
            else
            {
                SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove("loginToken");
                editor.commit();
            }
        }
        if (requestCode == MENU_SETTINGS_REQUEST_CODE) {
            if (isOnline()) checkIfLoggedIn();
            else finish();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    private void checkIfLoggedIn() {
        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        token = sharedPref.getString("loginToken", "");
        if (!token.equalsIgnoreCase(""))
        {
            Intent intent = new Intent(MainActivity.this, OSMapsActivity.class);
            intent.putExtra("token", token);
            startActivityForResult(intent, MENU_ACTIVITY_REQUEST_CODE);
        }
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Wyjście z aplikacji")
                .setMessage("Czy na pewno chcesz wyjść z aplikacji?")
                .setPositiveButton("Tak", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("Nie", null)
                .show();
    }
}
