package pl.lodz.p.dmcs.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import org.osmdroid.events.MapEventsReceiver;
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
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OSMapsActivity extends AppCompatActivity implements MapEventsReceiver {
    private final static int REQUEST_WRITE_STORAGE = 1;
    public final static int ACTIVITY_NAVIGATE_REQUEST_CODE = 2;
    public final static int ACTIVITY_SEARCH_REQUEST_CODE = 3;
    private final static int REQUEST_LOCATION = 4;
    public final static int ACTIVITY_ADMIN_REQUEST_CODE = 5;
    public final static int ACTIVITY_LIST_REQUEST_CODE = 6;
    public final static int ACTIVITY_SHOW_ROOM_REQUEST_CODE = 7;
    private String token;
    private JSONArray buildings = null;
    private int level = 0;
    private int minLevel = 0;
    private int maxLevel = 0;
    private Map<Integer, List<FolderOverlay>> customLayers = new HashMap<>();
    //private ItemizedIconOverlay<OverlayItem> currentLocationOverlay = null;
    private FolderOverlay currentLocationOverlay = new FolderOverlay();
    protected List<Overlay> navigationOverlays = new ArrayList<>();
    protected Map<Integer, Overlay> insideOverlays = new HashMap<>();
    private FolderOverlay gpsOverlay = new FolderOverlay();
    protected boolean centerOnMe = true;

    protected boolean isSpecialOverlayOpened = false;
    protected boolean isAdmin = false;

    private LocationManager locationManager;
    private LocationListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                for (Overlay i : gpsOverlay.getItems()) {
                    gpsOverlay.remove(i);
                }
                MapView map = (MapView) findViewById(R.id.map);
                GeoPoint p = new GeoPoint(location.getLatitude(), location.getLongitude());
                Polygon circle = new Polygon(OSMapsActivity.this);
                double size = map != null ? 20.0 / Math.pow(2, Math.max(0, map.getZoomLevel() - 16)) : 20.0;
                circle.setPoints(Polygon.pointsAsCircle(p, size));
                circle.setFillColor(0xFF0000FF);
                circle.setStrokeColor(Color.BLUE);
                circle.setStrokeWidth(1);
                circle.setInfoWindow(null);
                gpsOverlay.add(circle);
                if (map != null)
                {
                    map.invalidate();
                    IMapController mapController = map.getController();
                    if (centerOnMe)
                    {
                        mapController.setCenter(p);
                        centerOnMe = true;
                        ImageButton centerBtn = (ImageButton) findViewById(R.id.centerOnMe);
                        if (centerBtn != null) centerBtn.setBackgroundResource(R.drawable.button_round_selected);
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(OSMapsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED){
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
        /*
        ImageButton btnNavigate = (ImageButton) findViewById(R.id.btnNavigate);
        if (btnNavigate != null) btnNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OSMapsActivity.this, NavigateActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, ACTIVITY_NAVIGATE_REQUEST_CODE);
            }
        });
        */

        int permissionCheckLoc1 = ContextCompat.checkSelfPermission(OSMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheckLoc2 = ContextCompat.checkSelfPermission(OSMapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheckLoc1 != PackageManager.PERMISSION_GRANTED && permissionCheckLoc2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OSMapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        else initLocation();
    }

    protected void addBuildings()
    {
        final ImageButton btnLevelUp = (ImageButton) findViewById(R.id.btnLevelUp);
        final ImageButton btnLevelDown = (ImageButton) findViewById(R.id.btnLevelDown);
        final Button levelText = (Button) findViewById(R.id.level);
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
                centerOnMe = false;
                ImageButton centerBtn = (ImageButton) findViewById(R.id.centerOnMe);
                centerBtn.setBackgroundResource(R.drawable.button_round);
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
                    task.setOverlayContainer(insideOverlays);
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
        mMap.getOverlays().add(gpsOverlay);
        /*try {
            //addRouteOverlay(new GeoPoint(19.45376900, 51.75269400), new GeoPoint(19.45596900, 51.74705900));
            GetDirectionsTask gdt = new GetDirectionsTask();
            gdt.setMap(mMap);
            gdt.execute(GetDirectionsTask.getDirectionsUrl(new GeoPoint(19.45376900, 51.75269400), new GeoPoint(19.45596900, 51.74705900)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }*/
        /*
        ImageButton btnNavCancel = (ImageButton) findViewById(R.id.btnNavigateCancel);
        if (btnNavCancel != null) btnNavCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<Integer> layerKeys = customLayers.keySet();
                for (int i = 0; i < navigationOverlays.size(); i++)
                {
                    if (navigationOverlays.get(i) instanceof FolderOverlay) {
                        for (int layer : layerKeys) {
                            List<FolderOverlay> layerData = customLayers.get(layer);
                            if (layerData.contains(navigationOverlays.get(i))) layerData.remove(navigationOverlays.get(i));
                        }
                        if (currentLocationOverlay.getItems().contains(navigationOverlays.get(i))) currentLocationOverlay.getItems().remove(navigationOverlays.get(i));
                    }
                    mMap.getOverlays().remove(navigationOverlays.get(i));
                }
                mMap.invalidate();
                v.setVisibility(View.GONE);
                ImageButton btnNav = (ImageButton) findViewById(R.id.btnNavigate);
                if (btnNav != null) btnNav.setVisibility(View.VISIBLE);
                ImageButton btnSearch = (ImageButton) findViewById(R.id.btnSearch);
                if (btnSearch != null) btnSearch.setVisibility(View.VISIBLE);
            }
        });
        */
        /*
        ImageButton btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        if (btnSearch != null) btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OSMapsActivity.this, SearchRoomActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, ACTIVITY_SEARCH_REQUEST_CODE);
            }
        });
        */

        //Sprawdzam, czy jestem Adminem
        JSONObject data = new JSONObject();
        try {
            data.put("action", "getAdmins");
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
                    if (obj.getJSONArray("data").length() > 0) {
                        isAdmin = true;
                        invalidateOptionsMenu();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
        //Ukrywanie wszystkich CustomInfoWindow po kliknieciu na mape
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        mMap.getOverlays().add(0, mapEventsOverlay);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACTIVITY_NAVIGATE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                final MapView mMap = (MapView) findViewById(R.id.map);
                if (mMap == null) return;
                GeoPoint startPoint = new GeoPoint(data.getDoubleExtra("startLat", 0.0), data.getDoubleExtra("startLng", 0.0));
                GeoPoint endPoint = new GeoPoint(data.getDoubleExtra("endLat", 0.0), data.getDoubleExtra("endLng", 0.0));
                Integer startRoom = data.hasExtra("startRoom") ? data.getIntExtra("startRoom", 0) : null;
                Integer endRoom = data.hasExtra("endRoom") ? data.getIntExtra("endRoom", 0) : null;
                Integer startLevel = data.hasExtra("startLevel") ? data.getIntExtra("startLevel", 0) : null;
                Integer endLevel = data.hasExtra("endLevel") ? data.getIntExtra("endLevel", 0) : null;
                GetDirectionsTask gdt = new GetDirectionsTask();
                gdt.setMap(mMap);
                gdt.setOverlayContainer(navigationOverlays);
                if (isOnline()) gdt.execute(GetDirectionsTask.getDirectionsUrl(startPoint, endPoint));
                if (startRoom != null && startLevel != null && insideOverlays.containsKey(startRoom))
                {
                    Polygon o = (Polygon) insideOverlays.get(startRoom);
                    Road road = new Road((ArrayList<GeoPoint>) o.getPoints());
                    // then, build an overlay with the route shape:
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);//, mMap.getContext());
                    roadOverlay.setColor(Color.GREEN);
                    FolderOverlay fo = new FolderOverlay();
                    fo.add(roadOverlay);
                    if (level != startLevel || mMap.getZoomLevel() < 19) fo.setEnabled(false);

                    //Add Route Overlays into map
                    customLayers.get(startLevel).add(fo);
                    mMap.getOverlays().add(fo);
                    navigationOverlays.add(fo);

                    mMap.invalidate();
                }
                if (endRoom != null && endLevel != null && insideOverlays.containsKey(endRoom))
                {
                    Polygon o = (Polygon) insideOverlays.get(endRoom);
                    Road road = new Road((ArrayList<GeoPoint>) o.getPoints());
                    // then, build an overlay with the route shape:
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);//, mMap.getContext());
                    roadOverlay.setColor(Color.RED);
                    FolderOverlay fo = new FolderOverlay();
                    fo.add(roadOverlay);
                    if (level != endLevel || mMap.getZoomLevel() < 19) fo.setEnabled(false);

                    //Add Route Overlays into map
                    customLayers.get(endLevel).add(fo);
                    mMap.getOverlays().add(fo);
                    navigationOverlays.add(fo);

                    mMap.invalidate();
                }
                isSpecialOverlayOpened = true;
                invalidateOptionsMenu();
                /*
                ImageButton btnNavCancel = (ImageButton) findViewById(R.id.btnNavigateCancel);
                if (btnNavCancel != null) btnNavCancel.setVisibility(View.VISIBLE);
                ImageButton btnNav = (ImageButton) findViewById(R.id.btnNavigate);
                if (btnNav != null) btnNav.setVisibility(View.GONE);
                ImageButton btnSearch = (ImageButton) findViewById(R.id.btnSearch);
                if (btnSearch != null) btnSearch.setVisibility(View.GONE);
                */
            }
            //if (resultCode == Activity.RESULT_CANCELED) {
            //Write your code if there's no result
            //}
        }
        if (requestCode == ACTIVITY_SEARCH_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                final MapView mMap = (MapView) findViewById(R.id.map);
                if (mMap == null) return;
                Integer searchID = data.hasExtra("searchID") ? data.getIntExtra("searchID", 0) : null;
                Integer searchLevel = data.hasExtra("searchLevel") ? data.getIntExtra("searchLevel", 0) : null;
                Double searchLat = data.hasExtra("searchLat") ? data.getDoubleExtra("searchLat", 0.0) : null;
                Double searchLng = data.hasExtra("searchLng") ? data.getDoubleExtra("searchLng", 0.0) : null;

                if (searchLat != null && searchLng != null)
                {
                    GeoPoint p = new GeoPoint(searchLat, searchLng);
                    Polygon circle = new Polygon(this);
                    circle.setPoints(Polygon.pointsAsCircle(p, 40.0));
                    circle.setFillColor(0x40404040);
                    circle.setStrokeColor(Color.GREEN);
                    circle.setStrokeWidth(1);
                    circle.setInfoWindow(null);

                    FolderOverlay fo = new FolderOverlay();
                    fo.add(circle);
                    if (mMap.getZoomLevel() >= 19) fo.setEnabled(false);

                    currentLocationOverlay.add(fo);
                    mMap.getOverlays().add(fo);
                    navigationOverlays.add(fo);

                    mMap.invalidate();
                }
                if (searchID != null && searchLevel != null && insideOverlays.containsKey(searchID))
                {
                    Polygon o = (Polygon) insideOverlays.get(searchID);
                    Road road = new Road((ArrayList<GeoPoint>) o.getPoints());
                    // then, build an overlay with the route shape:
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);//, mMap.getContext());
                    roadOverlay.setColor(Color.GREEN);
                    FolderOverlay fo = new FolderOverlay();
                    fo.add(roadOverlay);
                    if (level != searchLevel || mMap.getZoomLevel() < 19) fo.setEnabled(false);

                    //Add Route Overlays into map
                    customLayers.get(searchLevel).add(fo);
                    mMap.getOverlays().add(fo);
                    navigationOverlays.add(fo);

                    mMap.invalidate();
                }
                isSpecialOverlayOpened = true;
                invalidateOptionsMenu();
                /*
                ImageButton btnNavCancel = (ImageButton) findViewById(R.id.btnNavigateCancel);
                if (btnNavCancel != null) btnNavCancel.setVisibility(View.VISIBLE);
                ImageButton btnNav = (ImageButton) findViewById(R.id.btnNavigate);
                if (btnNav != null) btnNav.setVisibility(View.GONE);
                ImageButton btnSearch = (ImageButton) findViewById(R.id.btnSearch);
                if (btnSearch != null) btnSearch.setVisibility(View.GONE);
                */
            }
            //if (resultCode == Activity.RESULT_CANCELED) {
            //Write your code if there's no result
            //}
        }
        if (requestCode == ACTIVITY_ADMIN_REQUEST_CODE || requestCode == ACTIVITY_LIST_REQUEST_CODE || requestCode == ACTIVITY_SHOW_ROOM_REQUEST_CODE){
            if (listener != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.removeUpdates(listener);
                }
            }
            token = "";
            buildings = null;
            level = 0;
            minLevel = 0;
            maxLevel = 0;
            customLayers = new HashMap<>();
            //private ItemizedIconOverlay<OverlayItem> currentLocationOverlay = null;
            currentLocationOverlay = new FolderOverlay();
            navigationOverlays = new ArrayList<>();
            insideOverlays = new HashMap<>();
            gpsOverlay = new FolderOverlay();

            isSpecialOverlayOpened = false;
            isAdmin = false;
            initActivity();
            final MapView mMap = (MapView) findViewById(R.id.map);
            if (mMap != null)
            {
                mMap.getOverlays().clear();
                mMap.invalidate();
            }
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
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    initLocation();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        final MapView mMap = (MapView) findViewById(R.id.map);
        if (mMap != null) InfoWindow.closeAllInfoWindowsOn(mMap);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.osmaps_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (isSpecialOverlayOpened)
        {
            menu.findItem(R.id.menuItemCancel).setVisible(true);
            menu.findItem(R.id.menuItemSearch).setVisible(false);
            menu.findItem(R.id.menuItemNavigate).setVisible(false);
        }
        else
        {
            menu.findItem(R.id.menuItemCancel).setVisible(false);
            menu.findItem(R.id.menuItemSearch).setVisible(true);
            menu.findItem(R.id.menuItemNavigate).setVisible(true);
        }
        if (isAdmin)
        {
            menu.findItem(R.id.menuItemAdmin).setVisible(true);
        }
        else
        {
            menu.findItem(R.id.menuItemAdmin).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.menuItemAddBuilding:
                intent = new Intent(OSMapsActivity.this, AddBuildingActivity.class);
                intent.putExtra("token", token);
                startActivity(intent);
                return true;
            case R.id.menuItemAdmin:
                intent = new Intent(OSMapsActivity.this, AdminActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, ACTIVITY_ADMIN_REQUEST_CODE);
                return true;
            case R.id.menuItemList:
                intent = new Intent(OSMapsActivity.this, ListActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, ACTIVITY_LIST_REQUEST_CODE);
                return true;
            case R.id.menuItemLogout:
                setResult(RESULT_OK);
                finish();
                return true;
            case R.id.menuItemCancel:
                final MapView mMap = (MapView) findViewById(R.id.map);
                if (mMap != null) {
                    Set<Integer> layerKeys = customLayers.keySet();
                    for (int i = 0; i < navigationOverlays.size(); i++) {
                        if (navigationOverlays.get(i) instanceof FolderOverlay) {
                            for (int layer : layerKeys) {
                                List<FolderOverlay> layerData = customLayers.get(layer);
                                if (layerData.contains(navigationOverlays.get(i)))
                                    layerData.remove(navigationOverlays.get(i));
                            }
                            if (currentLocationOverlay.getItems().contains(navigationOverlays.get(i)))
                                currentLocationOverlay.getItems().remove(navigationOverlays.get(i));
                        }
                        mMap.getOverlays().remove(navigationOverlays.get(i));
                    }
                    mMap.invalidate();
                    isSpecialOverlayOpened = false;
                    invalidateOptionsMenu();
                }
                return true;
            case R.id.menuItemSearch:
                intent = new Intent(OSMapsActivity.this, SearchRoomActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, ACTIVITY_SEARCH_REQUEST_CODE);
                return true;
            case R.id.menuItemNavigate:
                intent = new Intent(OSMapsActivity.this, NavigateActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, ACTIVITY_NAVIGATE_REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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

    protected void initLocation() {
        int permissionCheckLoc1 = ContextCompat.checkSelfPermission(OSMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheckLoc2 = ContextCompat.checkSelfPermission(OSMapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheckLoc1 == PackageManager.PERMISSION_GRANTED || permissionCheckLoc2 == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, listener);
            centerOnMe = false;
            final ImageButton centerBtn = (ImageButton) findViewById(R.id.centerOnMe);
            centerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (centerOnMe)
                    {
                        centerBtn.setBackgroundResource(R.drawable.button_round);
                        centerOnMe = false;;
                    }
                    else
                    {
                        centerBtn.setBackgroundResource(R.drawable.button_round_selected);
                        centerOnMe = true;
                    }
                }
            });
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (listener != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(listener);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        initLocation();
    }
}
