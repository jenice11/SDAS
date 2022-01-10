package com.example.sdas.Service;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.sdas.Model.History;
import com.example.sdas.Model.MyLocation;
import com.example.sdas.R;
import com.example.sdas.Utils.Common;
import com.google.android.gms.location.LocationResult;
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
import java.util.Random;

import io.paperdb.Paper;

public class MyLocationReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.example.sdas.UPDATE_LOCATION";

    String uid;
    DatabaseReference trackingUserLocation;
    List<MyLocation> locationList = new ArrayList<>();
    List<MyLocation> locationList2 = new ArrayList<>();

    Double distance;
    DatabaseReference history = FirebaseDatabase.getInstance().getReference(Common.HISTORY);
    String key;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    long ct = System.currentTimeMillis();

    DatabaseReference publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

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

                    putDouble(mEditor,"latA", location.getLatitude());
                    putDouble(mEditor,"longA", location.getLongitude());

//                    Double currentUserLatA = getDouble(mPreferences,"latA", 0);
//                    Double currentUserLongA = getDouble(mPreferences,"longA", 0);
//
//                    System.out.println("Shared Pref - Current Location 1 = LatA: " + currentUserLatA + "  LongA: " + currentUserLongA);
//
                    getDistance(context);

//                    Intent intent2service = new Intent(context,TrackingService.class);
//                    Bundle args = new Bundle();
//                    args.putSerializable("ARRAYLIST",(Serializable)locationList);
//                    intent.putExtra("BUNDLE",args);
//
//                    Bundle extra = new Bundle();
//                    extra.putSerializable("objects", (Serializable) locationList);
//
//                    System.out.println("PLACEHOLDER - locationlist 1" + locationList);
//
//
//                    Intent intent2serv = new Intent(context,TrackingService.class);
//                    intent2serv.putExtra("extra", extra);
//                    context.startService(intent2serv);
                }
            }
        }
    }


    //start comment
    public void getDistance(Context context){
        trackingUserLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        trackingUserLocation.orderByKey()
                //.equalTo(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    double LogLatA, LogLonA, LogLatB, LogLonB;
                    String LogDate, LogTime;
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
                                MyLocationReceiver.this.locationList.add(location);
                            }
//                            Log.d("LIST", String.valueOf(locationList));
                            insertHistory(MyLocationReceiver.this.locationList, context);
//                            System.out.println("PLACEHOLDER - locationlist 2" + locationList);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        trackingUserLocation.removeEventListener(this);
                    }
                });
    }

    private void insertHistory(final List<MyLocation> locationList, Context context) {
        trackingUserLocation.child(Common.loggedUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        String abc = snapshot.getKey();
//
//                        Log.d("what key===", abc);
                        MyLocation currentlocation = snapshot.getValue(MyLocation.class);

                        double latA = (double) currentlocation.getLatitude();
                        double longA = (double) currentlocation.getLongitude();

                        Double currentUserLatA = getDouble(mPreferences,"latA", 0);
                        Double currentUserLongA = getDouble(mPreferences,"longA", 0);

                        System.out.println("Current Location - Preferences - Lat A =      " + currentUserLatA);
                        System.out.println("Current Location - Preferences - Long A =      " + currentUserLongA);

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

                                        if(distance <= 1.5){
                                            if(distance<=0.5 && distance >=0){ risk = "High"; }
                                            if(distance<=1.0 && distance >=0.5){ risk = "Medium"; }
                                            if(distance<=1.5 && distance >=1.0){ risk = "Low"; }

                                            History history = new History();

                                            history.setDistance(distance);
                                            history.setDate(date);
                                            history.setTime(time);
                                            history.setLatitudeA(latA);
                                            history.setLongitudeA(longA);
                                            history.setLatitudeB(latitudeB);
                                            history.setLongitudeB(longitudeB);
                                            history.setRisk(risk);
//                                            history.setTimestamp(history.getTimestampLong());

                                            history.setTimestamp(ct);

                                            mEditor.putLong("currentTime", ct);
                                            mEditor.commit();

                                            System.out.println("11Shared current time = " + ct);

                                            listhistory.setValue(history);

                                            notification(distance,context);
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

    private void notification(double distance, Context context) {
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        AudioAttributes att = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        Intent intent = new Intent(context, MyLocationReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        String stringdouble= String.format("%.2f", distance);

        String risk = "zero";
        if(distance<=0.5 && distance >=0){ risk = "HIGH"; }
        if(distance<=1.0 && distance >=0.5){ risk = "MEDIUM"; }
        if(distance<=1.5 && distance >=1.0){ risk = "LOW"; }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channel_id", "Test Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Nearby device detected");
            notificationChannel.setSound(ringtoneUri,att);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setContentTitle("Device Nearby in " + stringdouble + "m ")
//                .setContentText("Please perform social distancing ")
                .setSound(ringtoneUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("You are at " + risk + " risk"+
                                "\nPlease perform social distancing"))
                .setSmallIcon(R.drawable.ic_baseline_notification_important_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_baseline_notification_important_24dp))
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        int id= new Random(System.currentTimeMillis()).nextInt(1000);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(id, builder.build());
    }


}


