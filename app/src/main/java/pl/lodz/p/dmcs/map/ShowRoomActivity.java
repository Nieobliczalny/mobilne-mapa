package pl.lodz.p.dmcs.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.RunnableFuture;

public class ShowRoomActivity extends AppCompatActivity {
    private String token;
    private int id;
    private String type;
    private JSONObject building = null;
    private TextView nameText = null;
    private RatingBar ratingBar = null;
    private ListView listView = null;
    private Button submitButton = null;
    private EditText editText = null;
    private ArrayAdapter<String> mAdapter;
    private final ArrayList<String> list = new ArrayList<>();
    private final ArrayList<Integer> userIDs = new ArrayList<>();
    private int current_rating = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_room);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            token = extras.getString("token");
            id = extras.getInt("id");
            type = extras.getString("type");
        }

        nameText = (TextView) this.findViewById(R.id.textView5);
        ratingBar = (RatingBar) this.findViewById(R.id.ratingBar);
        listView = (ListView) this.findViewById(R.id.listView);
        submitButton = (Button) this.findViewById(R.id.button6);
        editText = (EditText) this.findViewById(R.id.editText4);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if(!((editText.getText().toString()).matches("")))
                {
                    ratingBar.setIsIndicator(false);
                }
                else {
                    ratingBar.setRating(current_rating);
                    ratingBar.setIsIndicator(true);
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ShowRoomActivity.this, ShowUserActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("id", userIDs.get(i));
                startActivity(intent);
            }

        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendComment();
            }
        });
        android.util.Log.i("TEST","PARAPET : " + id);

        JSONObject data = new JSONObject();
        try {
            if (type.equalsIgnoreCase("building")) data.put("action", "getBuildingById");
            else data.put("action", "getRoomById");
            data.put("id", id);
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }
        SendPostTask task = new SendPostTask();
        task.setActivity(ShowRoomActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {

                    building = obj.getJSONObject("data");
                    android.util.Log.i("TEST","PARAPET : " + building.getString("name"));
                    initiate(building);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }

    public void sendComment(){

        JSONObject data = new JSONObject();
        try {
            data.put("action", "postComment");
            data.put("id", id);
            data.put("token", token);
            data.put("type", type);
            data.put("comment", editText.getText());
            data.put("rating", Math.floor(ratingBar.getRating()));
            android.util.Log.i("DDD", data.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
        editText.setText("");
        SendPostTask task = new SendPostTask();
        task.setActivity(ShowRoomActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject comment) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        android.util.Log.i("TAG", comment.toString());
                        list.add(comment.getString("text") + "\n" + "By --" +comment.getString("author_nick"));
                        userIDs.add(comment.getInt("author"));
                        current_rating = comment.getInt("total_ranking");
                        ratingBar.setRating((float)current_rating);///////////////////////
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            }
        });
        task.execute(data);

    }


    public void initiate(JSONObject object) throws JSONException {
        final String name = object.getString("name");

        final int rating = object.getInt("rating");
        final JSONArray commentList = object.getJSONArray("comments");
        for(int i = 0; i < commentList.length(); i++){
            JSONObject comment = (JSONObject) commentList.get(i);
            list.add(comment.getString("text") + "\n" + "By --" +comment.getString("author_nick"));
            userIDs.add(comment.getInt("author"));
        }
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    nameText.setText(name);
                    ratingBar.setRating(rating);
                    listView.setAdapter(mAdapter);
            }
        });
    }
}
