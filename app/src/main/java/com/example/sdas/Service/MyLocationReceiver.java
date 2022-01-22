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
    List<History> historyList = new ArrayList<>();

    Double distance;
    DatabaseReference history = FirebaseDatabase.getInstance().getReference(Common.HISTORY);
    DatabaseReference user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());
    String key;
    long ct = System.currentTimeMillis();
    DatabaseReference publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    public static double calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[3];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);

        return results[0];
    }

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

//        System.out.println("At mylocationreceiver");

        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(ACTION)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    Location location = result.getLastLocation();

                    if (Common.loggedUser != null) {
                        publicLocation.child(Common.loggedUser.getUid()).setValue(location);

//                        publicLocation.child(Common.loggedUser.getUid()).child("trackStatus").setValue(true);

//                        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
//                        user_information.child(Common.loggedUser.getUid()).child("trackStatus").setValue("true");
                    } else {
                        publicLocation.child(uid).setValue(location);
                    }
                    Log.d(TAG, "New update " + location);

//                    System.out.println("Current Location 0 = LatA: " + location.getLatitude() + "  LongA: " + location.getLongitude());
                    putDouble(mEditor, "latA", location.getLatitude());
                    putDouble(mEditor, "longA", location.getLongitude());
//                    Double currentUserLatA = getDouble(mPreferences,"latA", 0);
//                    Double currentUserLongA = getDouble(mPreferences,"longA", 0);
//
//                    System.out.println("Shared Pref - Current Location 1 = LatA: " + currentUserLatA + "  LongA: " + currentUserLongA);
                    mEditor.commit();

                    getListHistory(context);
                }
            }
        }
    }

    public void getListHistory(Context context) {
        user_history.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                        key = postSnapshot.getKey();
//                        Log.d(TAG, "USER ID KEY: " + key);
                        History history = postSnapshot.getValue(History.class);
                        historyList.add(history);
                    }
                    insertHistory(historyList, context);
                } else {
                    insertHistoryNoExisting(context);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void insertHistory(List<History> hList, Context context) {
        String userKey = Common.loggedUser.getUid();

        Double currentUserLatA = getDouble(mPreferences, "latA", 0);
        Double currentUserLongA = getDouble(mPreferences, "longA", 0);

        System.out.println("Shared Pref - Current Location 2 = LatA: " + currentUserLatA + "  LongA: " + currentUserLongA);

        trackingUserLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
        trackingUserLocation.orderByKey()
                //.equalTo(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                key = postSnapshot.getKey();
//                                Log.d(TAG, "USER ID KEY: " + key);
//                                Log.d(TAG, "USER ID CURRENT: " + userKey);

                                double latitudeB, longitudeB;
//                                boolean trackStat = (boolean) postSnapshot.child("trackStatus").getValue();
//                                if(trackStat==true)
//                                {
                                Log.d("LIST", postSnapshot.child("latitude").getValue()
                                        + ", " + postSnapshot.child("longitude").getValue());

                                latitudeB = (double) postSnapshot.child("latitude").getValue();
                                longitudeB = (double) postSnapshot.child("longitude").getValue();

                                distance = calculateDistance(currentUserLatA, currentUserLongA, latitudeB, longitudeB);

                                double roundOffDist = (double) Math.round(distance * 10000) / 10000;

                                DatabaseReference listhistory = history.child(Common.loggedUser.getUid()).push();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                String date = sdf.format(Calendar.getInstance().getTime());

                                SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                String time = stf.format(Calendar.getInstance().getTime());
                                String risk = "No Risk";

                                if (key.equals(userKey)) {
                                    System.out.println("Coordinate A same user - lat  " + currentUserLatA + "long " + currentUserLongA);
                                } else {
                                    if (distance <= 1.5) {
                                        if (distance <= 0.5 && distance >= 0) {
                                            risk = "High";
                                        } else if (distance <= 1.0 && distance >= 0.5) {
                                            risk = "Medium";
                                        } else if (distance <= 1.5 && distance >= 1.0) {
                                            risk = "Low";
                                        }
                                        Log.d(TAG, "<< Distance 1 == " + distance + " // " + roundOffDist + ">>");

                                        String historyuser = hList.get(0).getUserkey();
//                                        System.out.println("userkey history 2 assigned: " + historyuser);

                                        if (!(key.equals(historyuser))) {
                                            System.out.println("the location userkey same with history key prev");
                                        } else {
                                            long timestamp = hList.get(0).getTimestampLong();
                                            long checktime;
                                            checktime = ct - timestamp;

//                                            System.out.println("History last Timestamp= " + timestamp);
//                                            System.out.println("Current ct= " + ct);

                                            System.out.println("Current checktime= " + checktime);

                                            if (checktime >= 75000) {
//                                                Double dist1 = hList.get(0).getDistance();
                                                double hDistance = hList.get(0).getDistance();
                                                System.out.println("Check distance" + hDistance + " /// " + roundOffDist);

//                                                if (roundOffDist == hDistance) {
//                                                    System.out.println("Distance is same as prev history" + hDistance + "--" + roundOffDist + "\nSKIP DIST");
//                                                } else {
                                                    History history = new History();

                                                    history.setDistance(roundOffDist);
                                                    history.setDate(date);
                                                    history.setTime(time);
                                                    history.setLatitudeA(currentUserLatA);
                                                    history.setLongitudeA(currentUserLongA);
                                                    history.setLatitudeB(latitudeB);
                                                    history.setLongitudeB(longitudeB);
                                                    history.setRisk(risk);
                                                    history.setUserkey(key);
                                                    history.setTimestamp(ct);

                                                    listhistory.setValue(history);
                                                    notification(roundOffDist, context);

                                                    System.out.println("ct more than 1min insert");
                                                    System.out.println("Insert checktime= " + checktime);
//                                                }
                                            } else {
                                                System.out.println("Same user - ct less than 1min \n SKIP");
                                            }
                                        }
                                    } else {
                                        System.out.println("Distance more than 1.5m =: " + distance);
                                    }
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

    public void insertHistoryNoExisting(Context context) {
        String userKey = Common.loggedUser.getUid();

        Double currentUserLatA = getDouble(mPreferences, "latA", 0);
        Double currentUserLongA = getDouble(mPreferences, "longA", 0);

        System.out.println("Shared Pref - Current Location 2 = LatA: " + currentUserLatA + "  LongA: " + currentUserLongA);

        trackingUserLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
        trackingUserLocation.orderByKey()
                //.equalTo(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                key = postSnapshot.getKey();

                                double latitudeB, longitudeB;
//                                boolean trackStat = (boolean) postSnapshot.child("trackStatus").getValue();
//
//                                if(trackStat==true)
//                                {
                                Log.d("LIST", postSnapshot.child("latitude").getValue()
                                        + ", " + postSnapshot.child("longitude").getValue());

                                if (key.equals(userKey)) {
                                    System.out.println("Coordinate A same user - lat  " + currentUserLatA + "long " + currentUserLongA);
                                } else {
                                    latitudeB = (double) postSnapshot.child("latitude").getValue();
                                    longitudeB = (double) postSnapshot.child("longitude").getValue();

                                    distance = calculateDistance(currentUserLatA, currentUserLongA, latitudeB, longitudeB);

                                    Log.d(TAG, "<< Distance 2 == " + distance + " >>");

                                    Double dist = getDouble(mPreferences, "dist", 0);
                                    System.out.println("Shared prev distance = " + dist);


                                    if (!(dist.equals(distance))) {
                                        DatabaseReference listhistory = history.child(Common.loggedUser.getUid()).push();
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                        String date = sdf.format(Calendar.getInstance().getTime());

                                        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                        String time = stf.format(Calendar.getInstance().getTime());
                                        String risk = "No Risk";

                                        double roundOffDist = (double) Math.round(distance * 10000) / 10000;


                                        if (distance <= 1.5) {
                                            if (distance <= 0.5 && distance >= 0) {
                                                risk = "High";
                                            }
                                            if (distance <= 1.0 && distance >= 0.5) {
                                                risk = "Medium";
                                            }
                                            if (distance <= 1.5 && distance >= 1.0) {
                                                risk = "Low";
                                            }

                                            History history = new History();

                                            history.setDistance(roundOffDist);
                                            history.setDate(date);
                                            history.setTime(time);
                                            history.setLatitudeA(currentUserLatA);
                                            history.setLongitudeA(currentUserLongA);
                                            history.setLatitudeB(latitudeB);
                                            history.setLongitudeB(longitudeB);
                                            history.setRisk(risk);
                                            history.setUserkey(key);
//                                            history.setTimestamp(history.getTimestampLong());

                                            history.setTimestamp(ct);

//                                                mEditor.putLong("currentTime", ct);
                                            putDouble(mEditor, "dist", distance);
                                            mEditor.apply();


//                                                Double dist = getDouble(mPreferences,"dist", 0);
//                                                System.out.println("S distance 1= " + distance);
//                                                System.out.println("S distance 2= " + dist);

                                            listhistory.setValue(history);
                                            notification(distance, context);
                                        }
                                    } else {
                                        System.out.println("Same distance as prev insert= " + distance);
                                    }
                                }
//                                }
//                                else
//                                {
//                                    Log.d(TAG, "<< Not Tracked Status >>");
//                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        trackingUserLocation.removeEventListener(this);
                    }
                });
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

        String stringdouble = String.format("%.2f", distance);

        String risk = "zero";
        if (distance <= 0.5 && distance >= 0) {
            risk = "HIGH";
        }
        if (distance <= 1.0 && distance >= 0.5) {
            risk = "MEDIUM";
        }
        if (distance <= 1.5 && distance >= 1.0) {
            risk = "LOW";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channel_id", "Test Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Nearby device detected");
            notificationChannel.setSound(ringtoneUri, att);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setContentTitle("Device Nearby in " + stringdouble + "m ")
//                .setContentText("Please perform social distancing ")
                .setSound(ringtoneUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("You are at " + risk + " risk" +
                                "\nPlease perform social distancing"))
                .setSmallIcon(R.drawable.ic_baseline_notification_important_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_baseline_notification_important_24dp))
//                .setGroup("SDAS_GROUP")
//                .setGroupSummary(true)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        int id = new Random(System.currentTimeMillis()).nextInt(1000);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(id, builder.build());
    }
}


