package com.example.sdas.Service;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.sdas.HomeActivity;
import com.example.sdas.Model.History;
import com.example.sdas.Model.MyLocation;
import com.example.sdas.R;
import com.example.sdas.Utils.Common;
import com.example.sdas.Utils.NotificationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.joda.time.DateTime;


public class TrackingService extends Service {
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    DatabaseReference user_history;
    DatabaseReference publicLocation;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    public TrackingService() {
        publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // do your jobs here
        updateLocation();
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();


        Long ct = mPreferences.getLong("currentTime", 0);


        System.out.println("Shared current time = " + ct);

        user_history.orderByChild("timestamp").startAt(ct)
        //under construction
//        user_history.orderByChild("datetime").startAt(String.valueOf(currentDT))
//        user_history.orderByChild("timestamp").startAt(String.valueOf(ServerValue.TIMESTAMP))
                .addChildEventListener(new ChildEventListener() {
//        user_history.addChildEventListener(new ChildEventListener() {
            //        user_history.limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                double distance = snapshot.getValue(History.class).getDistance();
                notification(distance);
//                System.out.println("Firebase timestamp = " + snapshot.getValue(History.class).getTimestampLong());


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                user_history.removeEventListener(this);
            }
        });
//        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(0.1f);
        locationRequest.setFastestInterval(5000);
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void updateLocation() {
        buildLocationRequest();
//        Log.d(TAG, "AT update location tracking service");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MyLocationReceiver.class);
        intent.setAction(MyLocationReceiver.ACTION);

        return PendingIntent.getBroadcast(this,111,intent,PendingIntent.FLAG_ONE_SHOT);
    }

    private void notification(double distance) {
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        AudioAttributes att = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

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
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("Device Nearby in " + stringdouble + "m ")
//                .setContentText("Please perform social distancing ")
                .setSound(ringtoneUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("You are at " + risk + " risk"+
                                "\nPlease perform social distancing"))
                .setSmallIcon(R.drawable.ic_baseline_notification_important_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_notification_important_24dp))
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        int id= new Random(System.currentTimeMillis()).nextInt(1000);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(id, builder.build());
    }
}