package com.example.sdas.Service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

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

//        System.out.println("Start service at , " + time);
        updateLocation();
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());

        //job completed. Rest for 5 second before doing another one
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //do job again
        startJob();
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setFastestInterval(5000);
        locationRequest.setInterval(5000);
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
}