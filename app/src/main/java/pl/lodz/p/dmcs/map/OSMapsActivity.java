package pl.lodz.p.dmcs.map;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OSMapsActivity extends AppCompatActivity {
    private final static int REQUEST_WRITE_STORAGE = 1;
    public final static int ACTIVITY_NAVIGATE_REQUEST_CODE = 2;
    private String token;
    private JSONArray buildings = null;
    private int level = 0;
    private int minLevel = 0;
    private int maxLevel = 0;
    private final Map<Integer, List<FolderOverlay>> customLayers = new HashMap<>();
    //private ItemizedIconOverlay<OverlayItem> currentLocationOverlay = null;
    private FolderOverlay currentLocationOverlay = new FolderOverlay();
    protected List<Overlay> navigationOverlays = new ArrayList<>();
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

        //Navigate button
        Button btnNavigate = (Button) findViewById(R.id.btnNavigate);
        if (btnNavigate != null) btnNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OSMapsActivity.this, NavigateActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, ACTIVITY_NAVIGATE_REQUEST_CODE);
            }
        });
    }

    protected void addBuildings()
    {
        final Button btnLevelUp = (Button) findViewById(R.id.btnLevelUp);
        final Button btnLevelDown = (Button) findViewById(R.id.btnLevelDown);
        final TextView levelText = (TextView) findViewById(R.id.level);
        final MapView mMap = (MapView) findViewById(R.id.map);
        if (mMap == null || buildings == null) return;
        //Powiększenie zooma z 18 na 21
        mMap.setMaxZoomLevel(21);
        mMap.getTileProvider().setTileSource(new XYTileSource("Mapnik",
                0, 21, 256, ".png", new String[] {
                "http://a.tile.openstreetmap.org/",
                "http://b.tile.openstreetmap.org/",
                "http://c.tile.openstreetmap.org/" }));



        if (btnLevelDown != null) btnLevelDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (level <= minLevel) return;
                if (customLayers.containsKey(level))
                {
                    for (int i = 0; i < customLayers.get(level).size(); i++)
                    {
                        customLayers.get(level).get(i).setEnabled(false);
                    }
                }
                level--;
                if (customLayers.containsKey(level))
                {
                    for (int i = 0; i < customLayers.get(level).size(); i++)
                    {
                        customLayers.get(level).get(i).setEnabled(true);
                    }
                }
                levelText.setText(level + "");
                mMap.invalidate();
            }
        });
        if (btnLevelUp != null) btnLevelUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (level >= maxLevel) return;
                if (customLayers.containsKey(level))
                {
                    for (int i = 0; i < customLayers.get(level).size(); i++)
                    {
                        customLayers.get(level).get(i).setEnabled(false);
                    }
                }
                level++;
                if (customLayers.containsKey(level))
                {
                    for (int i = 0; i < customLayers.get(level).size(); i++)
                    {
                        customLayers.get(level).get(i).setEnabled(true);
                    }
                }
                levelText.setText(level + "");
                mMap.invalidate();
            }
        });

        mMap.setMapListener(new MapListener() {
            public boolean onZoom(ZoomEvent arg0) {
                //android.util.Log.i("TAG", "ZOOM: " + arg0.toString());
                if (btnLevelDown == null || btnLevelUp == null || levelText == null) return false;
                if (arg0.getZoomLevel() >= 19)
                {
                    btnLevelDown.setVisibility(View.VISIBLE);
                    btnLevelUp.setVisibility(View.VISIBLE);
                    levelText.setVisibility(View.VISIBLE);
                    if (customLayers.containsKey(level))
                    {
                        for (int i = 0; i < customLayers.get(level).size(); i++)
                        {
                            customLayers.get(level).get(i).setEnabled(true);
                        }
                    }
                    if (mMap.getOverlays().contains(currentLocationOverlay)) mMap.getOverlays().remove(currentLocationOverlay);
                    mMap.invalidate();
                }
                else
                {
                    btnLevelDown.setVisibility(View.INVISIBLE);
                    btnLevelUp.setVisibility(View.INVISIBLE);
                    levelText.setVisibility(View.INVISIBLE);
                    if (customLayers.containsKey(level))
                    {
                        for (int i = 0; i < customLayers.get(level).size(); i++)
                        {
                            customLayers.get(level).get(i).setEnabled(false);
                        }
                    }
                    if (!mMap.getOverlays().contains(currentLocationOverlay)) mMap.getOverlays().add(currentLocationOverlay);
                    mMap.invalidate();
                }
                return false;
            }

            public boolean onScroll(ScrollEvent arg0) {
                //android.util.Log.i("TAG", "SCROLL: " + arg0.toString());
                return false;
            }
        } );

        //final ArrayList<OverlayItem> items = new ArrayList<>();
        //FolderOverlay fo = new FolderOverlay();
        for (int i = 0; i < buildings.length(); i++)
        {
            try {
                JSONObject building = buildings.getJSONObject(i);
                android.util.Log.i("XD", building.getDouble("latitude") + " " + building.getDouble("longitude"));
                /*OverlayItem myLocationOverlayItem = new OverlayItem(building.getString("name"), building.getString("rating") + " z " + building.getInt("ratingCount") + " głosów", new GeoPoint(building.getDouble("latitude"), building.getDouble("longitude")));
                Drawable myCurrentLocationMarker = getResources().getDrawable(R.drawable.marker);
                myLocationOverlayItem.setMarker(myCurrentLocationMarker);*/
                GeoPoint p = new GeoPoint(building.getDouble("latitude"), building.getDouble("longitude"));
                Polygon circle = new Polygon(this);
                circle.setPoints(Polygon.pointsAsCircle(p, 40.0));
                circle.setFillColor(0x40404040);
                circle.setStrokeColor(Color.RED);
                circle.setStrokeWidth(1);
                circle.setInfoWindow(new CustomInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, mMap, building.getInt("id"), "building", token, this));
                circle.setTitle(building.getString("name"));
                circle.setSnippet(building.getString("rating") + " z " + building.getInt("ratingCount") + " głosów");
                currentLocationOverlay.add(circle);

                JSONArray floors = building.getJSONArray("floors");
                for (int j = 0; j < floors.length(); j++)
                {
                    JSONObject floor = floors.getJSONObject(j);
                    int level = floor.getInt("level");
                    if (!customLayers.containsKey(level)) customLayers.put(level, new ArrayList<FolderOverlay>());
                    LoadKmlTask task = new LoadKmlTask();
                    task.setList(customLayers.get(level));
                    task.setMap(mMap);
                    task.setToken(token);
                    task.setActivity(OSMapsActivity.this);
                    task.execute("http://mobilne.kjozwiak.ovh/map.php?b=" + building.getInt("id") + "&f=" + level);
                    if (level < minLevel) minLevel = level;
                    if (level > maxLevel) maxLevel = level;
                }
                //LatLng weeia = new LatLng(building.getDouble("latitude"), building.getDouble("longitude"));
                //mMap.addMarker(new MarkerOptions().position(weeia).title(building.getString("name")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
/*
        currentLocationOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        Toast alert = Toast.makeText(OSMapsActivity.this, item.getTitle() + ": " + item.getSnippet(), Toast.LENGTH_SHORT);
                        alert.show();
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, this);*/
        mMap.getOverlays().add(currentLocationOverlay);
        /*try {
            //addRouteOverlay(new GeoPoint(19.45376900, 51.75269400), new GeoPoint(19.45596900, 51.74705900));
            GetDirectionsTask gdt = new GetDirectionsTask();
            gdt.setMap(mMap);
            gdt.execute(GetDirectionsTask.getDirectionsUrl(new GeoPoint(19.45376900, 51.75269400), new GeoPoint(19.45596900, 51.74705900)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }*/
        Button btnNavCancel = (Button) findViewById(R.id.btnNavigateCancel);
        if (btnNavCancel != null) btnNavCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < navigationOverlays.size(); i++)
                {
                    mMap.getOverlays().remove(navigationOverlays.get(i));
                }
                mMap.invalidate();
                v.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACTIVITY_NAVIGATE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                final MapView mMap = (MapView) findViewById(R.id.map);
                GeoPoint startPoint = new GeoPoint(data.getDoubleExtra("startLat", 0.0), data.getDoubleExtra("startLng", 0.0));
                GeoPoint endPoint = new GeoPoint(data.getDoubleExtra("endLat", 0.0), data.getDoubleExtra("endLng", 0.0));
                Integer startRoom = data.hasExtra("startRoom") ? data.getIntExtra("startRoom", 0) : null;
                Integer endRoom = data.hasExtra("endRoom") ? data.getIntExtra("endRoom", 0) : null;
                GetDirectionsTask gdt = new GetDirectionsTask();
                gdt.setMap(mMap);
                gdt.setOverlayContainer(navigationOverlays);
                gdt.execute(GetDirectionsTask.getDirectionsUrl(startPoint, endPoint));
                Button btnNavCancel = (Button) findViewById(R.id.btnNavigateCancel);
                if (btnNavCancel != null) btnNavCancel.setVisibility(View.VISIBLE);
            }
            //if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            //}
        }
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

    public void addRouteOverlay(GeoPoint startPoint, GeoPoint endPoint)
    {
        final MapView mMap = (MapView) findViewById(R.id.map);
        //1 Routing via road manager
        RoadManager roadManager = new MapQuestRoadManager("sLxBDYPmotx36lpYmaqWkS0uuG3uVmnF");//new GraphHopperRoadManager("716c4886-f241-4a2c-a23d-b9b0c49174a1", false);//new MapQuestRoadManager("_YOUR MAPQUEST API KEY_");//new OSRMRoadManager(this);
        roadManager.addRequestOption("routeType=pedestrian");

        //Then, retreive the road between your start and end point:
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        waypoints.add(endPoint); //end point

        Road road = roadManager.getRoad(waypoints);

        // then, build an overlay with the route shape:
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);//, mMap.getContext());
        roadOverlay.setColor(Color.GREEN);


        //Add Route Overlays into map
        mMap.getOverlays().add(roadOverlay);

        mMap.invalidate();//refesh map
