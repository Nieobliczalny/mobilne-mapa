package pl.lodz.p.dmcs.map;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.EventListener;

public class MainActivity extends AppCompatActivity {
    protected Context ctx;
    protected String token = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                task.setResponseListener(new JsonResponseListener() {
                    @Override
                    public void onResponse(final JSONObject obj) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast t = Toast.makeText(ctx, "Welcome " + obj.getString("nick") + "!", Toast.LENGTH_SHORT);
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
                task.setResponseListener(new JsonResponseListener() {
                    @Override
                    public void onResponse(final JSONObject obj) {
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

    private class SendPostTask extends AsyncTask<JSONObject, Integer, String> {
        private JsonResponseListener responseListener = null;
        @Override
        protected String doInBackground(JSONObject... params) {
            int count = params.length;
            for (int i = 0; i < count; i++) {
                try {
                    URL u = new URL("http://mobilne.kjozwiak.ovh/index.php");
                    InputStream is = downloadUrl(u, params[i]);
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuilder jsonStringBuilder = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        jsonStringBuilder.append(line);
                    }
                    is.close();
                    return jsonStringBuilder.toString();
                } catch (Exception mue) {
                    //
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast t = Toast.makeText(ctx, "Error while contacting server", Toast.LENGTH_SHORT);
                            t.show();
                        }
                    });
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String l) {
            try {
                final JSONObject json = new JSONObject(l);
                if (json.has("error"))
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast t = Toast.makeText(ctx, "Error - " + json.getString("error"), Toast.LENGTH_SHORT);
                                t.show();
                            } catch (Exception e) {
                                //
                            }
                        }
                    });
                }
                else
                {
                    if (json.has("token")) token = json.getString("token");
                    if (responseListener != null) responseListener.onResponse(json);
                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast t = Toast.makeText(ctx, "Got response! " + l, Toast.LENGTH_SHORT);
                                t.show();
                            } catch (Exception e) {
                                //
                            }
                        }
                    });*/
                }
            } catch (Exception e){
                //
                if (l != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast t = Toast.makeText(ctx, "Error while parsing response", Toast.LENGTH_SHORT);
                            t.show();
                        }
                    });
                }
            }
        }

        private InputStream downloadUrl(URL url, JSONObject data) throws IOException {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            // Starts the query
            OutputStream outputPost = new BufferedOutputStream(conn.getOutputStream());
            outputPost.write(data.toString().getBytes());
            outputPost.flush();
            outputPost.close();
            return conn.getInputStream();
        }

        public void setResponseListener(JsonResponseListener jrl)
        {
            responseListener = jrl;
        }
    }
}
