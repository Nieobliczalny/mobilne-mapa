package pl.lodz.p.dmcs.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

/**
 * Created by Krystian on 2016-11-14.
 */

public class CustomInfoWindow extends BasicInfoWindow {

    public CustomInfoWindow(int layoutResId, MapView mapView, final int id, final String type, final String token, final Activity a) {
        super(layoutResId, mapView);

        this.mView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent e) {
                if(e.getAction() == 1) {
                    android.util.Log.i("TEST", ">" + id + " / " + type);
                    CustomInfoWindow.this.close();
                }

                return true;
            }
        });

        TextView title = (TextView) this.mView.findViewById(R.id.bubble_title);
        if (title != null) title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id > 0) {
                    Intent intent = new Intent(a, ShowRoomActivity.class);
                    intent.putExtra("token", token);
                    intent.putExtra("type", type);
                    intent.putExtra("id", id);
                    a.startActivity(intent);
                }
            }
        });
    }

    public void onOpen(Object item) {
        super.onOpen(item);
    }

    public void onClose() {
        super.onClose();
    }
}