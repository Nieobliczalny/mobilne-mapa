package pl.lodz.p.dmcs.map;

import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements AdminBuildingsFragment.OnFragmentInteractionListener, AdminFloorsFragment.OnFragmentInteractionListener, AdminTypesFragment.OnFragmentInteractionListener, AdminAccessFragment.OnFragmentInteractionListener {

    protected String token = "";
    protected AdminBuildingsFragment abf = null;
    protected AdminFloorsFragment aff = null;
    protected AdminTypesFragment atf = null;
    protected AdminAccessFragment aaf = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null)
        {
            savedInstanceState.remove ("android:support:fragments");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            token = extras.getString("token");
        }

        abf = AdminBuildingsFragment.newInstance();//token);
        aff = AdminFloorsFragment.newInstance();
        atf = AdminTypesFragment.newInstance();
        aaf = AdminAccessFragment.newInstance();

        //Pobranie danych do abf
        JSONObject data = new JSONObject();
        try {
            data.put("action", "getBuildingsToAccept");
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task = new SendPostTask();
        task.setActivity(AdminActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    abf.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);

        //Pobranie danych do aff
        JSONObject data2 = new JSONObject();
        try {
            data2.put("action", "getFloorsToAccept");
            data2.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task2 = new SendPostTask();
        task2.setActivity(AdminActivity.this);
        task2.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    aff.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task2.execute(data2);

        //Pobranie danych do atf
        JSONObject data3 = new JSONObject();
        try {
            data3.put("action", "getTypesToAccept");
            data3.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task3 = new SendPostTask();
        task3.setActivity(AdminActivity.this);
        task3.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    atf.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task3.execute(data3);

        //Pobranie danych do aaf
        JSONObject data4 = new JSONObject();
        try {
            data4.put("action", "getAdmins");
            data4.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task4 = new SendPostTask();
        task4.setActivity(AdminActivity.this);
        task4.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    aaf.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task4.execute(data4);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(abf, "Punkty");
        adapter.addFragment(aff, "Plany pięter");
        adapter.addFragment(atf, "Typy sal");
        adapter.addFragment(aaf, "Dostęp Admina");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBuildingAccept(int buildingID) {
        JSONObject data = new JSONObject();
        try {
            data.put("action", "acceptBuilding");
            data.put("buildingID", buildingID);
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task = new SendPostTask();
        task.setActivity(AdminActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    abf.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }

    @Override
    public void onBuildingDecline(int buildingID) {
        JSONObject data = new JSONObject();
        try {
            data.put("action", "declineBuilding");
            data.put("buildingID", buildingID);
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task = new SendPostTask();
        task.setActivity(AdminActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    abf.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }

    @Override
    public void onFloorAccept(int floorID) {
        JSONObject data = new JSONObject();
        try {
            data.put("action", "acceptFloor");
            data.put("floorID", floorID);
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task = new SendPostTask();
        task.setActivity(AdminActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    aff.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }

    @Override
    public void onFloorDecline(int floorID) {
        JSONObject data = new JSONObject();
        try {
            data.put("action", "declineFloor");
            data.put("floorID", floorID);
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task = new SendPostTask();
        task.setActivity(AdminActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    aff.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }

    @Override
    public void onTypeAccept(int reqID) {
        JSONObject data = new JSONObject();
        try {
            data.put("action", "acceptType");
            data.put("reqID", reqID);
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task = new SendPostTask();
        task.setActivity(AdminActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    atf.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }

    @Override
    public void onTypeDecline(int reqID) {
        JSONObject data = new JSONObject();
        try {
            data.put("action", "declineType");
            data.put("reqID", reqID);
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task = new SendPostTask();
        task.setActivity(AdminActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    atf.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }

    @Override
    public void onAdminAccessGrant(String userNick) {
        JSONObject data = new JSONObject();
        try {
            data.put("action", "grantAdmin");
            data.put("userNick", userNick);
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task = new SendPostTask();
        task.setActivity(AdminActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    aaf.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }

    @Override
    public void onAdminAccessRevoke(int userID) {
        JSONObject data = new JSONObject();
        try {
            data.put("action", "revokeAdmin");
            data.put("userID", userID);
            data.put("token", token);
        } catch (Exception e){
            e.printStackTrace();
        }

        SendPostTask task = new SendPostTask();
        task.setActivity(AdminActivity.this);
        task.setResponseListener(new JsonResponseListener() {
            @Override
            public void onResponse(final JSONObject obj) {
                try {
                    aaf.setData(obj.getJSONArray("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(data);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
