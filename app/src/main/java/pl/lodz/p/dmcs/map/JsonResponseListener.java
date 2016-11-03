package pl.lodz.p.dmcs.map;

import org.json.JSONObject;

/**
 * Created by Krystian on 2016-11-03.
 */

public interface JsonResponseListener {
    void onResponse(JSONObject obj);
}
