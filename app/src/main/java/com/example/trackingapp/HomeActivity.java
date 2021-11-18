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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;


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
    private static final int MY_REQUEST_CODE = 7117;

    /////////////

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
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

        Paper.init(this);

        calculateDistance();

//        trackingUserLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
//
//
//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        trackingUserLocation.orderByKey()
//                //.equalTo(firebaseUser.getUid())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.getValue() == null)
//                        {
//
//                        }
//                        else{
////                            Common.loggedUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
//                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
//                                String key = postSnapshot.getKey();
//                                userList.add(key);
//
//                                Log.d(TAG, "USER ID KEY: " + key);
//                                MyLocation location = postSnapshot.getValue(MyLocation.class);
//                                locationList.add(location);
//                            }
//
////                            for (String list : userList) {
////                                System.out.println(userList);
////                            }
////
//                            for(MyLocation location: locationList) {
//                                Log.d(TAG, "<< " + location + " >>");
//                                Log.d(TAG, "<<Test " + location.getLatitude() + " >>");
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
    }

    public void calculateDistance(){
        trackingUserLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);


        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        trackingUserLocation.orderByKey()
                //.equalTo(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() == null)
                        {

                        }
                        else{
//                            Common.loggedUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                String key = postSnapshot.getKey();
                                userList.add(key);

                                Log.d(TAG, "USER ID KEY: " + key);
                                MyLocation location = postSnapshot.getValue(MyLocation.class);

                                locationList.add(location);
                                Log.d(TAG, "<<Distance results1: =  " + location + " >>");
                            }

//                            for (String list : userList) {
//                                System.out.println(userList);
//                            }

//                            double result = 0;
//                            double[] resultList = new double[1];
//                            for (int k = 0; k < latitudeList.size() - 1; k++)
//                            {
//                                Location.distanceBetween(latitudeList.get(k), longitudeList.get(k),
//                                        latitudeList.get(k+1), longitudeList.get(k + 1), resultList);
//                                result = result + resultList[0];
//
//                            }
                            double latitudeA=0, longitudeA=0, latitudeB=0, longitudeB=0, x = 0, y = 0;
                            float[] results = new float[1];
                            double result = 0;

//
                            for(MyLocation location: locationList) {

                                Log.d(TAG, "<<1 x=" + x + "y=" + y);

                                if(Double.valueOf(latitudeA).equals(x) && Double.valueOf(longitudeA).equals(y)){
                                    Log.d(TAG, "<<2 If lat and long A same then put B>>");
                                    latitudeB = Double.valueOf(location.getLatitude());
                                    longitudeB = Double.valueOf(location.getLongitude());
                                    Log.d(TAG, "<<3 == LatB" + latitudeB + "LongB" + longitudeB);
                                }
                                else{
                                    latitudeA = Double.valueOf(location.getLatitude());
                                    longitudeA = Double.valueOf(location.getLongitude());
                                    Log.d(TAG, "<<4If lat long A no exist, add>>");
                                    Log.d(TAG, "<<3 == LatA" + latitudeA + "LongA" + longitudeA);
                                }

                                x = Double.valueOf(location.getLatitude());
                                y = Double.valueOf(location.getLongitude());


//                                Log.d(TAG, "<<Test " + location.getLatitude() + " >>");
                                
//                                if(latitudeA == location.getLatitude() && longitudeA == location.getLongitude()){
//                                    latitudeB = location.getLatitude();
//                                    longitudeB = location.getLongitude();
//                                    Log.d(TAG, "<<2If lat and long A same then put B>>");
//                                }
//                                else{
//                                    Log.d(TAG, "<<3latitude: " + location.getLatitude() + "longitude: " + location.getLongitude());
////                                    latitudeA = location.getLatitude();
////                                    longitudeA = location.getLongitude();
//                                    Log.d(TAG, "<<4If lat long A no exist, add>>");
//                                }
                            }
                            getDistance(latitudeA,longitudeA, latitudeB, longitudeB, result);
                            Log.d(TAG, "<<5Distance results: =  " + results + " >>");



                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public static double getDistance (double startLatitude,
                                      double startLongitude,
                                      double endLatitude,
                                      double endLongitude,
                                      double result){
        Location loc1 = new Location("");
        loc1.setLatitude(startLatitude);
        loc1.setLongitude(startLongitude);

        Location loc2 = new Location("");
        loc2.setLatitude(endLatitude);
        loc2.setLongitude(endLongitude);

        result = loc1.distanceTo(loc2);

        Log.d(TAG, "<<Distance results: =  " + result + " >>");
        return result;

    }

//    double distance_between(Location l1, Location l2)
//    {
//        //float results[] = new float[1];
//    /* Doesn't work. returns inconsistent results
//    Location.distanceBetween(
//            l1.getLatitude(),
//            l1.getLongitude(),
//            l2.getLatitude(),
//            l2.getLongitude(),
//            results);
//            */
//        double lat1=l1.getLatitude();
//        double lon1=l1.getLongitude();
//        double lat2=l2.getLatitude();
//        double lon2=l2.getLongitude();
//        double R = 6371; // km
//        double dLat = (lat2-lat1)*Math.PI/180;
//        double dLon = (lon2-lon1)*Math.PI/180;
//        lat1 = lat1*Math.PI/180;
//        lat2 = lat2*Math.PI/180;
//
//        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
//                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//        double d = R * c * 1000;
//
//        log_write("dist betn "+
//                d + " " +
//                l1.getLatitude()+ " " +
//                l1.getLongitude() + " " +
//                l2.getLatitude() + " " +
//                l2.getLongitude()
//        );
//
//        return d;
//    }



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