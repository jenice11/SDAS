package com.example.sdas.Service;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
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
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.sdas.HomeActivity;
import com.example.sdas.Model.History;
import com.example.sdas.Model.MyLocation;
import com.example.sdas.Model.User;
import com.example.sdas.R;
import com.example.sdas.Utils.Common;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    long maxID=0;
    Double distance;
    DatabaseReference history = FirebaseDatabase.getInstance().getReference(Common.HISTORY);
    final public static String ONE_TIME = "onetime";
    String key,key2;

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
                            key2 = "0";
//                            Common.loggedUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                key = postSnapshot.getKey();

//                                userList.add(key);
                                Log.d(TAG, "USER ID KEY: " + key);

                                MyLocation location = postSnapshot.getValue(MyLocation.class);

                                locationList.add(location);
//
//                                latitudeA = (double) dataSnapshot.child(key).child("latitude").getValue();
//                                longitudeA = (double) dataSnapshot.child(key).child("longitude").getValue();
//
//
//                                boolean trackstatus = (boolean) dataSnapshot.child(key).child("trackStatus").getValue();
//                                Log.d(TAG, "Track status =" + trackstatus);
//
//                                if(trackstatus==true)
//                                {
//                                    if((key2.equals(key) && key2 != null)){
//                                        Log.d(TAG, "KEY1 same as key 2" + key + "=====" + key2);
//                                    }
//                                    else{
//                                        if(!(Double.valueOf(latitudeB).equals(latitudeA) && Double.valueOf(longitudeB).equals(longitudeA))){
//                                            if(Double.valueOf(latitudeB).equals(x) && Double.valueOf(longitudeB).equals(y)){
//                                                latitudeB = (double) dataSnapshot.child(key).child("latitude").getValue();
//                                                longitudeB = (double) dataSnapshot.child(key).child("longitude").getValue();
//                                                key2 = postSnapshot.getKey();
//
//                                            }
//                                            else{
//                                                x = (double) dataSnapshot.child(key).child("latitude").getValue();
//                                                y = (double) dataSnapshot.child(key).child("longitude").getValue();
//                                                key2 = postSnapshot.getKey();
//
//                                                latitudeB = x;
//                                                longitudeB = y;
//
//                                            }
//                                            Log.d(TAG, "<< CURRENT LAT LONG: " + latitudeA + " , "+ longitudeA);
//                                            Log.d(TAG, ">> OTHER LAT LONG: " + latitudeB + " , "+ longitudeB);
//                                        }
//                                    }
//                                    key2 = key;
                                }

                        double latitudeA=0, longitudeA=0, latitudeB=0, longitudeB=0, x = 0, y = 0;

                        for(MyLocation location: locationList) {
                            if(location.isTrackStatus()==true)
                            {
//                                    Log.d(TAG, "<<1 x=" + x + " y=" + y);
                                if(Double.valueOf(latitudeA).equals(x) && Double.valueOf(longitudeA).equals(y)){
                                    latitudeB = Double.valueOf(location.getLatitude());
                                    longitudeB = Double.valueOf(location.getLongitude());
//                                    Log.d(TAG, "<<3 == LatB= " + latitudeB + "LongB= " + longitudeB);
                                }
                                else{
                                    latitudeA = Double.valueOf(location.getLatitude());
                                    longitudeA = Double.valueOf(location.getLongitude());
//                                    Log.d(TAG, "<<3 == LatA= " + latitudeA + "LongA= " + longitudeA);
                                }

                                x = Double.valueOf(location.getLatitude());
                                y = Double.valueOf(location.getLongitude());
                            }
                            else
                            {
                                Log.d(TAG, "<< Not Tracked Status >>");
                            }
                        }

                        Log.d(TAG, "<<4 == LatA= " + latitudeA + "LongA= " + longitudeA);
                        Log.d(TAG, "<<4 == LatB= " + latitudeB + "LongB= " + longitudeB);


                            distance = calculateDistance(latitudeA,longitudeA, latitudeB, longitudeB);

                            Log.d(TAG, "<< Distance== " + distance + " >>");

                            DatabaseReference listhistory = history.child(Common.loggedUser.getUid()).push();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            String date = sdf.format(Calendar.getInstance().getTime());

                            SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                            String time = stf.format(Calendar.getInstance().getTime());
                            String risk = "No Risk";

                            if(distance <= 1.5){
                                if(distance<=0.5 && distance >=0){ risk = "High"; }
                                if(distance<=1.0 && distance >=0.5){ risk = "Medium"; }
                                if(distance<=1.5 && distance >=1.0){ risk = "Low"; }

                                History history = new History();

                                history.setDistance(distance);
                                history.setDate(date);
                                history.setTime(time);
                                history.setLatitudeA(latitudeA);
                                history.setLongitudeA(longitudeA);
                                history.setLatitudeB(latitudeB);
                                history.setLongitudeB(longitudeB);
                                history.setRisk(risk);

                                listhistory.setValue(history);
                            }



//
////                            latitudeA = 3.8255664040574278;
////                            longitudeA = 103.3282252768507;
////                            latitudeB = 3.825566361011295;
////                            longitudeB = 103.32822265292521;
//                            //Total distance: 1.53 m (5.02 ft)
//
////                            Log.d(TAG, "<<5 Method 1: Distance results: =  " + results + " >>");
//
////                            Log.d(TAG, "<< Final == LatA= " + latitudeA + " LongA= " + longitudeA);
////                            Log.d(TAG, "<< Final == LatB= " + latitudeB + " LongB= " + longitudeB);
//
//                            Log.d(TAG, "<<FInal - Distance results: =  " + calculateDistance(latitudeA,longitudeA, latitudeB, longitudeB) + " >>");
//
//
//                            distance = calculateDistance(latitudeA,longitudeA, latitudeB, longitudeB);
//                            Log.d(TAG, "<<Double distance =  " + distance + " >>");

//                            history.addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                    if(dataSnapshot.exists()) {
//                                        maxID = (dataSnapshot.getChildrenCount());
//                                    }
//                                }
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//                                    Log.d(TAG, "<< Database Error Fail to read value >>");
//                                }
//                            });

//                            Log.d(TAG, "Children count:" + maxID);
//                            Log.d(TAG, "id:" + String.valueOf(maxID+1));



//                            DatabaseReference listhistory = history.child(String.valueOf(maxID+1));



//                            DatabaseReference listhistory = history.child(dataSnapshot.getKey()).push();
//                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
//                            String date = sdf.format(Calendar.getInstance().getTime());
//
//                            SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
//                            String time = stf.format(Calendar.getInstance().getTime());
//                            String risk = "No Risk";
//
//                            if(distance <= 1.5){
//                                listhistory.child("distance").setValue(distance);
//                                listhistory.child("date").setValue(date);
//                                listhistory.child("time").setValue(time);
//                                listhistory.child("latitudeA").setValue(latitudeA);
//                                listhistory.child("longitudeA").setValue(longitudeA);
//                                listhistory.child("latitudeB").setValue(latitudeB);
//                                listhistory.child("longitudeB").setValue(longitudeB);
//                                listhistory.child("userB").setValue(key);
//
//                                if(distance<=0.5 && distance >=0){ risk = "High"; }
//                                if(distance<=1.0 && distance >=0.5){ risk = "Medium"; }
//                                if(distance<=1.5 && distance >=1.0){ risk = "Low"; }
//
//                                listhistory.child("risk").setValue(risk);
//                            }



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


