package pl.lodz.p.dmcs.map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AdminBuildingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdminBuildingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminBuildingsFragment extends Fragment {

    private JSONArray array = null;
    private View v = null;

    private OnFragmentInteractionListener mListener;

    public AdminBuildingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AdminBuildingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminBuildingsFragment newInstance() {
        AdminBuildingsFragment fragment = new AdminBuildingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (getArguments() != null) {
            //token = getArguments().getString(ARG_TOKEN);
        //}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_buildings, container, false);
        this.v = view;
        if (this.array != null)
        {
            setData(this.array, view);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onBuildingAccept(int buildingID);
        void onBuildingDecline(int buildingID);
    }

    public void setData(JSONArray array) {
        View v = getView();
        if (v == null) v = this.v;
        this.array = array;
        setData(array, v);
    }

    public void setData(JSONArray array, View view) {
        try {
            ListView listView = (ListView) view.findViewById(R.id.listview);
            ArrayList<JSONObject> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++)
            {
                list.add(array.getJSONObject(i));
            }
            MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this.getActivity(), list);
            listView.setAdapter(adapter);

            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            //
        }
    }

    public class MySimpleArrayAdapter extends ArrayAdapter<JSONObject> {
        private final Context context;
        private final ArrayList<JSONObject> values;

        public MySimpleArrayAdapter(Context context, ArrayList<JSONObject> values) {
            super(context, R.layout.admin_list_item, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.admin_list_item, parent, false);
            try {
                TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
                textView.setText(values.get(position).getString("name"));
                TextView textView2 = (TextView) rowView.findViewById(R.id.secondLine);
                textView2.setText("Dodał: " + values.get(position).getString("nick") + " (ID: " + values.get(position).getInt("user") + ")");
                TextView textView3 = (TextView) rowView.findViewById(R.id.thirdLine);
                StringBuffer sb = new StringBuffer();
                sb.append("Szerokość: " + values.get(position).getDouble("latitude") + "\r\nDługość: " + values.get(position).getDouble("longitude"));
                String un = values.get(position).getString("unofficial_name");
                if(!un.equals("")) sb.append("\r\nNazwy nieoficjalne: "+un.replace(" ; ",", "));
                String number = values.get(position).getString("number");
                if(!number.equals("")) sb.append("\r\nSymbol: "+number);
                String units = values.get(position).getString("building_units");
                if(!units.equals("")) sb.append("\r\nJednostki: "+units);
                textView3.setText(sb.toString());
                Button btnAccept = (Button) rowView.findViewById(R.id.btnAccept);
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            try {
                                mListener.onBuildingAccept(values.get(position).getInt("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                Button btnDecline = (Button) rowView.findViewById(R.id.btnDecline);
                btnDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            try {
                                mListener.onBuildingDecline(values.get(position).getInt("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                Button btnCheck = (Button) rowView.findViewById(R.id.btnCheck);
                btnCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(context, AdminMapsActivity.class);
                            String[] polygons = {values.get(position).getDouble("longitude") + "," + values.get(position).getDouble("latitude")};
                            intent.putExtra("polygons", polygons);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                //
                e.printStackTrace();
            }
            return rowView;
        }
    }
}