/*
        Drawable    marker = getResources().getDrawable(R.drawable.marker);

        final ArrayList<OverlayItem> roadItems =
                new ArrayList<OverlayItem>();
        //ItemizedOverlayWithBubble<OverlayItem> roadNodes =
        //        new ItemizedOverlayWithBubble<OverlayItem>(this, roadItems, mMap);
        ArrayList<OverlayItem> roadNodes = new ArrayList<>();


        for (int i=0; i<road.mNodes.size(); i++)
        {
            RoadNode node = road.mNodes.get(i);
            OverlayItem nodeMarker = new OverlayItem("Step "+i, "", node.mLocation);
            nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            nodeMarker.setMarker(marker);
            roadNodes.add(nodeMarker);

            //nodeMarker.setDescription(node.mInstructions);
            //nodeMarker.setSubDescription(road.getLengthDurationText(node.mLength, node.mDuration));
            Drawable icon = getResources().getDrawable(R.drawable.marker);
            nodeMarker.setMarker(icon);
            android.util.Log.i(">>>TEST", node.mInstructions);
        }//end for

        mMap.getOverlays().add(new ItemizedIconOverlay<>(roadNodes,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        //Toast alert = Toast.makeText(OSMapsActivity.this, item.getTitle() + ": " + item.getSnippet(), Toast.LENGTH_SHORT);
                        //alert.show();
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, this));


        */
    }
}
