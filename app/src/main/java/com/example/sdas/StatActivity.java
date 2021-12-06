package com.example.sdas;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.leo.simplearcloader.SimpleArcLoader;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONException;
import org.json.JSONObject;

public class StatActivity extends AppCompatActivity {

    TextView tv_cases, tv_deaths, tv_recovered, tv_active , tv_Active_per_one_million, tv_affected_countries;

    SimpleArcLoader simpleArcLoader;
    ScrollView scrollView;
    PieChart pieChart;

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covid_stat);

        dl = findViewById(R.id.activity_main);
        t  = new ActionBarDrawerToggle(this,dl,R.string.open,R.string.close);

        dl.addDrawerListener(t);
        t.syncState();

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
//                    case R.id.nav_find_people:
//                        Intent intent = new Intent(HomeActivity.this, AllPeopleActivity.class);
//                        startActivity(intent);
//                        break;
//                    case R.id.nav_add_people:
//                        Intent intent1 = new Intent(HomeActivity.this, FriendRequestActivity.class);
//                        startActivity(intent1);
//                        break;
                    case R.id.nav_covid_stat:
                        startActivity(new Intent(getApplicationContext(),StatActivity.class));
                        break;

                    case R.id.nav_sign_out:
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        break;
                }
                return false;

            }
        });


        tv_cases = findViewById(R.id.tv_today_casess);
        tv_deaths = findViewById(R.id.tv_deaths);
        tv_recovered = findViewById(R.id.tv_recovered);
        tv_active = findViewById(R.id.tv_active);
        tv_Active_per_one_million = findViewById(R.id.tv_activepermil);
        tv_affected_countries = findViewById(R.id.tv_countries);


        scrollView = findViewById(R.id.scrollView);
        pieChart  = findViewById(R.id.piechart);
        simpleArcLoader = findViewById(R.id.loader);

        fetchData();


    }

    private void fetchData() {

        String url = "https://corona.lmao.ninja/v3/covid-19/all";
//        String url = "https://corona.lmao.ninja/v3/covid-19/countries/MYS";
        simpleArcLoader.start();

        StringRequest stringRequest  = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response.toString());

                    tv_cases.setText(jsonObject.getString("cases"));
                    tv_deaths.setText(jsonObject.getString("deaths"));
                    tv_recovered.setText(jsonObject.getString("recovered"));
                    tv_active.setText(jsonObject.getString("active"));
                    tv_Active_per_one_million.setText(jsonObject.getString("activePerOneMillion"));
                    tv_affected_countries.setText(jsonObject.getString("affectedCountries"));

                    pieChart.addPieSlice(new PieModel("Cases",Integer.parseInt(tv_cases.getText().toString()), Color.parseColor("#FFC107")));
                    pieChart.addPieSlice(new PieModel("Recovered",Integer.parseInt(tv_recovered.getText().toString()), Color.parseColor("#1BBD22")));
                    pieChart.addPieSlice(new PieModel("Active",Integer.parseInt(tv_active.getText().toString()), Color.parseColor("#03A9F4")));
                    pieChart.addPieSlice(new PieModel("Deaths",Integer.parseInt(tv_deaths.getText().toString()), Color.parseColor("#FF1100")));

                    pieChart.startAnimation();


                    simpleArcLoader.stop();
                    simpleArcLoader.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);


                } catch (JSONException e) {



                        simpleArcLoader.stop();
                        simpleArcLoader.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);

                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {



                    simpleArcLoader.stop();
                    simpleArcLoader.setVisibility(View.GONE);



                scrollView.setVisibility(View.VISIBLE);

                Toast.makeText(getApplicationContext(), error.getMessage(),Toast.LENGTH_LONG).show();


            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }


    public void nextActivity(View view) {

//        startActivity(new Intent(getApplicationContext(),DetailsActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (t.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
}