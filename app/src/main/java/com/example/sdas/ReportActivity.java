package com.example.sdas;

import com.example.sdas.Adapter.ViewPagerAdapter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.sdas.Utils.Common;
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ReportActivity extends AppCompatActivity {
    // Progress dialog
    private ProgressDialog pDialog;
    private TextView riskHighCount, riskMediumCount, riskLowCount;
    private TextView riskHighCountWeekly, riskMediumCountWeekly, riskLowCountWeekly;
    private TextView riskHighCountDaily, riskMediumCountDaily, riskLowCountDaily;

    PieChart pieChart;
    TextView textViewScore,textViewComment;
    ImageView navprofile;
    DatabaseReference user_history;
    List<String> list = new ArrayList<>();

    //Header & Navigation Menu
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView userName,userEmail;

    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setTitle("Social Distancing Report");

        //declaration
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        actionBarDrawerToggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View navView = navigationView.inflateHeaderView(R.layout.header);

        //text views
        riskHighCount = findViewById(R.id.home_risk_high_count);
        riskMediumCount = findViewById(R.id.home_risk_medium_count);
        riskLowCount = findViewById(R.id.home_risk_low_count);
        riskHighCountWeekly = findViewById(R.id.home_risk_high_count_weekly);
        riskMediumCountWeekly = findViewById(R.id.home_risk_medium_count_weekly);
        riskLowCountWeekly = findViewById(R.id.home_risk_low_count_weekly);
        riskHighCountDaily = findViewById(R.id.home_risk_high_count_daily);
        riskMediumCountDaily = findViewById(R.id.home_risk_medium_count_daily);
        riskLowCountDaily = findViewById(R.id.home_risk_low_count_daily);

        pieChart = findViewById(R.id.pieChart_view);

        userName = (TextView) navView.findViewById(R.id.name);
        userEmail = (TextView) navView.findViewById(R.id.email);

        tabLayout=(TabLayout)findViewById(R.id.tabLayout);
        viewPager=(ViewPager)findViewById(R.id.viewPager);


        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDb = mDatabase.getReference();
        String userKey = Common.loggedUser.getUid();

        mDb.child(Common.USER_INFORMATION).child(userKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uName = dataSnapshot.child("name").getValue(String.class);
                String uEmail = dataSnapshot.child("email").getValue(String.class);

                userName.setText(uName);
                userEmail.setText(uEmail);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        //progress dialog
        // pDialog = new ProgressDialog(this);
        // pDialog.setMessage("loading...");
        // pDialog.setCancelable(false);

        //parsing json data

        getDataforSummaryHistoryDaily();
        getDataforSummaryHistoryWeekly();
        getDataforSummaryHistoryOverall();

            //adding swipe to refresh
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.statsrefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            getDataforSummaryHistoryDaily();
            getDataforSummaryHistoryWeekly();
            getDataforSummaryHistoryOverall();

            pullToRefresh.setRefreshing(false);
        });
        tabLayout.addTab(tabLayout.newTab().setText("Daily"));
        tabLayout.addTab(tabLayout.newTab().setText("Weekly"));
        tabLayout.addTab(tabLayout.newTab().setText("Overall"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
//        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#2699FB"));
//        tabLayout.setTabTextColors(Color.parseColor("#FFFFFF"), Color.parseColor("#ffffff"));

        final ViewPagerAdapter viewadapter = new ViewPagerAdapter(this,getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(viewadapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.nav_home:
                        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                        break;
                    case R.id.nav_detection_log:
                        startActivity(new Intent(getApplicationContext(),DetectionLogActivity.class));
                        break;
                    case R.id.nav_covid_stat:
                        startActivity(new Intent(getApplicationContext(),StatsActivity.class));
                        break;
                    case R.id.nav_report:
                        startActivity(new Intent(getApplicationContext(),ReportActivity.class));
                        break;

                    case R.id.nav_sign_out:
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        break;
                }
                return false;

            }
        });
    }

    private void getDataforSummaryHistoryOverall() {
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());
        user_history.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren())
                {
                    for (DataSnapshot snapchild : snap.getChildren()){
                        if(snapchild.getKey().equals("risk"))
                        {
                            list.add(snapchild.getValue().toString());
//                            System.out.println("COUNT getValue: " + snapchild.getValue().toString());
                        }
                    }

//                    System.out.println("COUNT list: " + list);

                    int countHigh = Collections.frequency(list, "High");
                    int countMedium = Collections.frequency(list, "Medium");
                    int countLow = Collections.frequency(list, "Low");

                    //calculateScore(countHigh,countMedium,countLow);
                    riskHighCount.setText(String.format("%d",countHigh));
                    riskMediumCount.setText(String.format("%d",countMedium));
                    riskLowCount.setText(String.format("%d",countLow));
                }
//                System.out.println("COUNT list: " + list);
                list.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDataforSummaryHistoryDaily() {
        long todayStart, todayEnd;

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
        todayStart = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
        todayEnd = cal.getTimeInMillis();

        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());
        user_history.orderByChild("timestamp").startAt(todayStart).endAt(todayEnd).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren())
                {
//                    System.out.println("Weekly List: " + snap);

                    for (DataSnapshot snapchild : snap.getChildren()){
                        if(snapchild.getKey().equals("risk"))
                        {
                            list.add(snapchild.getValue().toString());
                        }
                    }

                    int countHigh = Collections.frequency(list, "High");
                    int countMedium = Collections.frequency(list, "Medium");
                    int countLow = Collections.frequency(list, "Low");

                    riskHighCountDaily.setText(String.format("%d",countHigh));
                    riskMediumCountDaily.setText(String.format("%d",countMedium));
                    riskLowCountDaily.setText(String.format("%d",countLow));
                }
