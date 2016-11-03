package pl.lodz.p.dmcs.map;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Krystian on 2016-11-03.
 */

public class SendPostTask extends AsyncTask<JSONObject, Integer, String> {
    private JsonResponseListener responseListener = null;
    private Activity ctx = null;
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
                ctx.runOnUiThread(new Runnable() {
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
                ctx.runOnUiThread(new Runnable() {
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
                ctx.runOnUiThread(new Runnable() {
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
    public void setActivity(Activity activity)
    {
        ctx = activity;
    }
}
