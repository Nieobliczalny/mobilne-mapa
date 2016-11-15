package pl.lodz.p.dmcs.map;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.MotionEvent;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Krystian on 2016-11-13.
 */

public class LoadKmlTask extends AsyncTask<String, Void, Boolean> {
    private MapView mMap = null;
    private List<FolderOverlay> list = null;
    private KmlDocument mKmlDocument = new KmlDocument();
    private boolean isOverlayEnabled = false;
    private Activity activity = null;
    private String token = "";
    private Map<Integer, Overlay> om = null;
    public void setActivity(Activity a)
    {
        activity = a;
    }
    public void setMap(MapView m)
    {
        mMap = m;
    }
    public void setToken(String t)
    {
        token = t;
    }
    public void setList(List<FolderOverlay> l)
    {
        list = l;
    }
    public void setOverlayContainer(Map<Integer, Overlay> l)
    {
        om = l;
    }
    public void setOverlayEnabled(boolean e)
    {
        isOverlayEnabled = e;
    }
    @Override
    protected Boolean doInBackground(String... url) {
        boolean ok = false;
        ok = mKmlDocument.parseKMLUrl(url[0]);
        return ok;
    }

    @Override
    protected void onPostExecute(Boolean ok) {
        //super.onPostExecute(ok);
        if (mMap != null) {
            FolderOverlay mKmlOverlay = (FolderOverlay) mKmlDocument.mKmlRoot.buildOverlay(mMap, null, null, mKmlDocument);
            mKmlOverlay.setEnabled(isOverlayEnabled);
            /*
            for (int i = 0; i < mKmlOverlay.getItems().size(); i++)
            {
                ((Polygon)mKmlOverlay.getItems().get(i)).getInfoWindow().
            }*/
            List<Overlay> items1 = mKmlOverlay.getItems();
            HashMap<String, Integer> ids = new HashMap<>();
            ArrayList<KmlFeature> placemarks = ((KmlFolder)mKmlDocument.mKmlRoot.mItems.get(0)).mItems;
            for (KmlFeature placemark : placemarks)
            {
                int id = Integer.parseInt(placemark.mId.split("\\.")[1]);
                if (id < 0) ids.put(placemark.mName, -id);
            }
            for (int i = 0; i < items1.size(); i++)
            {
                if (items1.get(i) instanceof FolderOverlay) {
                    FolderOverlay fo = (FolderOverlay) items1.get(i);
                    List<Overlay> items = fo.getItems();
                    for (int j = 0; j < items.size(); j++) {
                        if (items.get(j) instanceof Polygon) {
                            Polygon p = (Polygon) items.get(j);
                            int id = ids.containsKey(p.getTitle()) ? ids.get(p.getTitle()) : 0;
                            String[] desc = p.getSnippet().split("\\|");
                            if (desc.length > 1)
                            {
                                p.setSnippet(desc[1]);
                                p.setSubDescription(desc[0]);
                            }
                            p.setInfoWindow(new CustomInfoWindow(R.layout.bonuspack_bubble, mMap, id, "room", token, activity));
                            if (id > 0) om.put(id, p);
                            android.util.Log.i("TEST", p.getTitle() + " / " + p.getSubDescription() + " / " + p.getSnippet() + " / " + desc.length);
                        }
                    }
                }
            }
            //for (int i = 0; i < mKmlDocument.mKmlRoot.mItems.size(); i++) android.util.Log.i("TRRR", "" + mKmlDocument.mKmlRoot.mItems.get(i).mId + " android " + ((KmlFolder)mKmlDocument.mKmlRoot.mItems.get(i)).mItems.get(0).mId);
            //android.util.Log.i("TESTING" +  ((FolderOverlay)mKmlOverlay.getItems().get(0)).getItems().size(), ((FolderOverlay)mKmlOverlay.getItems().get(0)).getItems().get(0).getClass().toString());
            list.add(mKmlOverlay);
            mMap.getOverlays().add(mKmlOverlay);
            mMap.invalidate();
        }
    }
}
