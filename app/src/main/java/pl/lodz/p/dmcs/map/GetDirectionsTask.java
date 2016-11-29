package pl.lodz.p.dmcs.map;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Krystian on 2016-11-14.
 */

public class GetDirectionsTask extends AsyncTask<String, Void, String> {
    private MapView mMap = null;
    private List<Overlay> om = null;
    public void setMap(MapView m)
    {
        mMap = m;
    }
    public void setOverlayContainer(List<Overlay> l)
    {
        om = l;
    }

    // Downloading data in non-ui thread
    @Override
    protected String doInBackground(String... url) {

        // For storing data from web service
        String data = "";

        try{
            // Fetching the data from web service
            Log.d("Background Task", url[0]);
            data = downloadUrl(url[0]);
        }catch(Exception e){
            Log.d("Background Task",e.toString());
        }
        return data;
    }

    /**
     * Executes in UI thread, after the execution of doInBackground()
     * @param res
     */
    @Override
    protected void onPostExecute(String res) {
        super.onPostExecute(res);
        try {
            Log.d("Background Task", res);
            JSONObject jObject = new JSONObject(res);
            DirectionsJSONParser parser = new DirectionsJSONParser();
            // Starts parsing data
            List<List<HashMap<String,String>>> result = parser.parse(jObject);
            ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                waypoints = new ArrayList<GeoPoint>();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    GeoPoint position = new GeoPoint(lat, lng);

                    waypoints.add(position);
                }
            }

            Road road = new Road(waypoints);
            // then, build an overlay with the route shape:
            Polyline roadOverlay = RoadManager.buildRoadOverlay(road);//, mMap.getContext());
            roadOverlay.setColor(Color.RED);
            roadOverlay.setWidth(8);


            //Add Route Overlays into map
            mMap.getOverlays().add(roadOverlay);
            om.add(roadOverlay);

            mMap.invalidate();
        } catch (JSONException e) {
            Log.d("Background Task", e.toString());
        }
    }

    public static String getDirectionsUrl(GeoPoint origin,GeoPoint dest){

        // Origin of route
        String str_origin = "origin="+origin.getLongitude()+","+origin.getLatitude();

        // Destination of route
        String str_dest = "destination="+dest.getLongitude()+","+dest.getLatitude();

        // Sensor enabled
        String sensor = "sensor=false";

        // Enable walking mode
        String walking = "mode=walking";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor + "&" + walking;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}