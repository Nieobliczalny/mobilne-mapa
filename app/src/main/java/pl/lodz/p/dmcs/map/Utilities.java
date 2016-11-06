package pl.lodz.p.dmcs.map;

import android.content.Context;
import android.graphics.Color;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.views.overlay.TilesOverlay;

/**
 * Created by Krystian on 2016-11-06.
 */

public class Utilities {
    public static TilesOverlay CreateTilesOverlay(Context ctx, final int level) {
        //Dodanie własnego TileProvidera
        final MapTileProviderBasic provider = new MapTileProviderBasic(ctx, new OnlineTileSourceBase("PL" + level, 19, 19, 256, "PNG", new String[0]) {
            @Override
            public String getTileURLString(MapTile aTile) {
                //BoundingBox bbox=tile2boundingBox(aTile.getX(), aTile.getY(), aTile.getZoomLevel());
                String baseUrl = "http://mobilne.kjozwiak.ovh/map.php?x=" + aTile.getX() + "&y=" + aTile.getY() + "&zoom=" + aTile.getZoomLevel() + "&level=" + level; //http://egeoint.nrlssc.navy.mil/arcgis/rest/services/usng/USNG_93/MapServer/export?dpi=96&transparent=true&format=png24&bbox="+bbox.west+","+bbox.south+","+bbox.east+","+bbox.north+"&size=256,256&f=image";
                return baseUrl;
            }
        });
        TilesOverlay layer = new TilesOverlay(provider, ctx);
        layer.setLoadingBackgroundColor(Color.TRANSPARENT);
        layer.setLoadingLineColor(Color.TRANSPARENT);
        layer.setEnabled(false);
        return layer;
    }
}
