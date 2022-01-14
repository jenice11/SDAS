package com.example.sdas.Service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.core.app.ActivityCompat;

import com.example.sdas.Utils.Common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


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
//        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
//        String time = stf.format(Calendar.getInstance().getTime());
//
//        System.out.println("Start service at , " + time);
//        updateLocation();
//        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                startJob();
            }
        });
        t.start();

//        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    private void startJob(){
        //do job here
        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String time = stf.format(Calendar.getInstance().getTime());

        System.out.println("Start service at , " + time);
        updateLocation();
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());

        //job completed. Rest for 5 second before doing another one
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //do job again
        startJob();
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(0.1f);
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void updateLocation() {
        buildLocationRequest();
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
//        System.out.println("PI = " + intent);

//        return PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        return PendingIntent.getBroadcast(this,111,intent,PendingIntent.FLAG_ONE_SHOT);
    }

//    @Override
//    public void onDestroy() {
//        Intent intent = new Intent(this, MyLocationReceiver.class);
//        intent.setAction(MyLocationReceiver.ACTION);
//        System.out.println("PI = " + intent);
//
//        PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//
//        System.out.println(" service stopped...");
//    }

//    private void notification(double distance) {
//        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//        AudioAttributes att = new AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                .build();
//
//        Intent intent = new Intent(this, HomeActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//
//        String stringdouble= String.format("%.2f", distance);
//
//        String risk = "zero";
//        if(distance<=0.5 && distance >=0){ risk = "HIGH"; }
//        if(distance<=1.0 && distance >=0.5){ risk = "MEDIUM"; }
//        if(distance<=1.5 && distance >=1.0){ risk = "LOW"; }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel("channel_id", "Test Notification Channel",
//                    NotificationManager.IMPORTANCE_HIGH);
//            notificationChannel.setDescription("Nearby device detected");
//            notificationChannel.setSound(ringtoneUri,att);
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
//                .setContentTitle("Device Nearby in " + stringdouble + "m ")
////                .setContentText("Please perform social distancing ")
//                .setSound(ringtoneUri)
//                .setPriority(NotificationCompat.PRIORITY_MAX)
//                .setStyle(new NotificationCompat.BigTextStyle()
//                        .bigText("You are at " + risk + " risk"+
//                                "\nPlease perform social distancing"))
//                .setSmallIcon(R.drawable.ic_baseline_notification_important_24dp)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_notification_important_24dp))
//                // Set the intent that will fire when the user taps the notification
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//
//        int id= new Random(System.currentTimeMillis()).nextInt(1000);
//
//        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
//        managerCompat.notify(id, builder.build());
//    }
}