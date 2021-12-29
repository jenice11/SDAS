package com.example.sdas.Service;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.sdas.Model.History;
import com.example.sdas.Model.MyLocation;
import com.example.sdas.Utils.Common;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class MyLocationReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.example.sdas.UPDATE_LOCATION";

    String uid;
    DatabaseReference trackingUserLocation;
    List<MyLocation> locationList = new ArrayList<>();
    Double distance;
    DatabaseReference history = FirebaseDatabase.getInstance().getReference(Common.HISTORY);
    String key;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    long ct = System.currentTimeMillis();
    Boolean checklist;
    DatabaseReference publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);

    @Override
    public void onReceive(Context context, Intent intent) {

        Paper.init(context);
        uid = Paper.book().read(Common.USER_UID_SAVE_KEY);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        mEditor = mPreferences.edit();


        if(intent != null)
        {
            final String action = intent.getAction();
            if(action.equals(ACTION))
            {
                LocationResult result = LocationResult.extractResult(intent);
                if(result != null)
                {
                    Location location = result.getLastLocation();
                    if(Common.loggedUser != null)
                    {
                        publicLocation.child(Common.loggedUser.getUid()).setValue(location);
                        publicLocation.child(Common.loggedUser.getUid()).child("trackStatus").setValue(true);

//                        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
//                        user_information.child(Common.loggedUser.getUid()).child("trackStatus").setValue("true");
                    }
                    else
                    {
                        publicLocation.child(uid).setValue(location);
                    }
                    Log.d(TAG, "New update "+location);
                    getDistance();
                }
            }
        }
    }


    //start comment
    public void getDistance(){
        trackingUserLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        trackingUserLocation.orderByKey()
                //.equalTo(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
//                            Common.loggedUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                key = postSnapshot.getKey();
//                                userList.add(key);
                                Log.d(TAG, "USER ID KEY: " + key);

                                MyLocation location = postSnapshot.getValue(MyLocation.class);
                                locationList.add(location);
                            }
