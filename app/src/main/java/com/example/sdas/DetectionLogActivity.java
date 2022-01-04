package com.example.sdas;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sdas.Model.History;
import com.example.sdas.Utils.Common;
import com.example.sdas.ViewHolder.HistoryAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.paperdb.Paper;

public class DetectionLogActivity extends AppCompatActivity implements View.OnClickListener{

    //Header & Navigation Menu
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView userName,userEmail;

    //history
    private RecyclerView recyclerView;
    HistoryAdapter adapter;
    DatabaseReference user_history, publicLocation;
    private TextView riskHighCountDaily, riskMediumCountDaily, riskLowCountDaily;
    List<String> list = new ArrayList<>();

    TextView name, email, empid;
    ImageView navprofile;
    private Button mTrackButton,mStopButton;
    DatabaseReference user_information;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_log);
        setTitle("Detection Log");

        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        user_information.keepSynced(true);

        //declaration
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        actionBarDrawerToggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View navView = navigationView.inflateHeaderView(R.layout.header);

        userName = (TextView) navView.findViewById(R.id.name);
        userEmail = (TextView) navView.findViewById(R.id.email);

        recyclerView = findViewById(R.id.recycler_all_history);


        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDb = mDatabase.getReference();
        String userKey = Common.loggedUser.getUid();

        mDb.child(Common.USER_INFORMATION).child(userKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uName = dataSnapshot.child("name").getValue(String.class);
                String uEmail = dataSnapshot.child("email").getValue(String.class);

                userName.setText(uName);
                userEmail.setText(uEmail);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // Create a instance of the database and get
        // its reference
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());

        // To display the Recycler view linearly
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // It is a class provide by the FirebaseUI to make a
        // query in the database to fetch appropriate data
        FirebaseRecyclerOptions<History> options
                = new FirebaseRecyclerOptions.Builder<History>()
                .setQuery(user_history, History.class)
                .build();
        // Connecting object of required Adapter class to
        // the Adapter class itself
        adapter = new HistoryAdapter(options);
        // Connecting Adapter class with the Recycler view*/

        recyclerView.setAdapter(adapter);

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.getLayoutManager().scrollToPosition(0);

        layoutManager.scrollToPositionWithOffset(10, 0);

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
            }
        }, 1000);

        ActivityCompat.requestPermissions(DetectionLogActivity.this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION ,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.nav_home:
                        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                        break;
                    case R.id.nav_detection_log:
                        startActivity(new Intent(getApplicationContext(),DetectionLogActivity.class));
                        break;
                    case R.id.nav_covid_stat:
                        startActivity(new Intent(getApplicationContext(),StatsActivity.class));
                        break;
                    case R.id.nav_report:
                        startActivity(new Intent(getApplicationContext(),ReportActivity.class));
                        break;

                    case R.id.nav_sign_out:
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        break;
                }
                return false;

            }
        });

        Paper.init(this);
    }

    public void onClick(View v) {
        if(R.id.btn_dl_log == v.getId()){
            Toast.makeText(this, "Download pdf", Toast.LENGTH_SHORT).show();

//            createPDF();
        }
    }

    // Function to tell the app to start getting
    // data from database on starting of the activity
    @Override protected void onStart()
    {
        super.onStart();
        adapter.startListening();
    }

    // Function to tell the app to stop getting
    // data from database on stopping of the activity
    @Override protected void onStop()
    {
        super.onStop();
        adapter.stopListening();
    }

