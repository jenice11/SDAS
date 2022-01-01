package com.example.sdas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.sdas.Model.History;
import com.example.sdas.Utils.Common;
import com.example.sdas.Utils.VolleyController;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {
    //json API object url
    private String urlJsonObj = "https://corona.lmao.ninja/v3/covid-19/countries/malaysia";
    private static String TAG = MainActivity.class.getSimpleName();
    // Progress dialog
    private ProgressDialog pDialog;
    private TextView home_risk_high_count, home_risk_medium_count, home_risk_low_count, txtTodayCases, txtActive, txtCritical, txtRecovered;
    NavigationView navigationView;
    PieChart pieChart;
    TextView name, email, empid;
    ImageView navprofile;
    DatabaseReference user_history;
    List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);



        //text views
        home_risk_high_count = findViewById(R.id.home_risk_high_count);
        home_risk_medium_count = findViewById(R.id.home_risk_medium_count);
        home_risk_low_count = findViewById(R.id.home_risk_low_count);
        txtRecovered = findViewById(R.id.txt_recovered);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View navView = navigationView.inflateHeaderView(R.layout.header);
        pieChart = findViewById(R.id.pieChart_view);

        name = (TextView) navView.findViewById(R.id.name);
        email = (TextView) navView.findViewById(R.id.email);

        //progress dialog
        // pDialog = new ProgressDialog(this);
        // pDialog.setMessage("loading...");
        // pDialog.setCancelable(false);

        //parsing json data
        parseJSON();
        //adding swipe to refresh
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.statsrefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            parseJSON();
            pullToRefresh.setRefreshing(false);
        });
        //bottom navigation
//        BottomNavigationView navigation = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.nav_home:
                        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                        break;

                    case R.id.nav_covid_stat:
                        startActivity(new Intent(getApplicationContext(), ReportActivity.class));
                        break;

                    case R.id.nav_sign_out:
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        break;
                }
                return false;
            }
        });
        getDataforSummaryHistory();


    }

    private void getDataforSummaryHistory() {
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());
        user_history.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

//                System.out.println("COUNT Key: " + snapshot.getKey());
//                System.out.println("COUNT CHILDREN: " + snapshot.getChildrenCount());

                History history = snapshot.getValue(History.class);
                String risk = history.getRisk();
                System.out.println("COUNT risk: " + risk);

                for (DataSnapshot snap : snapshot.getChildren())
                {
                    if(snap.getKey().equals("risk"))
                    {
                        //If you want the node value
                        list.add(snap.getValue().toString());
                        System.out.println("COUNT getValue: " + snap.getValue().toString());

                        //If you want the key value
//                        list.add(snap.getKey());
//                        System.out.println("COUNT getKey: " + snap.getKey());
                    }
                }
                System.out.println("COUNT list: " + list);

                int countHigh = Collections.frequency(list, "High");
                int countMedium = Collections.frequency(list, "Medium");
                int countLow = Collections.frequency(list, "Low");
                home_risk_high_count.setText(String.format("%d",countHigh));
                home_risk_medium_count.setText(String.format("%d",countMedium));
                home_risk_low_count.setText(String.format("%d",countLow));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //handling bottom navigation
//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = item -> {
//        switch (item.getItemId()) {
//            case R.id.navigation_home:
//                startActivity(new Intent(StatsActivity.this, MainActivity.class));
//                finish();
//            case R.id.navigation_stats:
//
//
//                return true;
//            case R.id.navigation_settings:
//                startActivity(new Intent(StatsActivity.this, SettingMoreActivity.class));
//                finish();
//
//
//        }
//        return false;
//    };

    /**
     * getting json object {
     */
    private void parseJSON() {
        // showpDialog();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlJsonObj, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    // Parsing json object response
                    // response will be a json object
                    String Rcases = response.getString("cases");
                    String Rdeaths = response.getString("deaths");
                    String Rrecovered = response.getString("recovered");
                    String Ractive = response.getString("active");


                    initPieChart();

                    showPieChart(Rcases,Rdeaths,Rrecovered,Ractive);


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                // hidepDialog();
            }
        },

                error -> {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_SHORT).show();
                    // hide the progress dialog
                    // hidepDialog();
                }) {

            //cache for 24 if user not connected to internet
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {

                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 3 * 60 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
                    final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        // Adding request to request queue
        VolleyController.getInstance().addToRequestQueue(jsonObjReq);


    }


    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void showPieChart(String Rcases,String Rdeaths,String Rrecovered,String Ractive){

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = "";

        //initializing data
        Map<String, Integer> typeAmountMap = new HashMap<>();
        typeAmountMap.put("Total Cases",Integer.parseInt(Rcases));
        typeAmountMap.put("Recovered",Integer.parseInt(Rrecovered));;
        typeAmountMap.put("Active",Integer.parseInt(Ractive));
        typeAmountMap.put("Deaths",Integer.parseInt(Rdeaths));


        //initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#D60F0F"));
        colors.add(Color.parseColor("#0288D1"));
        colors.add(Color.parseColor("#7A2CF0"));
        colors.add(Color.parseColor("#43A047"));


        //input data and fit data into pie chart entry
        for(String type: typeAmountMap.keySet()){
            pieEntries.add(new PieEntry(typeAmountMap.get(type).floatValue(), type));
        }



        //collecting the entries with label name
        PieDataSet pieDataSet = new PieDataSet(pieEntries,label);
        //setting text size of the value
        pieDataSet.setValueTextSize(12f);
        //providing color list for coloring different entries
        pieDataSet.setColors(colors);
        //grouping the data set from entry to chart
        PieData pieData = new PieData(pieDataSet);
        //showing the value of the entries, default true if not set
//        pieData.setValueFormatter(new PercentFormatter());
        // usage on whole data object
        pieData.setValueFormatter(new MyValueFormatter());

        pieData.setDrawValues(true);
        pieChart.setDrawSliceText(false);

        pieChart.setData(pieData);
        pieChart.invalidate();

    }

    private void initPieChart(){
        //using percentage as values instead of amount
        pieChart.setUsePercentValues(true);

        //remove the description label on the lower left corner, default true if not set
        pieChart.getDescription().setEnabled(false);

        //enabling the user to rotate the chart, default true
        pieChart.setRotationEnabled(true);
        //adding friction when rotating the pie chart
        pieChart.setDragDecelerationFrictionCoef(0.9f);
        //setting the first entry start from right hand side, default starting from top
        pieChart.setRotationAngle(0);

        //highlight the entry when it is tapped, default true if not set
        pieChart.setHighlightPerTapEnabled(true);
        //adding animation so the entries pop up from 0 degree
        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        //setting the color of the hole in the middle, default white
        pieChart.setHoleColor(Color.parseColor("#000000"));

//        pieChart.setHoleRadius(10f);
//        pieChart.setTransparentCircleRadius(0f);
    }

    public class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,###,##0.0");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            if(value < 5) return "";
            else return mFormat.format(value) + " %";
        }
    }


}
