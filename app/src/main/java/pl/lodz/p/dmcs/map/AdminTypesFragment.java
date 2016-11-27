package pl.lodz.p.dmcs.map;

import android.content.Context;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AdminTypesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdminTypesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminTypesFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private JSONArray array = null;
    private View v = null;

    public AdminTypesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AdminTypesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminTypesFragment newInstance() {
        AdminTypesFragment fragment = new AdminTypesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
        //}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_types, container, false);
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
        void onTypeAccept(int reqID);
        void onTypeDecline(int reqID);
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
                textView.setText(values.get(position).getString("name") + " z " + values.get(position).getString("building_name"));
                TextView textView2 = (TextView) rowView.findViewById(R.id.secondLine);
                textView2.setText("Dodał: " + values.get(position).getString("nick") + " (ID: " + values.get(position).getInt("user") + ")");
                TextView textView3 = (TextView) rowView.findViewById(R.id.thirdLine);
                textView3.setText("Stary typ: " + values.get(position).getString("oldTypeDesc") + "\r\nZaproponowany: " + values.get(position).getString("newTypeDesc"));
                Button btnAccept = (Button) rowView.findViewById(R.id.btnAccept);
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            try {
                                mListener.onTypeAccept(values.get(position).getInt("id"));
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
                                mListener.onTypeDecline(values.get(position).getInt("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                Button btnCheck = (Button) rowView.findViewById(R.id.btnCheck);
                btnCheck.setVisibility(View.GONE);
            } catch (JSONException e) {
                //
                e.printStackTrace();
            }
            return rowView;
        }
    }
}
