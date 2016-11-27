package pl.lodz.p.dmcs.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.File;
import java.util.ArrayList;

public class AdminMapsActivity extends AppCompatActivity {
    private final static int REQUEST_WRITE_STORAGE = 1;
    private FolderOverlay currentLocationOverlay = new FolderOverlay();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_maps);
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(AdminMapsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AdminMapsActivity.this,
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

        setContentView(R.layout.activity_admin_maps);
        MapView map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(18);
        GeoPoint startPoint = new GeoPoint(51.752694, 19.453769);
        mapController.setCenter(startPoint);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String[] pols = extras.getStringArray("polygons");
            if (pols != null) {
                for (int i = 0; i < pols.length; i++) {
                    String[] polPnts = pols[i].split(" ");
                    if (polPnts.length == 1)
                    {
                        //Punkt
                        String[] coords = polPnts[0].split(",");
                        GeoPoint p = new GeoPoint(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
                        if (i == 0) mapController.setCenter(p);
                        Polygon circle = new Polygon(this);
                        circle.setPoints(Polygon.pointsAsCircle(p, 1.0));
                        circle.setFillColor(0xFFA00000);
                        circle.setStrokeColor(Color.RED);
                        circle.setStrokeWidth(1);
                        currentLocationOverlay.add(circle);
                    }
                    else
                    {
                        ArrayList<GeoPoint> o = new ArrayList<>();
                        for (int j = 0; j < polPnts.length; j++)
                        {
                            String[] coords = polPnts[j].split(",");
                            GeoPoint p = new GeoPoint(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
                            o.add(p);
                        }
                        if (i == 0) mapController.setCenter(o.get(0));
                        Road road = new Road(o);
                        // then, build an overlay with the route shape:
                        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);//, mMap.getContext());
                        roadOverlay.setColor(Color.GREEN);
                        roadOverlay.setWidth(1);
                        currentLocationOverlay.add(roadOverlay);
                    }
                }
            }
        }
        map.getOverlays().add(currentLocationOverlay);
    }
}
