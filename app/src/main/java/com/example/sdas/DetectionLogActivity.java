package com.example.sdas;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sdas.Model.History;
import com.example.sdas.Utils.Common;
import com.example.sdas.Adapter.HistoryAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Objects;

import io.paperdb.Paper;

public class DetectionLogActivity extends AppCompatActivity implements View.OnClickListener {

    //Header & Navigation Menu
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView userName,userEmail;

    //history
    private RecyclerView recyclerView;
    HistoryAdapter adapter;
    DatabaseReference user_history;
    List<String> list = new ArrayList<>();
    static String uName, uEmail;

    TextView name, email, empid;
    ImageView navprofile;
    private Button logDL;
    DatabaseReference user_information;

    List<History> historyList = new ArrayList<>();

    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_log);
        setTitle("Detection Log");

        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        user_information.keepSynced(true);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        mEditor = mPreferences.edit();

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

        logDL = findViewById(R.id.btn_dl_log);
        logDL.setOnClickListener(this);

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
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE ,Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
            v.setDrawingCacheEnabled(true);
            getLog();
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

    private void getLog(){
        ArrayList<String> logList;
        logList = new ArrayList<>();
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());
        user_history.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot snapchild : snapshot.getChildren()){
                        logList.add(snapchild.child("date").getValue().toString());
                        History history = snapchild.getValue(History.class);
                        DetectionLogActivity.this.historyList.add(history);
                    }
                }
//                System.out.println("loglist2-: " + logList);
                createPDF(DetectionLogActivity.this.historyList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void createPDF(List<History> logList) {
        int pagewidth = 1440;
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());
        PdfDocument pdfDocument = new PdfDocument();

        File output = null;

        int n;
        File outputDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "SDAS_LOG");
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }

        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1200,2010,1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        titlePaint.setTextSize(70);
        canvas.drawText("Detection History Log", (pagewidth/2)-90, 170,titlePaint);

        String name = mPreferences.getString("name", "");
        String email = mPreferences.getString("email", "");

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(35f);
        paint.setColor(Color.BLACK);
        canvas.drawText("Student Name:   "+ name,20,290,paint);
        canvas.drawText("Student Email:    "+ email,20,335,paint);


        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.ITALIC));
        titlePaint.setTextSize(50);


        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(35f);
        paint.setColor(Color.BLACK);
        //canvas.drawText("Staff Name: "+ name,20,640,paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(20,380,pagewidth-250,460,paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText("No.",50,430,paint);
        canvas.drawText("Date Time",200,430,paint);
        canvas.drawText("Risk",700,430,paint);
        canvas.drawText("Distance",950,430,paint);
        canvas.drawLine(180,390,180,440,paint);
        canvas.drawLine(680,390,680,440,paint);
        canvas.drawLine(930,390,930,440,paint);



        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(35f);
        paint.setColor(Color.BLACK);
        n=530;

        for(int i=0;i<logList.size();i++){
            if(logList.get(i).getRisk().equals("High"))
            {
                paint.setColor(Color.parseColor("#D60F0F"));
            }
            else if(logList.get(i).getRisk().equals("Medium")){
                paint.setColor(Color.parseColor("#C68517"));
            }
            else{
                paint.setColor(Color.parseColor("#49814B"));
            }
            canvas.drawText(String.valueOf(i + 1), 50, n, paint);
            canvas.drawText(logList.get(i).getDate() +" " +logList.get(i).getTime(), 200, n, paint);
            canvas.drawText(logList.get(i).getRisk(), 700, n, paint);
//            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format("%.2f",logList.get(i).getDistance()), 960, n, paint);


            n+=80;
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(20,460,pagewidth-250,n-25,paint);

        pdfDocument.finishPage(page);

        long ct = System.currentTimeMillis();


//        Random rand = new Random();
        output = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "PDF"+"/Report"+ct+".pdf");
        try{
            FileOutputStream out = new FileOutputStream(output);
            pdfDocument.writeTo(out);
            out.flush();
            out.close();
            System.out.println("path: " + output);
            Toast.makeText(getApplicationContext(), "Report pdf is downloaded", Toast.LENGTH_SHORT).show();
            openPdf(output);
        }catch(IOException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "PDF not downloaded", Toast.LENGTH_SHORT).show();
        }
        pdfDocument.close();
    }

    private void openPdf(File output) {
        if (output.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                    BuildConfig.APPLICATION_ID + ".provider", output);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No Application for pdf view", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    private ArrayList<User> getCurrentUser(){
//        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference mDb = mDatabase.getReference();
//        String userKey = Common.loggedUser.getUid();
//
//        mDb.child(Common.USER_INFORMATION).child(userKey).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
////                String uName = dataSnapshot.child("name").getValue(String.class);
////                String uEmail = dataSnapshot.child("email").getValue(String.class);
//
//                User user = dataSnapshot.getValue(User.class);
//                DetectionLogActivity.this.userArr.add(user);
//
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {}
//        });
//        return userArr;
//
//    }



//    private void downloadLogPDF(){
//        new CreatePdf(this)
//                .setPdfName("SDAS_History_Log")
//                .openPrintDialog(openPrintDialog)
//                .setContentBaseUrl(null)
//                .setPageSize(PrintAttributes.MediaSize.ISO_A4)
//                .setContent("Content")
////                .setContent(getString(R.string.content))
//                .setFilePath(this.getExternalFilesDir(null).getAbsolutePath() + "/SDAS_PDF")
//                .setCallbackListener(new CreatePdf.PdfCallbackListener() {
//                    @Override
//                    public void onFailure(String s) {
//                        Toast.makeText(getApplicationContext(), "PDF Convert Fail", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onSuccess(String s) {
//                        Toast.makeText(getApplicationContext(), "PDF Convert Success", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .create();
//    }



}