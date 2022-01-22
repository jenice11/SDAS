package com.example.sdas;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sdas.Model.History;
import com.example.sdas.Service.MyLocationReceiver;
import com.example.sdas.Service.TrackingService;
import com.example.sdas.Utils.Common;
import com.example.sdas.Adapter.HistoryAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
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

import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    //Header & Navigation Menu
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView userName,userEmail;

    //history
    private RecyclerView recyclerView;
    HistoryAdapter adapter;
    DatabaseReference user_history, publicLocation;
    private TextView riskHighCountDaily, riskMediumCountDaily, riskLowCountDaily;
    List<String> list = new ArrayList<>();

    private Button mTrackButton,mStopButton;
    DatabaseReference user_information;

    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("Home");

        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        user_information.keepSynced(true);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        mEditor = mPreferences.edit();


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

        userName = (TextView) navView.findViewById(R.id.name);
        userEmail = (TextView) navView.findViewById(R.id.email);

        mTrackButton = findViewById(R.id.trackButton);
        mTrackButton.setOnClickListener(this);
        mStopButton = findViewById(R.id.stopButton);
        mStopButton.setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_all_history);

        riskHighCountDaily = findViewById(R.id.home_risk_high_count_daily);
        riskMediumCountDaily = findViewById(R.id.home_risk_medium_count_daily);
        riskLowCountDaily = findViewById(R.id.home_risk_low_count_daily);

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDb = mDatabase.getReference();
        String userKey = Common.loggedUser.getUid();

        mDb.child(Common.USER_INFORMATION).child(userKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uName = dataSnapshot.child("name").getValue(String.class);
                String uEmail = dataSnapshot.child("email").getValue(String.class);

                System.out.println("name: " + uName + "/ " +uEmail);


                mEditor.putString("name", uName);
                mEditor.putString("email", uEmail);

                mEditor.apply();

                String name = mPreferences.getString("name", "");
                String email = mPreferences.getString("email", "");

                System.out.println("SHARED: " + name + "/ " +email);


                userName.setText(uName);
                userEmail.setText(uEmail);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        getDataforSummaryHistoryDaily();

        // Create a instance of the database and get
        // its reference
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());

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

        // To display the Recycler view linearly
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // It is a class provide by the FirebaseUI to make a
        // query in the database to fetch appropriate data
        FirebaseRecyclerOptions<History> options
                = new FirebaseRecyclerOptions.Builder<History>()
                .setQuery(user_history.orderByChild("timestamp").startAt(todayStart).endAt(todayEnd), History.class)
                .build();
        // Connecting object of required Adapter class to
        // the Adapter class itself
        adapter = new HistoryAdapter(options);
        // Connecting Adapter class with the Recycler view*/

        recyclerView.setAdapter(adapter);

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.getLayoutManager().scrollToPosition(0);

        layoutManager.scrollToPositionWithOffset(10, 0);

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
            }
        }, 1000);

        ActivityCompat.requestPermissions(HomeActivity.this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION ,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                1);

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
        Paper.init(this);

        Boolean alreadyExecuted=false;
        if(!alreadyExecuted) {
            putDouble(mEditor,"dist", 0.0);
            mEditor.apply();
            alreadyExecuted = true;
        }



//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
//                            return;
//                        }
//
//                        // Get new FCM registration token
//                        String token = task.getResult();
//
//                        // Log and toast
//                        Log.d(TAG, token);
////                        Toast.makeText(HomeActivity.this, token, Toast.LENGTH_SHORT).show();
//                    }
//                });

    }

    public void onClick(View v) {
        if(R.id.trackButton == v.getId()){
//            publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
//            publicLocation.child(Common.loggedUser.getUid()).child("trackStatus").setValue(true);

            Intent ishintent = new Intent(this, TrackingService.class);
            PendingIntent pintent = PendingIntent.getService(this, 0, ishintent, 0);
            AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),1000, pintent);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, pintent);
//            startService(new Intent(this, TrackingService.class));
            startBroadCastReceiver();
            Toast.makeText(this, "Start tracking now", Toast.LENGTH_SHORT).show();

            publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
            publicLocation.child(Common.loggedUser.getUid()).child("trackStatus").setValue(true);



//            startService(new Intent(this, TrackingService.class));
//            Toast.makeText(this, "Start tracking now", Toast.LENGTH_SHORT).show();
        }

        if(R.id.stopButton == v.getId()){
            publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
            publicLocation.child(Common.loggedUser.getUid()).removeValue();
//            publicLocation.child(Common.loggedUser.getUid()).child("trackStatus").setValue(false);

            Intent intent = new Intent(this, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1253, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);

            stopService(new Intent(this, TrackingService.class));
            killBroadCastReceiver();

            Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, TrackingService.class));

        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1253, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
        publicLocation.child(Common.loggedUser.getUid()).child("trackStatus").setValue(false);
        killBroadCastReceiver();

        Log.d(TAG, "App Destroyed + firebase status false");
    }
    // Function to tell the app to start getting
    // data from database on starting of the activity
    @Override protected void onStart()
    {
        super.onStart();
        adapter.startListening();
    }

    // Function to tell the app to stop getting
    // data from database on stopping of the activity
    @Override protected void onStop()
    {
        super.onStop();
        adapter.stopListening();
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

//        System.out.println("Current Time:       " + cal.getTime());
//        System.out.println("... in milliseconds:      " + cal.getTimeInMillis());
//        System.out.println("... First Day:      " + String.valueOf(todayStart));

        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());
        user_history.orderByChild("timestamp").startAt(todayStart).endAt(todayEnd).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren())
                {
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

    private void startBroadCastReceiver() {
        PackageManager pm = this.getPackageManager();
        ComponentName componentName = new ComponentName(this, MyLocationReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
//        Toast.makeText(getApplicationContext(), "BroadCast Receiver Started", Toast.LENGTH_LONG).show();
    }

    private void killBroadCastReceiver() {
        PackageManager pm = this.getPackageManager();
        ComponentName componentName = new ComponentName(this, MyLocationReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
//        Toast.makeText(getApplicationContext(), "BroadCast Receiver Killed", Toast.LENGTH_LONG).show();
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }




}