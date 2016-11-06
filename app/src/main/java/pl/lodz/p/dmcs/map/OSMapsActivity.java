package pl.lodz.p.dmcs.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OSMapsActivity extends AppCompatActivity {
    private final static int REQUEST_WRITE_STORAGE = 1;
    private String token;
    private JSONArray buildings = null;
    private int level = 0;
    private final Map<Integer, TilesOverlay> customLayers = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(OSMapsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(OSMapsActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);

            } else {
                initActivity();
            }
        } else {
            initActivity();
        }

    }

    //Magic code, DO NOT TOUCH!
    private void initActivity()
    {
        //important! set your user agent to prevent getting banned from the osm servers
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
                    File osmCache = new File(getCacheDir(), "osmdroid");
                    boolean test = osmCache.mkdirs();
                    (new File(getCacheDir(), "osmdroid/tiles")).mkdirs();
                    org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setOfflineMapsPath(osmCache.getPath());
                    org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setCachePath(osmCache.getPath());
                    OpenStreetMapTileProviderConstants.TILE_PATH_BASE = osmCache;
                    String sqlitepath = org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.getBasePath().getPath() + "/tiles/cache.db";
                    File dbDir = new File(sqlitepath);
                    dbDir.mkdirs();
                    //android.util.Log.i(">>>>>TAG", org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.getBasePath().getPath() + " :: " + osmCache.getPath());
                    //SQLiteDatabase db = SQLiteDatabase.openDatabase(sqlitepath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
                    //if (db != null) db.close();

        setContentView(R.layout.activity_osmaps);
        MapView map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(16);
        GeoPoint startPoint = new GeoPoint(51.752694, 19.453769);
        mapController.setCenter(startPoint);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            token = extras.getString("token");
        }

        JSONObject data = new JSONObject();
        try {
            data.put("action", "getBuildings");
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }
        SendPostTask task = new SendPostTask();
        task.setActivity(OSMapsActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    buildings = obj.getJSONArray("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                addBuildings();
            }
        });
        task.execute(data);
    }

    protected void addBuildings()
    {
        final Button btnLevelUp = (Button) findViewById(R.id.btnLevelUp);
        final Button btnLevelDown = (Button) findViewById(R.id.btnLevelDown);
        final TextView levelText = (TextView) findViewById(R.id.level);
        final MapView mMap = (MapView) findViewById(R.id.map);
        if (mMap == null || buildings == null) return;
        //Powiększenie zooma z 18 na 19
        mMap.setMaxZoomLevel(19);
        mMap.getTileProvider().setTileSource(new XYTileSource("Mapnik",
                0, 19, 256, ".png", new String[] {
                "http://a.tile.openstreetmap.org/",
                "http://b.tile.openstreetmap.org/",
                "http://c.tile.openstreetmap.org/" }));

        for (int i = -1; i < 4; i++) {
            customLayers.put(i, Utilities.CreateTilesOverlay(OSMapsActivity.this, i));
            mMap.getOverlays().add(customLayers.get(i));
        }
        customLayers.get(0).setEnabled(true);


        if (btnLevelDown != null) btnLevelDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level--;
                Iterator<TilesOverlay> layers = customLayers.values().iterator();
                while (layers.hasNext()) layers.next().setEnabled(false);
                if (customLayers.containsKey(level)) customLayers.get(level).setEnabled(true);
                levelText.setText(level + "");
                mMap.invalidate();
            }
        });
        if (btnLevelUp != null) btnLevelUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level++;
                Iterator<TilesOverlay> layers = customLayers.values().iterator();
                while (layers.hasNext()) layers.next().setEnabled(false);
                if (customLayers.containsKey(level)) customLayers.get(level).setEnabled(true);
                levelText.setText(level + "");
                mMap.invalidate();
            }
        });

        mMap.setMapListener(new MapListener() {
            public boolean onZoom(ZoomEvent arg0) {
                //android.util.Log.i("TAG", "ZOOM: " + arg0.toString());
                if (btnLevelDown == null || btnLevelUp == null || levelText == null) return false;
                if (arg0.getZoomLevel() == 19)
                {
                    btnLevelDown.setVisibility(View.VISIBLE);
                    btnLevelUp.setVisibility(View.VISIBLE);
                    levelText.setVisibility(View.VISIBLE);
                    level = 0;
                }
                else
                {
                    btnLevelDown.setVisibility(View.INVISIBLE);
                    btnLevelUp.setVisibility(View.INVISIBLE);
                    levelText.setVisibility(View.INVISIBLE);
                }
                return false;
            }

            public boolean onScroll(ScrollEvent arg0) {
                //android.util.Log.i("TAG", "SCROLL: " + arg0.toString());
                return false;
            }
        } );

        final ArrayList<OverlayItem> items = new ArrayList<>();
        for (int i = 0; i < buildings.length(); i++)
        {
            try {
                JSONObject building = buildings.getJSONObject(i);
                android.util.Log.i("XD", building.getDouble("latitude") + " " + building.getDouble("longitude"));
                OverlayItem myLocationOverlayItem = new OverlayItem(building.getString("name"), "More info on " + building.getString("name"), new GeoPoint(building.getDouble("latitude"), building.getDouble("longitude")));
                Drawable myCurrentLocationMarker = getResources().getDrawable(R.drawable.marker);
                myLocationOverlayItem.setMarker(myCurrentLocationMarker);

                items.add(myLocationOverlayItem);
                //LatLng weeia = new LatLng(building.getDouble("latitude"), building.getDouble("longitude"));
                //mMap.addMarker(new MarkerOptions().position(weeia).title(building.getString("name")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ItemizedIconOverlay<OverlayItem> currentLocationOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        Toast alert = Toast.makeText(OSMapsActivity.this, item.getTitle() + " @ " + item.getSnippet(), Toast.LENGTH_SHORT);
                        alert.show();
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, this);
        mMap.getOverlays().add(currentLocationOverlay);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    initActivity();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast alert = Toast.makeText(this, "To display maps app needs to have Write Storage permission granted", Toast.LENGTH_SHORT);
                    alert.show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