//    private void createPDF() {
//        PdfDocument pdfDocument = new PdfDocument();
//
//        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());
//
//        File output = null;
//
//        int n;
//        File outputDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DOWNLOADS), "PDF");
//        if(!outputDir.exists()){
//            outputDir.mkdirs();
//        }
//
//        Paint paint = new Paint();
//        Paint titlePaint = new Paint();
//
//        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1200,2010,1).create();
//        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
//        Canvas canvas = page.getCanvas();
//        ArrayList<String> name;
//        name = new ArrayList<>();
//        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
//        String userId = currentFirebaseUser.getUid();
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference = database.getReference().child("Staff").child(userId);
//
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//
//                    String sid = snapshot.child("staffId").getValue().toString();
//                    //String sname = snapshot.child("staffName").getValue().toString();
//                    name.add(snapshot.child("staffName").getValue().toString());
//                    System.out.println("name: " + name);
//                    System.out.println("test: " + name.get(0));
//                    paint.setTextAlign(Paint.Align.LEFT);
//                    paint.setTextSize(35f);
//                    paint.setColor(Color.BLACK);
//                    canvas.drawText("Staff Name: "+ name.get(0),20,640,paint);
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        System.out.println("sid: " + userId);
//        canvas.drawBitmap(scaledbmp,0,0,paint);
//        titlePaint.setTextAlign(Paint.Align.CENTER);
//        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
//        titlePaint.setTextSize(70);
//        canvas.drawText("Report Analysis", (pagewidth/2)-90, 350,titlePaint);
//
//        paint.setColor(Color.rgb(0,113,188));
//        paint.setTextSize(30f);
//        paint.setTextAlign(Paint.Align.RIGHT);
//        canvas.drawText("GOMASH",1160,40,paint);
//
//        titlePaint.setTextAlign(Paint.Align.CENTER);
//        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.ITALIC));
//        titlePaint.setTextSize(50);
//        canvas.drawText(semester,(pagewidth/2)-90,450,titlePaint);
//
//
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTextSize(35f);
//        paint.setColor(Color.BLACK);
//        canvas.drawText("Form Type: Student Form",20,640,paint);
//
//        //canvas.drawText("Staff Name: "+ name,20,640,paint);
//
//
//        dateFormat = new SimpleDateFormat("dd/MM/yy");
//        canvas.drawText("Date: "+dateFormat.format(date),pagewidth-500,640,paint);
//        dateFormat = new SimpleDateFormat("HH:mm:ss");
//        canvas.drawText("Time: "+dateFormat.format(date),pagewidth-500,690,paint);
//
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(2);
//        canvas.drawRect(20,780,pagewidth-250,860,paint);
//
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setStyle(Paint.Style.FILL);
//        canvas.drawText("No.",40,830,paint);
//        canvas.drawText("Form Name",200,830,paint);
//        canvas.drawText("Sheet Qty",700,830,paint);
//        canvas.drawText("Cost(RM)",900,830,paint);
//        canvas.drawLine(180,790,180,840,paint);
//        canvas.drawLine(680,790,680,840,paint);
//        canvas.drawLine(880,790,880,840,paint);
//
//        if(reportsAdapter!=null){
//            n=950;
//
//            for(int i=0;i<form.size();i++){
//                paint.setTextAlign(Paint.Align.LEFT);
//                canvas.drawText(String.valueOf(i + 1), 40, n, paint);
//                canvas.drawText(form.get(i).getFormName(), 200, n, paint);
//                canvas.drawText(form.get(i).getSheets(), 700, n, paint);
//                paint.setTextAlign(Paint.Align.RIGHT);
//                canvas.drawText(String.valueOf(form.get(i).getCost()), 1150, n, paint);
//                n+=100;
//            }
//
//
//        }else {
//            titlePaint.setTextAlign(Paint.Align.LEFT);
//            titlePaint.setTextSize(35f);
//            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.ITALIC));
//            canvas.drawText("No data for this semester", 200, 950, titlePaint);
//        }
//
//        int j = 950+(100*(form.size()+1));
//        paint.setColor(Color.rgb(247,147,30));
//        canvas.drawRect(680,j-50,pagewidth-250,j+50,paint);
//        paint.setColor(Color.BLACK);
//        paint.setTextSize(50f);
//        paint.setTextAlign(Paint.Align.LEFT);
//        canvas.drawText("Total",700,j,paint);
//        paint.setTextAlign(Paint.Align.RIGHT);
//        canvas.drawText("RM " + getArrayList(),pagewidth-300,j,paint);
//
//
//        pdfDocument.finishPage(page);
//
//        Random rand = new Random();
//        output = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DOWNLOADS), "PDF"+"/Report"+rand.nextInt()+".pdf");
//        try{
//            FileOutputStream out = new FileOutputStream(output);
//            pdfDocument.writeTo(out);
//            out.flush();
//            out.close();
//            System.out.println("path: " + output);
//            Toast.makeText(getApplicationContext(), "Report pdf is downloaded", Toast.LENGTH_SHORT).show();
//        }catch(IOException e){
//            e.printStackTrace();
//            Toast.makeText(getApplicationContext(), "PDF not downloaded", Toast.LENGTH_SHORT).show();
//        }
//        pdfDocument.close();
//    }
}