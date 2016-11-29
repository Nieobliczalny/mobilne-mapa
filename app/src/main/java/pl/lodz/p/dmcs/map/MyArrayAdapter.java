package pl.lodz.p.dmcs.map;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by AveN on 2016-11-29.
 */

public class MyArrayAdapter extends BaseAdapter {

    private Context context;
    JSONArray commentList;

    public void setCommentList(JSONArray commentList) {
        this.commentList = commentList;
    }

    public MyArrayAdapter(Context context, JSONArray commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @Override
    public int getCount() {
        return commentList.length();
    }

    @Override
    public Object getItem(int i) {
        try {
            return commentList.get(i);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = (View) inflater.inflate(
                    R.layout.comment_list_item, null);
        }
        android.util.Log.i("ADAPTER", "----------GET VIEW");
        TextView content = (TextView) convertView.findViewById(R.id.comment_list_content);
        TextView author = (TextView) convertView.findViewById(R.id.comment_list_author);
        Button button = (Button) convertView.findViewById(R.id.buttonLike);
        final JSONObject comment;

        try {
            comment = (JSONObject) commentList.get(position);
            content.setText(comment.getString("text"));
            android.util.Log.i("ADAPTER", comment.getString("text"));

            String newDate = formatDate(comment.getString("date").toString());
            String ratingText = comment.getDouble("rating") != 0 ? "  Opinia: " + comment.getString("rating") : "";
            author.setText("Przez: " + comment.getString("author_nick") + ratingText + " Data: " + newDate);
            button.setText(comment.getString("usefulness") + "\n" + "Like");
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        ((ShowRoomActivity) context).sendVote(Integer.valueOf(comment.getString("id")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    public String formatDate(String dateBefore) throws ParseException {
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = originalFormat.parse(dateBefore);
        SimpleDateFormat newFormat = new SimpleDateFormat("yyy-MM-dd HH:mm");
        return newFormat.format(date);
    }
}
