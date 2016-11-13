package pl.lodz.p.dmcs.map;

import android.os.AsyncTask;
import android.view.MotionEvent;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;


/**
 * Created by Krystian on 2016-11-13.
 */

public class LoadKmlTask extends AsyncTask<String, Void, Boolean> {
    private MapView mMap = null;
    private List<FolderOverlay> list = null;
    private KmlDocument mKmlDocument = new KmlDocument();
    private boolean isOverlayEnabled = false;
    public void setMap(MapView m)
    {
        mMap = m;
    }
    public void setList(List<FolderOverlay> l)
    {
        list = l;
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
            list.add(mKmlOverlay);
            mMap.getOverlays().add(mKmlOverlay);
            mMap.invalidate();
        }
    }
}
