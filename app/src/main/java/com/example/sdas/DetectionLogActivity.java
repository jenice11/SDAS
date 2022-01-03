package com.example.sdas;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sdas.Model.History;
import com.example.sdas.Utils.Common;
import com.example.sdas.ViewHolder.HistoryAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class DetectionLogActivity extends AppCompatActivity implements View.OnClickListener{

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

    TextView name, email, empid;
    ImageView navprofile;
    private Button mTrackButton,mStopButton;
    DatabaseReference user_information;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_log);
        setTitle("Detection Log");

        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        user_information.keepSynced(true);

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

        recyclerView = findViewById(R.id.recycler_all_history);


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

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            String name = user.getDisplayName();
//            String email = user.getEmail();
//
//            userName.setText(name);
//            userEmail.setText(email);
//        }

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

        ActivityCompat.requestPermissions(DetectionLogActivity.this,
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
    }

    public void onClick(View v) {
        if(R.id.btn_dl_log == v.getId()){
//            Toast.makeText(this, "Pressed track button", Toast.LENGTH_SHORT).show();

            Toast.makeText(this, "Download pdf", Toast.LENGTH_SHORT).show();
        }
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
}