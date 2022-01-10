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
                    detectUser(context);
                }
            }
        }
    }

    public void detectUser(Context context){
        String userKey = Common.loggedUser.getUid();

        trackingUserLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
        trackingUserLocation.orderByKey()
                //.equalTo(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                key = postSnapshot.getKey();
                                Log.d(TAG, "USER ID KEY: " + key);
                                Log.d(TAG, "USER ID CURRENT: " + userKey);

                                Double currentUserLatA = getDouble(mPreferences,"latA", 0);
                                Double currentUserLongA = getDouble(mPreferences,"longA", 0);

                                double latitudeB, longitudeB;
                                boolean trackStat = (boolean) postSnapshot.child("trackStatus").getValue();

                                if(trackStat==true)
                                {
                                    Log.d("LIST", String.valueOf(postSnapshot.child("latitude").getValue())
                                            + ", " + String.valueOf(postSnapshot.child("longitude").getValue()));

                                    if(key.equals(userKey))
                                    {
                                        System.out.println("Coordinate A same user - lat  " + currentUserLatA + "long "+ currentUserLongA);
                                    }
                                    else{
                                        latitudeB = (double) postSnapshot.child("latitude").getValue();
                                        longitudeB = (double) postSnapshot.child("longitude").getValue();

                                        distance = calculateDistance(currentUserLatA,currentUserLongA, latitudeB, longitudeB);

                                        Log.d(TAG, "<< Distance== " + distance + " >>");

                                        Double dist = getDouble(mPreferences,"dist", 0);
                                        System.out.println("Shared prev distance = " + dist);


                                        if(!(dist.equals(distance))){
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
                                                history.setLatitudeA(currentUserLatA);
                                                history.setLongitudeA(currentUserLongA);
                                                history.setLatitudeB(latitudeB);
                                                history.setLongitudeB(longitudeB);
                                                history.setRisk(risk);
//                                            history.setTimestamp(history.getTimestampLong());

                                                history.setTimestamp(ct);

//                                                mEditor.putLong("currentTime", ct);
                                                putDouble(mEditor,"dist", distance);
                                                mEditor.apply();


//                                                Double dist = getDouble(mPreferences,"dist", 0);
//                                                System.out.println("S distance 1= " + distance);
//                                                System.out.println("S distance 2= " + dist);

                                                listhistory.setValue(history);
                                                notification(distance,context);
                                            }
                                        }else{
                                            System.out.println("Same distance as prev insert= " + distance);
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
                    public void onCancelled(@NonNull DatabaseError databaseError) {
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


