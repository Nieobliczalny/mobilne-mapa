package pl.lodz.p.dmcs.map;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
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
    private Button sortTypeButton = null;
    private EditText editText = null;
    private MyArrayAdapter myArrayAdapter;
    private final ArrayList<Integer> userIDs = new ArrayList<>();
    private int current_rating = 0;
    private JSONArray commentList;
    private String sortType = "BEST";
    private Button otherType = null;
    private  String temp = null;
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
/*
        otherType = (Button) this.findViewById(R.id.btnOtherType);
        if (type.equalsIgnoreCase("building")) otherType.setVisibility(View.GONE);
        else otherType.setVisibility(View.VISIBLE);
        otherType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });*/
        nameText = (TextView) this.findViewById(R.id.textView5);
        ratingBar = (RatingBar) this.findViewById(R.id.ratingBar);
        listView = (ListView) this.findViewById(R.id.listView);
        submitButton = (Button) this.findViewById(R.id.button6);
        editText = (EditText) this.findViewById(R.id.editText4);
        sortTypeButton = (Button) this.findViewById(R.id.sort_type);
        //sortTypeButton.setText(sortType);

        sortTypeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    changeSort();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

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
                    ratingBar.setIsIndicator(true);
                    ratingBar.setRating(current_rating);
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

    public void getComments()
    {
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
                    commentList = building.getJSONArray("comments");
                    commentList = sort(commentList);
                    ((MyArrayAdapter) listView.getAdapter()).setCommentList(commentList);
                    ((MyArrayAdapter) listView.getAdapter()).notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }

    public void sendVote(int commentId)
    {
        JSONObject data = new JSONObject();
        try {
            data.put("action", "likeComment");
            data.put("id", commentId);
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }
        SendPostTask task = new SendPostTask();
        task.setActivity(ShowRoomActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                    getComments();
            }
        });
        task.execute(data);
    }
    public void changeSort() throws JSONException {
        if(sortType.equals("BEST")) {
            sortType = "NEW";
            sortTypeButton.setText("Najnowsze");
        }
        else {
            sortType = "BEST";
            sortTypeButton.setText("Najlepsze");
        }

        commentList = sort(commentList);
        ((MyArrayAdapter) listView.getAdapter()).setCommentList(commentList);
        ((MyArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
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
                        //list.add(comment.getString("text") + "\n" + "By --" +comment.getString("author_nick"));
                        userIDs.add(comment.getInt("author"));
                        current_rating = comment.getInt("total_ranking");
                        ratingBar.setRating((float)current_rating);///////////////////////
                        getComments();
                        //((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
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
        temp = building.getString("name");
        if (type.equalsIgnoreCase("building")) {
            String unofficial_name = building.getString("unofficial_name").replace(" ; ", ", ");
            String number = building.getString("number");
            if (!unofficial_name.equals("") || !number.equals("")) {
                temp += " ( ";
                if (!unofficial_name.equals("")) {
                    temp += unofficial_name;
                }
                if (!number.equals("")) {
                    if (!unofficial_name.equals("")) temp += ", ";
                    temp += number;
                }
                temp += " )";
            }
        }
        android.util.Log.i("-----------","PARAPET : " + object.toString());
        final int rating = object.getInt("rating");
        commentList = object.getJSONArray("comments");
        commentList = sort(commentList);
        for(int i = 0; i < commentList.length(); i++){
            JSONObject comment = (JSONObject) commentList.get(i);
            userIDs.add(comment.getInt("author"));
        }

        myArrayAdapter = new MyArrayAdapter(this, commentList);
        //mAdapter = new ArrayAdapter<>(this, R.layout.comment_list_item, list);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    nameText.setText(temp);
                    ratingBar.setRating(rating);
                    listView.setAdapter(myArrayAdapter);
            }
        });
    }

    public JSONArray sort(JSONArray list) throws JSONException {
        if(sortType.equals("BEST"))
            return sortByUsefulness(list);
        else
            return sortByDate(list);
    }

    public JSONArray sortByDate(JSONArray list) throws JSONException {
        JSONArray sortedJsonArray = new JSONArray();

        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < list.length(); i++) {
            jsonValues.add(list.getJSONObject(i));
        }
        Collections.sort( jsonValues, new Comparator<JSONObject>() {

            private static final String KEY_NAME = "date";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(KEY_NAME);
                    valB = (String) b.get(KEY_NAME);
                }
                catch (JSONException e) {
                    //do something
                }

                try {
                    return (formatDate(valA).after(formatDate(valB))) ? -1 : 0;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //if you want to change the sort order, simply use the following:
                //return -valA.compareTo(valB);
                return 0;
            }
        });

        for (int i = 0; i < list.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }
        return sortedJsonArray;
    }

    public Date formatDate(String dateBefore) throws ParseException {
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return originalFormat.parse(dateBefore);
    }
    public JSONArray sortByUsefulness(JSONArray list) throws JSONException {
        JSONArray sortedJsonArray = new JSONArray();

        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < list.length(); i++) {
            jsonValues.add(list.getJSONObject(i));
        }
        Collections.sort( jsonValues, new Comparator<JSONObject>() {

            private static final String KEY_NAME = "usefulness";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(KEY_NAME);
                    valB = (String) b.get(KEY_NAME);
                }
                catch (JSONException e) {
                    //do something
                }

                return -valA.compareTo(valB);
                //if you want to change the sort order, simply use the following:
                //return -valA.compareTo(valB);
            }
        });

        for (int i = 0; i < list.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }
        return sortedJsonArray;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.showroom_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (type.equalsIgnoreCase("building"))
        {
            menu.findItem(R.id.menuAddUName).setVisible(true);
            menu.findItem(R.id.menuChangeNumber).setVisible(true);
            menu.findItem(R.id.menuRoomType).setVisible(false);
        }
        else
        {
            menu.findItem(R.id.menuAddUName).setVisible(false);
            menu.findItem(R.id.menuChangeNumber).setVisible(false);
            menu.findItem(R.id.menuRoomType).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.menuAddUName:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Zaproponuj dodatkową nazwę budynku");

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString().trim();
                        if (m_Text.equals("")) return;
                        String txt = "";
                        try {
                            txt = building.getString("unofficial_name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String[] names = txt.split(" ; ");
                        boolean nameExists = false;
                        for (int i = 0; i < names.length; i++)
                        {
                            if (m_Text.equalsIgnoreCase(names[i].trim())) nameExists = true;
                        }
                        if (!nameExists) {
                            JSONObject data = new JSONObject();
                            try {
                                data.put("action", "proposeNewBuildingUName");
                                data.put("id", id);
                                data.put("name", m_Text);
                                data.put("token", token);
                                android.util.Log.i("XXXD", data.toString());
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            SendPostTask task = new SendPostTask();
                            task.setActivity(ShowRoomActivity.this);
                            task.setResponseListener(new JsonResponseListener() {
                                @Override
                                public void onResponse(final JSONObject obj) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast t = Toast.makeText(ShowRoomActivity.this, "Dane o nowej nazwie wysłane. Po rozpatrzeniu propozycji przez Administratora otrzymasz e-mail o akceptacji/odrzuceniu.", Toast.LENGTH_SHORT);
                                            t.show();
                                        }
                                    });
                                }
                            });
                            task.execute(data);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast t = Toast.makeText(ShowRoomActivity.this, "Taka informacja już jest, nie ma potrzeby dodawać ponownie.", Toast.LENGTH_SHORT);
                                    t.show();
                                }
                            });
                        }
                    }
                });
                builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.create().show();
                return true;
            case R.id.menuChangeNumber:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("Zaproponuj inny symbol budynku");

                final EditText input2 = new EditText(this);
                input2.setInputType(InputType.TYPE_CLASS_TEXT);
                try {
                    input2.setText(building.getString("number"));
                } catch (JSONException e) {
                    //e.printStackTrace();
                }
                builder2.setView(input2);

                builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input2.getText().toString().trim();
                        if (m_Text.equals("")) return;
                        String txt = "";
                        try {
                            txt = building.getString("number");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (!m_Text.equalsIgnoreCase(txt)) {
                            JSONObject data = new JSONObject();
                            try {
                                data.put("action", "proposeNewBuildingSymbol");
                                data.put("id", id);
                                data.put("name", m_Text);
                                data.put("token", token);
                                android.util.Log.i("XXXD", data.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            SendPostTask task = new SendPostTask();
                            task.setActivity(ShowRoomActivity.this);
                            task.setResponseListener(new JsonResponseListener() {
                                @Override
                                public void onResponse(final JSONObject obj) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast t = Toast.makeText(ShowRoomActivity.this, "Dane o nowym symbolu wysłane. Po rozpatrzeniu propozycji przez Administratora otrzymasz e-mail o akceptacji/odrzuceniu.", Toast.LENGTH_SHORT);
                                            t.show();
                                        }
                                    });
                                }
                            });
                            task.execute(data);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast t = Toast.makeText(ShowRoomActivity.this, "Taka informacja już jest, nie ma potrzeby dodawać ponownie.", Toast.LENGTH_SHORT);
                                    t.show();
                                }
                            });
                        }
                    }
                });
                builder2.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder2.create().show();
                return true;
            case R.id.menuRoomType:
                ArrayList<String> items = new ArrayList<String>();
                items.add("Pomieszczenie");
                items.add("Sala wykładowa");
                items.add("Korytarz");
                items.add("Sala laboratoryjna");
                items.add("Toaleta");
                items.add("Schody / Winda");
                items.add("Zaplecze");
                items.add("Pomieszczenie specjalne");
                items.add("Biuro");
                items.add("Sala konferencyjna");
                final String[] opts = new String[items.size()];
                for (int i = 0; i < opts.length; i++) {
                    opts[i] = items.get(i);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(ShowRoomActivity.this);
                        builder.setTitle("Zaproponuj nowy typ sali")
                                .setItems(opts, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // The 'which' argument contains the index position
                                        // of the selected item
                                        int type = -1;
                                        try {
                                            type = building.getInt("type");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        if (which != type) {
                                            JSONObject data = new JSONObject();
                                            try {
                                                data.put("action", "proposeNewRoomType");
                                                data.put("id", id);
                                                data.put("type", which);
                                                data.put("token", token);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            SendPostTask task = new SendPostTask();
                                            task.setActivity(ShowRoomActivity.this);
                                            task.setResponseListener(new JsonResponseListener() {
                                                @Override
                                                public void onResponse(final JSONObject obj) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast t = Toast.makeText(ShowRoomActivity.this, "Dane o typie sali wysłane. Po rozpatrzeniu propozycji przez Administratora otrzymasz e-mail o akceptacji/odrzuceniu.", Toast.LENGTH_SHORT);
                                                            t.show();
                                                        }
                                                    });
                                                }
                                            });
                                            task.execute(data);
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast t = Toast.makeText(ShowRoomActivity.this, "Taka informacja już jest, nie ma potrzeby dodawać ponownie.", Toast.LENGTH_SHORT);
                                                    t.show();
                                                }
                                            });
                                        }
                                    }
                                });
                        AlertDialog d = builder.create();
                        d.show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
