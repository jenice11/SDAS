package com.example.sdas.Fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sdas.R;
import com.example.sdas.Utils.Common;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OverallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OverallFragment extends Fragment {
    TextView textViewScore,textViewComment;
    PieChart pieChart;
    DatabaseReference user_history;
    List<String> list = new ArrayList<>();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public OverallFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OverallFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OverallFragment newInstance(String param1, String param2) {
        OverallFragment fragment = new OverallFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overall, container, false);
        textViewScore = (TextView) view.findViewById(R.id.textViewScore);
        textViewComment = (TextView) view.findViewById(R.id.textViewComment);
        pieChart = (PieChart)view.findViewById(R.id.pieChart_view);

        getDataforSummaryHistoryOverall();

        return view;
    }

    private void getDataforSummaryHistoryOverall() {
        user_history = FirebaseDatabase.getInstance().getReference(Common.HISTORY).child(Common.loggedUser.getUid());
        user_history.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren())
                {
                    for (DataSnapshot snapchild : snap.getChildren()){
                        if(snapchild.getKey().equals("risk"))
                        {
                            list.add(snapchild.getValue().toString());
//                            System.out.println("COUNT getValue: " + snapchild.getValue().toString());
                        }
                    }

//                    System.out.println("COUNT list: " + list);

                    int countHigh = Collections.frequency(list, "High");
                    int countMedium = Collections.frequency(list, "Medium");
                    int countLow = Collections.frequency(list, "Low");

                    calculateScore(countHigh,countMedium,countLow);
                }
                System.out.println("COUNT list: " + list);
                list.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void calculateScore(int countHigh, int countMedium, int countLow){
        int high = 3;
        int medium = 2;
        int low = 1;
        int scorePositive,scoreNegative,overallScore;
        String comment = null;

        scorePositive = 100;
        scoreNegative = (countHigh * high) + (countMedium * medium) + (countLow * low);

        overallScore = scorePositive - scoreNegative;

        if(scoreNegative<=20)
        {
            textViewScore.setTextColor(Color.parseColor("#43A047"));
            textViewComment.setTextColor(Color.parseColor("#43A047"));
            comment = "You are at low risk!";
        }
        else if(scoreNegative <=40 && scoreNegative >=20)
        {
            textViewScore.setTextColor(Color.parseColor("#A84420"));
            textViewComment.setTextColor(Color.parseColor("#A84420"));
            comment = "You are at medium risk!";
        }
        else if(scoreNegative >=40)
        {
            textViewScore.setTextColor(Color.RED);
            textViewComment.setTextColor(Color.RED);
            comment = "You are at high risk!";
        }

        textViewScore.setText(String.valueOf(overallScore));
        textViewComment.setText(comment);

        initPieChart();
        showPieChart(scorePositive,scoreNegative);
    }

    private void showPieChart(int scorePositive, int scoreNegative) {

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = "";

        //initializing data
        Map<String, Integer> typeAmountMap = new HashMap<>();
        typeAmountMap.put("Positive",scorePositive);
        typeAmountMap.put("Negative",scoreNegative);

        //initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#DAF1FB"));
        colors.add(Color.parseColor("#3495BD"));


        //input data and fit data into pie chart entry
        for(String type: typeAmountMap.keySet()){
            pieEntries.add(new PieEntry(typeAmountMap.get(type).floatValue(), type));
        }

        //collecting the entries with label name
        PieDataSet pieDataSet = new PieDataSet(pieEntries,label);
        //setting text size of the value
        pieDataSet.setValueTextSize(12f);
        //providing color list for coloring different entries
        pieDataSet.setColors(colors);
        //grouping the data set from entry to chart
        PieData pieData = new PieData(pieDataSet);
        //showing the value of the entries, default true if not set
//        pieData.setValueFormatter(new PercentFormatter());
        // usage on whole data object
        pieData.setValueFormatter(new OverallFragment.MyValueFormatter());

        pieData.setDrawValues(true);
        pieChart.setDrawSliceText(false);

        pieChart.setData(pieData);
        pieChart.invalidate();

    }

    private void initPieChart(){
        //using percentage as values instead of amount
        pieChart.setUsePercentValues(true);

        //remove the description label on the lower left corner, default true if not set
        pieChart.getDescription().setEnabled(false);

        //enabling the user to rotate the chart, default true
        pieChart.setRotationEnabled(true);
        //adding friction when rotating the pie chart
        pieChart.setDragDecelerationFrictionCoef(0.9f);
        //setting the first entry start from right hand side, default starting from top
        pieChart.setRotationAngle(0);

        //highlight the entry when it is tapped, default true if not set
        pieChart.setHighlightPerTapEnabled(true);
        //adding animation so the entries pop up from 0 degree
        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        //setting the color of the hole in the middle, default white
        pieChart.setHoleColor(Color.parseColor("#000000"));

//        pieChart.setHoleRadius(10f);
//        pieChart.setTransparentCircleRadius(0f);
    }

    public static class MyValueFormatter implements IValueFormatter {
        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,###,##0.0");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            if(value < 5) return "";
            else return mFormat.format(value) + " %";
        }
    }
}