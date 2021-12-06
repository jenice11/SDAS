package com.example.sdas;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sdas.Interface.IFirebaseLoadDone;
import com.example.sdas.Model.History;
import com.example.sdas.Model.MyLocation;
import com.example.sdas.Model.User;
import com.example.sdas.Service.TrackingService;
import com.example.sdas.Utils.Common;
import com.example.sdas.ViewHolder.HistoryAdapter;
//import com.example.sdas.ViewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;

    //history
    private RecyclerView recyclerView;
    HistoryAdapter adapter;
    DatabaseReference user_history, publicLocation;
    private TextView home_risk_high_count, home_risk_medium_count, home_risk_low_count;
    int countHigh = 0, countMedium = 0, countLow = 0;


    TextView name, email, empid;
    ImageView navprofile;
    Toolbar toolbar;
    private Button mTrackButton,mStopButton;
    Double distance;
    DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final int MY_REQUEST_CODE = 7117;

    /////////////

//    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
    RecyclerView recycler_friend_list;
    IFirebaseLoadDone firebaseLoadDone;
    DatabaseReference user_information;
    DatabaseReference trackingUserLocation;
    List<MyLocation> locationList = new ArrayList<>();
    List<String> userList = new ArrayList<>();


//    MaterialSearchBar searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //declaration
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTrackButton = findViewById(R.id.trackButton);
        mTrackButton.setOnClickListener(this);
        mStopButton = findViewById(R.id.stopButton);
        mStopButton.setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_all_history);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View navView = navigationView.inflateHeaderView(R.layout.header);

        name = (TextView) navView.findViewById(R.id.name);
        email = (TextView) navView.findViewById(R.id.email);
        empid = (TextView) navView.findViewById(R.id.empid);
        navprofile = (ImageView) navView.findViewById(R.id.navprofile);

        home_risk_high_count = findViewById(R.id.home_risk_high_count);
        home_risk_medium_count = findViewById(R.id.home_risk_medium_count);
        home_risk_low_count = findViewById(R.id.home_risk_low_count);

        getDataforSummaryHistory();


        // Create a instance of the database and get
        // its reference
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());

        // To display the Recycler view linearly
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // It is a class provide by the FirebaseUI to make a
        // query in the database to fetch appropriate data
        FirebaseRecyclerOptions<History> options
                = new FirebaseRecyclerOptions.Builder<History>()
                .setQuery(user_history, History.class)
                .build();
        // Connecting object of required Adapter class to
        // the Adapter class itself
        adapter = new HistoryAdapter(options);
        // Connecting Adapter class with the Recycler view*/
        recyclerView.setAdapter(adapter);

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        ActivityCompat.requestPermissions(HomeActivity.this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION ,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
//                    case R.id.nav_find_people:
//                        Intent intent = new Intent(HomeActivity.this, AllPeopleActivity.class);
//                        startActivity(intent);
//                        break;
//                    case R.id.nav_add_people:
//                        Intent intent1 = new Intent(HomeActivity.this, FriendRequestActivity.class);
//                        startActivity(intent1);
//                        break;
                    case R.id.nav_sign_out:

                        break;
                }
                return false;

            }
        });
        Paper.init(this);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        Log.d(TAG, token);
                        Toast.makeText(HomeActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void onClick(View v) {
        if(R.id.trackButton == v.getId()){
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(new Intent(this, TrackingService.class));
//                Toast.makeText(this, "Start tracking now Fore", Toast.LENGTH_SHORT).show();
//            } else {
            Intent ishintent = new Intent(this, TrackingService.class);
            PendingIntent pintent = PendingIntent.getService(this, 0, ishintent, 0);
            AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pintent);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),10000, pintent);

//                startService(new Intent(this, TrackingService.class));
                Toast.makeText(this, "Start tracking now Intent", Toast.LENGTH_SHORT).show();
//            }

//            startService(new Intent(this, TrackingService.class));
//            Toast.makeText(this, "Start tracking now", Toast.LENGTH_SHORT).show();
        }

        if(R.id.stopButton == v.getId()){
            publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
            publicLocation.child(Common.loggedUser.getUid()).child("trackStatus").setValue(false);

            stopService(new Intent(this, TrackingService.class));

            Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
        publicLocation.child(Common.loggedUser.getUid()).child("trackStatus").setValue(false);
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

    private void getDataforSummaryHistory() {
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());

        // Attach a listener to read the data at your profile reference
        user_history.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    String key =  child.getKey();

//                    Log.d("User key", child.getKey());
//                    Log.d("User ref", child.getRef().toString());
//                    Log.d("User val", child.getValue().toString());

                    History history = dataSnapshot.child(key).getValue(History.class);
                    String risk = history.getRisk();
//                    Log.e("Get Data", risk);

                    if(risk.equals("High")){
                        countHigh++;
//                        Log.e("Count===", String.valueOf(countHigh));
                    }
                    else if(risk.equals("Medium")){
                        countMedium++;
                    }
                    else if(risk.equals("Low")){
                        countLow++;
                    }

                }

//                Log.e("countHig2h===", String.valueOf(countHigh));
//                Log.e("countMedium2===", String.valueOf(countMedium));
//                Log.e("countLow2===", String.valueOf(countLow));
                home_risk_high_count.setText(String.format("%d",countHigh));
                home_risk_medium_count.setText(String.format("%d",countMedium));
                home_risk_low_count.setText(String.format("%d",countLow));

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

}