//                            Log.d("LIST", String.valueOf(locationList));
                            insertHistory(locationList);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        trackingUserLocation.removeEventListener(this);
                    }
                });
    }

    private void insertHistory(final List<MyLocation> locationList) {
        trackingUserLocation.child(Common.loggedUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        String abc = snapshot.getKey();

//                        Log.d("what key===", abc);
                        MyLocation currentlocation = snapshot.getValue(MyLocation.class);

                        double latA = (double) currentlocation.getLatitude();
                        double longA = (double) currentlocation.getLongitude();
                        System.out.println("PLACEHOLDER");
//                        System.out.println("A lat  " + latA + "A long "+ longA);

                        double latitudeB=0, longitudeB=0, x = 0, y = 0;

                        if(locationList != null && locationList.size()!=0)
                        {
                            for(MyLocation location: locationList) {
                                if(location.isTrackStatus()==true)
                                {
                                    Log.d("LIST", String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude()));
                                    if(latA == location.getLatitude() && longA ==location.getLongitude())
                                    {
                                        System.out.println("Coordinate A same user - lat  " + latA + "long "+ longA);
                                    }
                                    else{
                                        latitudeB = Double.valueOf(location.getLatitude());
                                        longitudeB = Double.valueOf(location.getLongitude());

                                        x = Double.valueOf(location.getLatitude());
                                        y = Double.valueOf(location.getLongitude());
//
//                                        System.out.println("B lat  " + latitudeB + "B long "+ longitudeB);
//                                        System.out.println("x lat  " + x + "x long "+ y);
//
//                                        Log.d(TAG, "<<4 == LatA= " + latA + "LongA= " + longA);
//                                        Log.d(TAG, "<<4 == LatB= " + latitudeB + "LongB= " + longitudeB);

                                        distance = calculateDistance(latA,longA, latitudeB, longitudeB);

                                        Log.d(TAG, "<< Distance== " + distance + " >>");

                                        DatabaseReference listhistory = history.child(Common.loggedUser.getUid()).push();
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                        String date = sdf.format(Calendar.getInstance().getTime());

                                        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                        String time = stf.format(Calendar.getInstance().getTime());
                                        String risk = "No Risk";
                                        if(distance<=0.5 && distance >=0){ risk = "High"; }
                                        if(distance<=1.0 && distance >=0.5){ risk = "Medium"; }
                                        if(distance<=1.5 && distance >=1.0){ risk = "Low"; }

                                        DatabaseReference user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());
                                        //Yes its working for 1 variable only
//                                        user_history.orderByChild("distance").equalTo(distance)
//                                                .addListenerForSingleValueEvent(new ValueEventListener() {
//                                                    @Override
//                                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                                        if(dataSnapshot.exists()){
//                                                            System.out.println("Data exist !!!!!");
//                                                            System.out.println(dataSnapshot);
//                                                        } else {
//                                                            System.out.println("Data not exist xxx");
//                                                            System.out.println(dataSnapshot);
//                                                        }
//                                                    }
//
//                                                    @Override
//                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                                                        throw databaseError.toException(); // don't ignore errors
//                                                    }
//                                                });



                                        double LatitudeB2 = latitudeB;
                                        double LongitudeB2 = longitudeB;
                                        String risk2 = risk;

                                        if(distance >=0 && distance <= 1.5) {
                                            user_history.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                                boolean processDone = false;
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if(!dataSnapshot.exists()) {
                                                        if (distance <= 1.5) {
                                                            History history = new History();

                                                            history.setDistance(distance);
                                                            history.setDate(date);
                                                            history.setTime(time);
                                                            history.setLatitudeA(latA);
                                                            history.setLongitudeA(longA);
                                                            history.setLatitudeB(LatitudeB2);
                                                            history.setLongitudeB(LongitudeB2);
                                                            history.setRisk(risk2);
                                                            history.setTimestamp(ct);

                                                            mEditor.putLong("currentTime", ct);
                                                            mEditor.commit();

//                                                            System.out.println("11Shared current time = " + ct);

                                                            listhistory.setValue(history);
                                                            Log.w(TAG, ">>>> new history added, no exist");
                                                        }
                                                    }
                                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                        if(dataSnapshot1.exists()){
                                                            System.out.println(dataSnapshot1);
                                                            if (dataSnapshot1.child("latitudeA").getValue().equals(latA)
                                                                    && dataSnapshot1.child("longitudeA").getValue().equals(longA)
                                                                    && dataSnapshot1.child("latitudeB").getValue().equals(LatitudeB2)
                                                                    && dataSnapshot1.child("longitudeB").getValue().equals(LongitudeB2)
                                                                    && dataSnapshot1.child("distance").getValue().equals(distance)
                                                            ) {
//                                                        System.out.println("Distance got = " + distance);
//                                                        System.out.println("latA got = " + latA);
//                                                        System.out.println("longA got = " + longA);
//                                                        System.out.println("latitudeB got = " + finalLatitudeB);
//                                                        System.out.println("longitudeB got = " + finalLongitudeB);
//                                                        System.out.println("------------------------------");

                                                                long checktime, time1;
                                                                long datatime = (long) dataSnapshot1.child("timestamp").getValue();

                                                                checktime = ct - datatime;
                                                                System.out.println("Data Time = " + datatime + "  / Check time = " + checktime);

                                                                System.out.println("This date = " + dataSnapshot1.child("date").getValue() + "Bo Date = " + date);

                                                                if ((dataSnapshot1.child("date").getValue().equals(date) && checktime >= 600000)) {
                                                                    System.out.println("Time >>> than 10 minutes on same day");

                                                                    History history = new History();

                                                                    history.setDistance(distance);
                                                                    history.setDate(date);
                                                                    history.setTime(time);
                                                                    history.setLatitudeA(latA);
                                                                    history.setLongitudeA(longA);
                                                                    history.setLatitudeB(LatitudeB2);
                                                                    history.setLongitudeB(LongitudeB2);
                                                                    history.setRisk(risk2);
                                                                    history.setTimestamp(ct);

                                                                    mEditor.putLong("currentTime", ct);
                                                                    mEditor.commit();

                                                                    listhistory.setValue(history);
                                                                    Log.w(TAG, ">>>>>>>>>new history added same date");


                                                                } else {
                                                                    System.out.println("Time xx << 10 minutes");
                                                                }
                                                        }

                                                        } else {
                                                            System.out.println("Data not exist yet xxx");
                                                        System.out.println(dataSnapshot1);
                                                            System.out.println(processDone);

                                                            if (!processDone){
                                                                if (distance <= 1.5) {
                                                                    History history = new History();

                                                                    history.setDistance(distance);
                                                                    history.setDate(date);
                                                                    history.setTime(time);
                                                                    history.setLatitudeA(latA);
                                                                    history.setLongitudeA(longA);
                                                                    history.setLatitudeB(LatitudeB2);
                                                                    history.setLongitudeB(LongitudeB2);
                                                                    history.setRisk(risk2);
                                                                    history.setTimestamp(ct);

                                                                    mEditor.putLong("currentTime", ct);
                                                                    mEditor.commit();

//                                                            System.out.println("11Shared current time = " + ct);

                                                                    listhistory.setValue(history);
                                                                    Log.w(TAG, ">>>> new history added, no exist");
                                                                }
                                                                processDone = true;
//                                                            System.out.println("2 " + processDone);
//                                                                user_history.orderByChild("timestamp").removeEventListener(this);

                                                            }

                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    user_history.removeEventListener(this);
                                                }
                                            });
                                        }


                                    }
                                }
                                else
                                {
                                    Log.d(TAG, "<< Not Tracked Status >>");
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        trackingUserLocation.removeEventListener(this);
                    }
                });
    }

    public static double calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[3];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);

        return results[0];
    }

}


