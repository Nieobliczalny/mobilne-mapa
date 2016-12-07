package pl.lodz.p.dmcs.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AdminAccessFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdminAccessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminAccessFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private JSONArray array = null;
    protected View v = null;
    private AutoCompleteTextView et = null;
    private ArrayAdapter<String> hintsUsers = null;

    public AdminAccessFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AdminAccessFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminAccessFragment newInstance() {
        AdminAccessFragment fragment = new AdminAccessFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (getArguments() != null) {
        //    mParam1 = getArguments().getString(ARG_PARAM1);
        //}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_admin_access, container, false);
        et = (AutoCompleteTextView) view.findViewById(R.id.userNick);
        hintsUsers = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line);


        this.v = view;
        if (this.array != null)
        {
            setData(this.array, view);
        }
        Button btnGrant = (Button) view.findViewById(R.id.btnMakeAdmin);
        if (btnGrant != null)
        {
            btnGrant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                    {

                        if (et != null) {
                            mListener.onAdminAccessGrant(et.getText().toString());
                        }
                    }
                }
            });
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
        void onAdminAccessGrant(String userNick);
        void onAdminAccessRevoke(int userID);
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
            final ArrayList<JSONObject> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++)
            {
                list.add(array.getJSONObject(i));
            }
            MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(getActivity(), list);
            listView.setAdapter(adapter);

            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            //
        }
    }


    public void setHints(JSONArray array) {
        try {
            if(array == null) return;
            Log.d(array.toString(),array.toString());

            for (int i = 0; i < array.length(); i++){
                JSONObject user = array.getJSONObject(i);
                Log.d(user.toString(),user.toString());
                String nick = user.getString("nick");
                hintsUsers.add(nick);
            }
            et.setAdapter(hintsUsers);


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
                textView.setText(values.get(position).getString("nick"));
                TextView textView2 = (TextView) rowView.findViewById(R.id.secondLine);
                textView2.setVisibility(View.GONE);
                TextView textView3 = (TextView) rowView.findViewById(R.id.thirdLine);
                textView3.setVisibility(View.GONE);
                Button btnAccept = (Button) rowView.findViewById(R.id.btnAccept);
                btnAccept.setVisibility(View.GONE);
                Button btnDecline = (Button) rowView.findViewById(R.id.btnDecline);
                btnDecline.setText("Usuń uprawnienia");
                btnDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            try {
                                mListener.onAdminAccessRevoke(values.get(position).getInt("id"));
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
