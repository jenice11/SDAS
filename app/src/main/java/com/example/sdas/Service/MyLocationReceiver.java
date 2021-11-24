package com.example.sdas.Service;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.sdas.HomeActivity;
import com.example.sdas.Model.MyLocation;
import com.example.sdas.R;
import com.example.sdas.Utils.Common;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.view.View;
import android.widget.Toast;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import io.paperdb.Paper;

public class MyLocationReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.example.sdas.UPDATE_LOCATION";

    DatabaseReference publicLocation;
    String uid;
    DatabaseReference trackingUserLocation;
    List<MyLocation> locationList = new ArrayList<>();
    List<String> userList = new ArrayList<>();
    long maxID;

    public MyLocationReceiver() {
        publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Paper.init(context);
        uid = Paper.book().read(Common.USER_UID_SAVE_KEY);

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
                }
                getDistance();


            }

        }
    }


    public void getDistance(){
        trackingUserLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

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
//
                            for(MyLocation location: locationList) {
                                if(location.isTrackStatus()==true)
                                {
                                    Log.d(TAG, "<<1 x=" + x + " y=" + y);

                                    if(Double.valueOf(latitudeA).equals(x) && Double.valueOf(longitudeA).equals(y)){
                                        latitudeB = Double.valueOf(location.getLatitude());
                                        longitudeB = Double.valueOf(location.getLongitude());
                                        Log.d(TAG, "<<3 == LatB= " + latitudeB + "LongB= " + longitudeB);
                                    }
                                    else{
                                        latitudeA = Double.valueOf(location.getLatitude());
                                        longitudeA = Double.valueOf(location.getLongitude());
                                        Log.d(TAG, "<<3 == LatA= " + latitudeA + "LongA= " + longitudeA);
                                    }

                                    x = Double.valueOf(location.getLatitude());
                                    y = Double.valueOf(location.getLongitude());
//                                }
                                }
                                else
                                {
                                    Log.d(TAG, "<< Not Tracked Status >>");
                                }
                            }

                            latitudeA = 3.8255664040574278;
                            longitudeA = 103.3282252768507;
                            latitudeB = 3.825566361011295;
                            longitudeB = 103.32822265292521;
                            //Total distance: 1.53 m (5.02 ft)

//                            Log.d(TAG, "<<5 Method 1: Distance results: =  " + results + " >>");

//                            Log.d(TAG, "<< Final == LatA= " + latitudeA + " LongA= " + longitudeA);
//                            Log.d(TAG, "<< Final == LatB= " + latitudeB + " LongB= " + longitudeB);

                            Log.d(TAG, "<<FInal - Distance results: =  " + calculateDistance(latitudeA,longitudeA, latitudeB, longitudeB) + " >>");


                            Double distance = calculateDistance(latitudeA,longitudeA, latitudeB, longitudeB);
                            Log.d(TAG, "<<Double distance =  " + distance + " >>");


                            DatabaseReference history = FirebaseDatabase.getInstance().getReference(Common.HISTORY);

                            history.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists())
                                        maxID = (dataSnapshot.getChildrenCount());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "<< Database Error Fail to read value >>");
                                }
                            });
//                            Date date = Calendar.getInstance().getTime();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            String date = sdf.format(Calendar.getInstance().getTime());

                            SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                            String time = stf.format(Calendar.getInstance().getTime());


                            if(distance < 1.5){
                                history.child(Common.loggedUser.getUid()).child(String.valueOf(maxID+1))
                                        .child("distance").setValue(distance);
                                history.child(Common.loggedUser.getUid()).child(String.valueOf(maxID+1))
                                        .child("date").setValue(date);
                                history.child(Common.loggedUser.getUid()).child(String.valueOf(maxID+1))
                                        .child("time").setValue(time);
                            }

//                            history.child(Common.loggedUser.getUid()).child("").setValue(distance);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    public static double calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[3];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);

        return results[0];
    }





}
