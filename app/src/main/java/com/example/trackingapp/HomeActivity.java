package com.example.trackingapp;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackingapp.Interface.IFirebaseLoadDone;
import com.example.trackingapp.Model.MyLocation;
import com.example.trackingapp.Model.User;
import com.example.trackingapp.Service.TrackingService;
import com.example.trackingapp.Utils.Common;
import com.example.trackingapp.ViewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;

    TextView name, email, empid;
    ImageView navprofile;
    Toolbar toolbar;
    private Button mTrackButton,mStopButton;
    Double distance;
    DatabaseReference mDatabase;
    private FirebaseAuth mAuth;


    /////////////

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
    RecyclerView recycler_friend_list;
    IFirebaseLoadDone firebaseLoadDone;

//    MaterialSearchBar searchBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTrackButton = findViewById(R.id.trackButton);
        mTrackButton.setOnClickListener(this);
        mStopButton = findViewById(R.id.stopButton);
        mStopButton.setOnClickListener(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View navView = navigationView.inflateHeaderView(R.layout.header);

        name = (TextView) navView.findViewById(R.id.name);
        email = (TextView) navView.findViewById(R.id.email);
        empid = (TextView) navView.findViewById(R.id.empid);
        navprofile = (ImageView) navView.findViewById(R.id.navprofile);

        ActivityCompat.requestPermissions(HomeActivity.this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION ,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.nav_find_people:
                        Intent intent = new Intent(HomeActivity.this, AllPeopleActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_add_people:
                        Intent intent1 = new Intent(HomeActivity.this, FriendRequestActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_sign_out:

                        break;
                }
                return false;

            }
        });

    }

    public void onClick(View v) {
        if(R.id.trackButton == v.getId()){
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(new Intent(this, TrackingService.class));
//                Toast.makeText(this, "Start tracking now Fore", Toast.LENGTH_SHORT).show();
//            } else {
                startService(new Intent(this, TrackingService.class));
                Toast.makeText(this, "Start tracking now Intent", Toast.LENGTH_SHORT).show();
//            }

//            startService(new Intent(this, TrackingService.class));
//            Toast.makeText(this, "Start tracking now", Toast.LENGTH_SHORT).show();
//            coordinates();
        }

        if(R.id.stopButton == v.getId()){
            stopService(new Intent(this, TrackingService.class));

            Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show();
        }
    }



//    private void coordinates() {
//        mDatabase.child("users").child(mAuth.getCurrentUser()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                if (!task.isSuccessful()) {
//                    Log.e("firebase", "Error getting data", task.getException());
//                }
//                else {
//                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
//                }
//            }
//        })
//
//        Location locationA = new Location("point A");
//
//        locationA.setLatitude(latA);
//        locationA.setLongitude(lngA);
//
//        Location locationB = new Location("point B");
//
//        locationB.setLatitude(latB);
//        locationB.setLongitude(lngB);
//
//        float distance = locationA.distanceTo(locationB);
//
//        // Attach a listener to read the data at our posts reference
//        mDatabase.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Post post = dataSnapshot.getValue(HomeActivity.class);
//                System.out.println(post);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });
//
//    }
//
//    public static void distanceBetween (double startLatitude, double startLongitude, double endLatitude, double endLongitude, float[] results){
//
//    }



//    private void calculateDistance() {
//        float[] results = new float[3];
//        Location.distanceBetween(self.latitude, self.longitude, position.latitude, position.longitude, results);
//        b.putString("distance_in_meters", String.valueOf(results[0]));
//    }
}