//                System.out.println("COUNT list: " + list);

                list.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDataforSummaryHistoryWeekly() {
        long firstDay, lastDay;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        // get start of this week in milliseconds
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        firstDay = cal.getTimeInMillis();

        // start of the next week
        cal.add(Calendar.WEEK_OF_YEAR, 1);
//        System.out.println("Start of the next week:   " + cal.getTime());
//        System.out.println("... in milliseconds:      " + cal.getTimeInMillis());
        lastDay =cal.getTimeInMillis();

        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());
        user_history.orderByChild("timestamp").startAt(firstDay).endAt(lastDay).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren())
                {
//                    System.out.println("Weekly List: " + snap);

                    for (DataSnapshot snapchild : snap.getChildren()){
                        if(snapchild.getKey().equals("risk"))
                        {
                            list.add(snapchild.getValue().toString());
                        }
                    }

                    int countHigh = Collections.frequency(list, "High");
                    int countMedium = Collections.frequency(list, "Medium");
                    int countLow = Collections.frequency(list, "Low");

//                    calculateScore(countHigh,countMedium,countLow);
                    riskHighCountWeekly.setText(String.format("%d",countHigh));
                    riskMediumCountWeekly.setText(String.format("%d",countMedium));
                    riskLowCountWeekly.setText(String.format("%d",countLow));
                }
//                System.out.println("COUNT list: " + list);

                list.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void calculateScore(int countHigh, int countMedium, int countLow){
//        int high = 3;
//        int medium = 2;
//        int low = 1;
//        int scorePositive,scoreNegative,overallScore;
//        String comment = null;
//
//        scorePositive = 100;
//        scoreNegative = (countHigh * high) + (countMedium * medium) + (countLow * low);
//
//        textViewScore = findViewById(R.id.textViewScore);
//        textViewComment = findViewById(R.id.textViewComment);
//        overallScore = scorePositive - scoreNegative;
//
//        if(scoreNegative<=20)
//        {
//            textViewScore.setTextColor(Color.parseColor("#43A047"));
//            textViewComment.setTextColor(Color.parseColor("#43A047"));
//            comment = "You are at low risk!";
//        }
//        else if(scoreNegative <=40 && scoreNegative >=20)
//        {
//            textViewScore.setTextColor(Color.parseColor("#A84420"));
//            textViewComment.setTextColor(Color.parseColor("#A84420"));
//            comment = "You are at medium risk!";
//        }
//        else if(scoreNegative >=40)
//        {
//            textViewScore.setTextColor(Color.RED);
//            textViewComment.setTextColor(Color.RED);
//            comment = "You are at high risk!";
//        }
//
//        textViewScore.setText(String.valueOf(overallScore));
//        textViewComment.setText(comment);
////
////        initPieChart();
////        showPieChart(scorePositive,scoreNegative);
//    }

//    private void showPieChart(int scorePositive, int scoreNegative) {
//
//        ArrayList<PieEntry> pieEntries = new ArrayList<>();
//        String label = "";
//
//        //initializing data
//        Map<String, Integer> typeAmountMap = new HashMap<>();
//        typeAmountMap.put("Positive",scorePositive);
//        typeAmountMap.put("Negative",scoreNegative);
//
//        //initializing colors for the entries
//        ArrayList<Integer> colors = new ArrayList<>();
//        colors.add(Color.parseColor("#DAF1FB"));
//        colors.add(Color.parseColor("#3495BD"));
//
//
//        //input data and fit data into pie chart entry
//        for(String type: typeAmountMap.keySet()){
//            pieEntries.add(new PieEntry(typeAmountMap.get(type).floatValue(), type));
//        }
//
//        //collecting the entries with label name
//        PieDataSet pieDataSet = new PieDataSet(pieEntries,label);
//        //setting text size of the value
//        pieDataSet.setValueTextSize(12f);
//        //providing color list for coloring different entries
//        pieDataSet.setColors(colors);
//        //grouping the data set from entry to chart
//        PieData pieData = new PieData(pieDataSet);
//        //showing the value of the entries, default true if not set
////        pieData.setValueFormatter(new PercentFormatter());
//        // usage on whole data object
//        pieData.setValueFormatter(new MyValueFormatter());
//
//        pieData.setDrawValues(true);
//        pieChart.setDrawSliceText(false);
//
//        pieChart.setData(pieData);
//        pieChart.invalidate();
//
//    }

//    private void initPieChart(){
//        //using percentage as values instead of amount
//        pieChart.setUsePercentValues(true);
//
//        //remove the description label on the lower left corner, default true if not set
//        pieChart.getDescription().setEnabled(false);
//
//        //enabling the user to rotate the chart, default true
//        pieChart.setRotationEnabled(true);
//        //adding friction when rotating the pie chart
//        pieChart.setDragDecelerationFrictionCoef(0.9f);
//        //setting the first entry start from right hand side, default starting from top
//        pieChart.setRotationAngle(0);
//
//        //highlight the entry when it is tapped, default true if not set
//        pieChart.setHighlightPerTapEnabled(true);
//        //adding animation so the entries pop up from 0 degree
//        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
//        //setting the color of the hole in the middle, default white
//        pieChart.setHoleColor(Color.parseColor("#000000"));
//
////        pieChart.setHoleRadius(10f);
////        pieChart.setTransparentCircleRadius(0f);
//    }
//
//    public static class MyValueFormatter implements IValueFormatter {
//
//        private DecimalFormat mFormat;
//
//        public MyValueFormatter() {
//            mFormat = new DecimalFormat("###,###,###,##0.0");
//        }
//
//        @Override
//        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//            if(value < 5) return "";
//            else return mFormat.format(value) + " %";
//        }
//    }




}
